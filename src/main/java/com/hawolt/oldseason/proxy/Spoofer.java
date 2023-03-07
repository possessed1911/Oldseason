package com.hawolt.oldseason.proxy;

import com.hawolt.oldseason.local.SessionTracker;
import com.hawolt.oldseason.proxy.rtmp.RtmpPacket;
import com.hawolt.oldseason.utility.Base64GZIP;
import com.hawolt.oldseason.utility.ByteMagic;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

/**
 * Created: 04/03/2023 19:52
 * Author: Twitter @hawolt
 **/

public class Spoofer implements ByteSink {
    public static final int RTMP_VIDEO_CHUNK_SIZE = 128;
    private final List<byte[]> list = new LinkedList<>();

    private final byte[] headers, buffer;
    private final byte initial;

    public Spoofer(RtmpPacket packet) {
        this.initial = packet.getInitialHeader();
        this.buffer = spoof(packet.getBody());
        this.headers = adjust(packet.getHeader());
        int packages = (int) Math.ceil(buffer.length / 128D);
        for (int i = 0; i < packages; i++) {
            int start = i * RTMP_VIDEO_CHUNK_SIZE;
            int end = start + RTMP_VIDEO_CHUNK_SIZE;
            byte[] body = Arrays.copyOfRange(buffer, start, Math.min(end, buffer.length));
            list.add(body);
        }
    }

    private byte[] adjust(byte[] header) {
        header[3] = (byte) ((buffer.length & 0xFF0000) >> 16);
        header[4] = (byte) ((buffer.length & 0x00FF00) >> 8);
        header[5] = (byte) ((buffer.length & 0x0000FF));
        return header;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    @Override
    public void drain(OutputStream outputStream) throws IOException {
        for (byte[] bytes : list) {
            outputStream.write(initial);
            outputStream.write(headers);
            outputStream.write(bytes);
        }
    }

    private static byte[] spoof(byte[] b) {
        try {
            byte[] compressedPayload = new byte[]{0, 17, 99, 111, 109, 112, 114, 101, 115, 115, 101, 100, 80, 97, 121, 108, 111, 97, 100, 1, 1};
            if (ByteMagic.indexOf(b, compressedPayload) == -1) {
                return b;
            }
            byte[] target = new byte[]{0, 7, 112, 97, 121, 108, 111, 97, 100};
            int index = ByteMagic.indexOf(b, target);
            if (index == -1) return b;
            int amf0StringIndex = index + target.length;
            int major = b[amf0StringIndex + 1] & 0xFF;
            int minor = b[amf0StringIndex + 2] & 0xFF;
            int originalLength = (major << 8) | minor & 0xFF;
            byte[] payload = Arrays.copyOfRange(b, index + target.length + 3, index + target.length + 3 + originalLength);
            String uncompressed = Base64GZIP.unzipBase64(new String(payload));
            JSONObject object = new JSONObject(uncompressed);
            if (!object.has("championSelectState")) return b;
            JSONObject championSelectState = object.getJSONObject("championSelectState");
            JSONObject cells = championSelectState.getJSONObject("cells");
            JSONArray alliedTeam = cells.getJSONArray("alliedTeam");
            for (int i = 0; i < alliedTeam.length(); i++) {
                JSONObject given = alliedTeam.getJSONObject(0);
                SessionTracker.check(given.getLong("summonerId"));
                given.put("nameVisibilityType", "VISIBLE");
                alliedTeam.remove(0);
                alliedTeam.put(given);
            }
            byte[] gzipped = Base64GZIP.gzip(object.toString());
            byte[] encoded = Base64.getEncoder().encode(gzipped);
            String spoofed = new String(encoded);
            byte[] header = new byte[]{2, (byte) ((spoofed.length() >> 8) & 0xFF), (byte) (spoofed.length() & 0xFF)};
            byte[] adjusted = new byte[b.length - payload.length + encoded.length];
            System.arraycopy(b, 0, adjusted, 0, index + target.length);
            System.arraycopy(header, 0, adjusted, index + target.length, header.length);
            System.arraycopy(
                    encoded,
                    0,
                    adjusted,
                    index + target.length + header.length,
                    encoded.length
            );
            System.arraycopy(
                    b,
                    index + target.length + 3 + originalLength,
                    adjusted,
                    index + target.length + header.length + encoded.length,
                    b.length - (index + target.length + 3 + originalLength)
            );
            return adjusted;
        } catch (Exception e) {
            return b;
        }
    }
}