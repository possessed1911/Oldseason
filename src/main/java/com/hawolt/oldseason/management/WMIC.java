package com.hawolt.oldseason.management;

import com.hawolt.io.Core;
import com.hawolt.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 19/07/2022 18:45
 * Author: Twitter @hawolt
 **/

public class WMIC {

    private static String wmic() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("WMIC", "path", "win32_process", "get", "Caption,Processid,Commandline");
        builder.redirectErrorStream(true);
        Process process = builder.start();
        try (InputStream stream = process.getInputStream()) {
            return Core.read(stream).toString();
        }
    }

    public static List<String> retrieve() throws IOException {
        String self = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        System.out.println(self);
        List<String> list = new ArrayList<>();
        for (String line : wmic().split(System.lineSeparator())) {
            if (!line.startsWith("javaw") || !line.contains("Oldseason")) continue;
            String pid = line.substring(line.lastIndexOf("\"") + 1).trim();
            if (pid.equals(self)) continue;
            list.add(pid);
        }
        return list;
    }

    public static void kill(String pid) {
        try {
            Runtime.getRuntime().exec("TASKKILL /F /IM " + pid);
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}
