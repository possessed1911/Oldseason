package com.hawolt.oldseason;

import com.hawolt.io.Core;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created: 04/03/2023 17:11
 * Author: Twitter @hawolt
 **/

public class Application {

    public static final ScheduledExecutorService service = Executors.newScheduledThreadPool(4);
    public static final List<MenuItem> list = new ArrayList<>();

    private static SystemTray tray;
    private static PopupMenu popup;

    public static TrayIcon icon;

    private static BufferedImage loadIcon() throws IOException {
        return ImageIO.read(new ByteArrayInputStream(Core.read(RunLevel.get("oldseason.png")).toByteArray()));
    }

    private static void launchTray(String name, BufferedImage image) {
        Application.icon = new TrayIcon(image, name);
        Application.tray = SystemTray.getSystemTray();
        Application.popup = new PopupMenu();
        Application.icon.setPopupMenu(Application.popup);
        try {
            Application.tray.add(Application.icon);
        } catch (AWTException e) {
            Logger.error(e);
        }
    }

    public static void launch(String name) throws Exception {
        if (!SystemTray.isSupported()) throw new Exception("System Tray not supported");
        BufferedImage image = loadIcon();
        launchTray(name, image);
    }

    public static void addExitOption() {
        addExitOption(null);
    }


    public static void addMenuEntry(String text, Runnable action) {
        MenuItem credit = new MenuItem(text);
        credit.addActionListener(listener -> action.run());
        Application.popup.add(credit);
    }

    public static void addExitOption(Runnable runnable) {
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(listener -> {
            if (runnable != null) runnable.run();
            Application.tray.remove(Application.icon);
            service.shutdown();
            System.exit(0);
        });
        Application.popup.add(exit);
    }

}
