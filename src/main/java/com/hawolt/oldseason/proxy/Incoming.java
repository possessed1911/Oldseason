package com.hawolt.oldseason.proxy;

import com.hawolt.logger.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created: 04/03/2023 19:45
 * Author: Twitter @hawolt
 **/

public class Incoming extends Connection {

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