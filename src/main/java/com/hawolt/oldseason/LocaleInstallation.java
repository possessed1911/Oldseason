package com.hawolt.oldseason;

import com.hawolt.logger.Logger;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 04/03/2023 19:17
 * Author: Twitter @hawolt
 **/

public class LocaleInstallation {

    public static File LEAGUE_INSTALL_DIR, SYSTEM_YAML;

    static {
        try {
            LEAGUE_INSTALL_DIR = getLeagueInstallDir();
            SYSTEM_YAML = locateYaml(LEAGUE_INSTALL_DIR);
        } catch (IOException e) {
            Logger.error(e);
            System.err.println("Unable to locate RiotClientServices.exe or system.yaml, exiting (1).");
            System.exit(1);
        }
    }

    private static void exit() {
        Logger.debug("Unable to identify {}", StaticConstants.RIOT_INSTALLS_JSON);
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        String message = String.format("Failed to locate %s, please join our Discord for assistance", StaticConstants.RIOT_INSTALLS_JSON);
        JOptionPane.showMessageDialog(dialog, message);
        System.exit(1);
    }

    public static File getLeagueInstallDir() throws IOException {
        Logger.debug("identified ALLUSERSPROFILE as {}", System.getenv("ALLUSERSPROFILE"));
        File file = Paths.get(System.getenv("ALLUSERSPROFILE"))
                .resolve(StaticConstants.RIOT_GAMES)
                .resolve(StaticConstants.RIOT_INSTALLS_JSON).toFile();
        if (!file.exists()) exit();
        Logger.debug("successfully found RiotInstalls.json");
        JSONObject object = new JSONObject(new String(Files.readAllBytes(file.toPath())));
        String live = object.getString("rc_live");
        JSONObject clients = object.getJSONObject("associated_client");
        List<String> list = new ArrayList<>();
        for (String key : clients.keySet()) {
            if (key.contains("PBE") || key.contains("VALORANT") || key.contains("LoR") || !clients.get(key).equals(live))
                continue;
            list.add(key);
        }
        return list.stream().map(File::new)
                .filter(File::exists)
                .findAny()
                .orElseGet(LocaleInstallation::get);
    }

    public static File locateYaml(File leagueInstallDir) throws FileNotFoundException {
        if (leagueInstallDir == null || !leagueInstallDir.exists()) {
            throw new FileNotFoundException("Unable to locate system.yaml");
        }
        return leagueInstallDir.toPath()
                .resolve(StaticConstants.SYSTEM_YAML)
                .toFile();
    }

    private static File get() {
        Logger.debug("opening file chooser dialog");
        JOptionPane.showMessageDialog(null, "Please locate and select your League of Legends directory");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private static List<String> load(List<String> list, JSONObject object) {
        for (String key : object.keySet()) {
            if (object.get(key) instanceof JSONObject) {
                load(list, object.getJSONObject(key));
            } else {
                list.add(object.getString(key));
            }
        }
        return list;
    }
}