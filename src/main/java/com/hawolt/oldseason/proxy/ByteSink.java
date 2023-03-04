package com.hawolt.oldseason.proxy;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created: 04/03/2023 19:54
 * Author: Twitter @hawolt
 **/
public interface ByteSink {
    void drain(OutputStream outputStream) throws IOException;
}
