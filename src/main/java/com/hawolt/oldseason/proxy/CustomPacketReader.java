package com.hawolt.oldseason.proxy;

import com.hawolt.rtmp.io.RtmpPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 04/03/2023 19:50
 * Author: Twitter @hawolt
 **/

public class CustomPacketReader {
    private final Map<Integer, RtmpPacket> map = new HashMap<>();

    public void read(byte initialHeader, InputStream input, OutputStream outputStream) throws IOException {
        int channel = initialHeader & 0x2F;
        int headerType = initialHeader & 0xC0;

        int headerSize = 0;
        if (headerType == 0x00) {
            headerSize = 12;
        } else if (headerType == 0x40) {
            headerSize = 8;
        } else if (headerType == 0x80) {
            headerSize = 4;
        } else if (headerType == 0xC0) {
            headerSize = 1;
        }

        if (!map.containsKey(channel)) map.put(channel, new RtmpPacket(initialHeader));
        RtmpPacket packet = map.get(channel);

        if (headerSize > 1) {
            byte[] header = new byte[headerSize - 1];
            packet.setHeaderSize(header.length);
            for (int i = 0; i < header.length; i++) {
                header[i] = (byte) input.read();
                packet.addToHeader(header[i]);
            }

            if (headerSize >= 8) {
                int size = 0;
                for (int i = 3; i < 6; i++) {
                    size = size * 256 + (header[i] & 0xFF);
                }
                packet.setBodySize(size);
                packet.setMessageType(header[6]);
            }
        }

        for (int i = 0; i < 128; i++) {
            byte b = (byte) input.read();
            packet.addToBody(b);
            if (packet.isComplete()) break;
        }

        if (!packet.isComplete()) return;
        map.remove(channel);

        Spoofer spoofer = new Spoofer(packet);
        spoofer.drain(outputStream);
    }
}
