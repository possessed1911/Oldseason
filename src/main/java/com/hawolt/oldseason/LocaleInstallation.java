package com.hawolt.oldseason;

import com.hawolt.logger.Logger;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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

    public static File RIOT_CLIENT_SERVICES, SYSTEM_YAML;

    static {
        try {
            RIOT_CLIENT_SERVICES = getRiotClientServices();
            SYSTEM_YAML = locateYaml(RIOT_CLIENT_SERVICES);
        } catch (IOException e) {
            Logger.error(e);
            System.err.println("Unable to locate RiotClientServices.exe or system.yaml, exiting (1).");
            System.exit(1);
        }
    }

    public static File getRiotClientServices() throws IOException {
        Logger.debug("identified ALLUSERSPROFILE as {}", System.getenv("ALLUSERSPROFILE"));
        File file = Paths.get(System.getenv("ALLUSERSPROFILE"))
                .resolve(StaticConstants.RIOT_GAMES)
                .resolve(StaticConstants.RIOT_INSTALLS_JSON).toFile();
        if (!file.exists()) {
            Logger.debug("Unable to identify {}", StaticConstants.RIOT_INSTALLS_JSON);
            return getRiotClientServices();
        }
        Logger.debug("successfully found RiotInstalls.json");
        JSONObject object = new JSONObject(new String(Files.readAllBytes(file.toPath())));
        List<String> list = load(new ArrayList<>(), object);
        return list.stream().map(File::new)
                .filter(File::exists)
                .findAny()
                .orElseGet(LocaleInstallation::get);
    }

    public static File locateYaml(File riotClientServices) throws FileNotFoundException {
        if (riotClientServices == null || !riotClientServices.exists()) {
            throw new FileNotFoundException("Unable to locate system.yaml");
        }
        return riotClientServices.toPath()
                .getParent()
                .getParent()
                .resolve(StaticConstants.LEAGUE_OF_LEGENDS)
                .resolve(StaticConstants.SYSTEM_YAML)
                .toFile();
    }

    private static File get() {
        Logger.debug("opening file chooser dialog");
        JOptionPane.showMessageDialog(null, "Please locate and select RiotClientServices.exe");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Executable Files", "exe"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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