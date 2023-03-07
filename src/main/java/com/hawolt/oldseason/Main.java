package com.hawolt.oldseason;

import com.hawolt.logger.Logger;
import com.hawolt.oldseason.local.SessionTracker;
import com.hawolt.oldseason.management.TaskManager;
import com.hawolt.oldseason.proxy.Incoming;
import com.hawolt.oldseason.proxy.Outgoing;
import com.hawolt.oldseason.tray.ProviderPopupMenu;
import com.hawolt.oldseason.utility.SSL;
import com.hawolt.oldseason.web.Browser;

import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created: 04/03/2023 19:11
 * Author: Twitter @hawolt
 **/

public class Main {
    public static CheckboxMenuItem automatic = new CheckboxMenuItem("Browse CS automatically");
    private static final List<String> legacy = new ArrayList<String>() {{
        add("eun1");
        add("tr");
        add("ru");
    }};

    private static final List<ServerSocket> proxies = new ArrayList<>();
    private static final Map<Integer, String> map = new HashMap<>();

    public static void main(String[] args) {
        Logger.debug("Started oldseason at {}", new Date());
        try {
            for (String pid : TaskManager.retrieve()) {
                Logger.debug("Found another running Oldseason instance, killing {}", pid);
                TaskManager.kill(pid);
            }
        } catch (IOException e) {
            Logger.error(e);
        }
        try {
            SSL.bypass();
            SessionTracker.launch();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Logger.error(e);
        }
        try {
            Main.rewrite();
        } catch (IOException e) {
            JDialog dialog = new JDialog();
            dialog.setModal(true);
            JOptionPane.showMessageDialog(dialog, "Failed to rewrite system.yaml, please join our Discord for assistance");
            Logger.error("Failure during rewrite");
            Logger.error(e);
            System.exit(1);
        }
        try {
            Application.launch("Oldseason");
            Application.addMenuEntry("Start Oldseason Client", () -> {
                try {
                    String client = LocaleInstallation.getLeagueInstallDir().toPath().resolve("LeagueClient.exe").toString();
                    Logger.debug("execute: \"" + client + "\" --allow-multiple-clients");
                    Runtime.getRuntime().exec("\"" + client + "\" --allow-multiple-clients");
                } catch (IOException e) {
                    Logger.error(e);
                }
            });
            Main.automatic.setEnabled(false);
            Application.popup.add(automatic);
            Application.popup.add(new ProviderPopupMenu());
            Application.addMenuEntry("Github", () -> Browser.browse("https://github.com/Riotphobia/Oldseason"));
            Application.addMenuEntry("Twitter", () -> Browser.browse("https://twitter.com/hawolt"));
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                Logger.debug("setting up proxy on port {} for {}", entry.getKey(), entry.getValue());
                ServerSocket socket = new ServerSocket(entry.getKey());
                proxies.add(socket);
                ExecutorService service = Executors.newCachedThreadPool();
                service.execute(() -> {
                    do {
                        try {
                            Socket incoming = socket.accept();
                            Logger.debug("accepted connection on port {} relaying to {}", entry.getKey(), entry.getValue());
                            Socket outgoing = SSLSocketFactory.getDefault().createSocket(entry.getValue(), 2099);
                            service.execute(new Incoming(incoming, outgoing));
                            service.execute(new Outgoing(outgoing, incoming));
                        } catch (IOException e) {
                            Logger.error(e);
                        }
                    } while (!service.isShutdown());
                });
            }
            Application.addExitOption(() -> {
                proxies.forEach(proxy -> {
                    try {
                        proxy.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private static void rewrite() throws IOException {
        Path path = LocaleInstallation.SYSTEM_YAML.toPath();
        Logger.debug("system.yaml: {}", path);
        Path original = path.getParent().resolve("system.yaml.backup");
        List<String> lines;
        if (original.toFile().exists()) {
            lines = Files.readAllLines(original);
        } else {
            Files.write(original, Files.readAllBytes(path));
            lines = Files.readAllLines(path);
        }
        int number = 11110;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.trim().equals("lcds:")) continue;
            StringBuilder host = new StringBuilder(lines.get(i + 1));
            int indexOfHost = host.indexOf(":") + 2;
            String relay = host.substring(indexOfHost, host.length());
            int firstIndex = relay.indexOf(".");
            int secondIndex = relay.indexOf(".", firstIndex + 1);
            String region = relay.substring(firstIndex + 1, secondIndex);
            if (!legacy.contains(region)) relay = String.format("feapp.%s.lol.pvp.net", region);
            else relay = String.format("prod.%s.lol.riotgames.com", region);
            host.replace(indexOfHost, host.length(), "127.0.0.1");
            lines.set(i + 1, host.toString());
            StringBuilder port = new StringBuilder(lines.get(i + 2));
            int indexOfPort = port.indexOf(":") + 2;
            int mapping = ++number;
            port.replace(indexOfPort, port.length(), String.valueOf(mapping));
            lines.set(i + 2, port.toString());
            StringBuilder tls = new StringBuilder(lines.get(i + 4));
            int indexOfTLS = tls.indexOf(":") + 2;
            tls.replace(indexOfTLS, tls.length(), "false");
            lines.set(i + 4, tls.toString());
            Logger.debug("mapping {} to port {}", relay, mapping);
            map.put(mapping, relay);
            i += 4;
        }
        byte[] bytes = lines.stream().collect(Collectors.joining(System.lineSeparator())).getBytes();
        Files.write(path, bytes);
    }
}
