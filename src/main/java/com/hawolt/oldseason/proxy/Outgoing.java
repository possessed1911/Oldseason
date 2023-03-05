package com.hawolt.oldseason.proxy;

import com.hawolt.logger.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created: 04/03/2023 19:45
 * Author: Twitter @hawolt
 **/

public class Outgoing extends Connection {

    public Outgoing(Socket in, Socket out) {
        super(in, out);
    }

    @Override
    public void run() {
        CustomPacketReader customPacketReader = new CustomPacketReader();
        try (InputStream input = in.getInputStream()) {
            OutputStream stream = out.getOutputStream();
            int code, line = 0;
            while (in.isConnected() && (code = input.read()) != -1) {
                if (line >= 1) {
                    try {
                        customPacketReader.read((byte) code, input, stream);
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                } else if (out.isConnected()) {
                    stream.write(read(input, code, input.available()));
                    line++;
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
