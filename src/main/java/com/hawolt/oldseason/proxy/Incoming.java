package com.hawolt.oldseason.proxy;

import com.hawolt.logger.Logger;
import com.hawolt.rtmp.amf.decoder.AMFDecoder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created: 04/03/2023 19:45
 * Author: Twitter @hawolt
 **/

public class Incoming extends Connection {
    private final AMFDecoder decoder = new AMFDecoder() {
        @Override
        public byte read() {
            byte b = data[position++];
            if ((((int) b) & 0xFF) == 0xC3) return read();
            return b;
        }
    };

    public Incoming(Socket in, Socket out) {
        super(in, out);
    }

    @Override
    public void run() {
        try (InputStream input = in.getInputStream()) {
            OutputStream stream = out.getOutputStream();
            int code;
            while (in.isConnected() && (code = input.read()) != -1) {
                if (out.isConnected()) stream.write(read(input, code, input.available()));
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}