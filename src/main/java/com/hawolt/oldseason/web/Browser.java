package com.hawolt.oldseason.web;

import com.hawolt.http.BasicHttp;
import com.hawolt.http.Method;
import com.hawolt.http.Request;
import com.hawolt.http.Response;
import com.hawolt.logger.Logger;
import com.hawolt.oldseason.local.ClientAssociation;
import com.hawolt.oldseason.local.LCUCall;
import com.hawolt.oldseason.local.LeagueClient;
import com.hawolt.oldseason.local.RiotCall;
import com.hawolt.oldseason.tray.ProviderPopupMenu;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created: 07/03/2023 05:34
 * Author: Twitter @hawolt
 **/

public class Browser {

    public static void open(ClientAssociation association) {
        LeagueClient client = association.getClient();
        try {
            Namespace namespace = getNamespace(client);
            Logger.info("Namespace: {}", namespace);
            long start = System.currentTimeMillis();
            JSONArray participants = new JSONArray();
            do {
                try {
                    participants = getChampSelect(client).getJSONArray("participants");
                    Logger.debug("We currently have {} participants", participants.length());
                } catch (Exception e) {
                    Logger.error(e);
                }
            } while (participants.length() != 5 && System.currentTimeMillis() - start <= TimeUnit.SECONDS.toMillis(10));
            Logger.debug("Exiting check with {} participants", participants.length());
            String query = participants.toList()
                    .stream()
                    .map(o -> (HashMap<?, ?>) o)
                    .map(JSONObject::new)
                    .map(o -> encode(o.getString("name")))
                    .collect(Collectors.joining(","));
            Logger.debug("q: {}", query);
            Provider provider = Provider.find(ProviderPopupMenu.selection);
            Logger.debug("p: {}", provider);
            if (provider == null) return;
            Logger.debug("Starting provider {}", provider);
            browse(provider.format(namespace, query));
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private static String encode(String o) {
        try {
            return URLEncoder.encode(o, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return o;
        }
    }

    private static JSONObject getChampSelect(LeagueClient client) throws IOException {
        String endpoint = String.format("https://127.0.0.1:%s/chat/v5/participants/champ-select", client.getRiotPort());
        Request request = new RiotCall(client, endpoint, Method.GET, false);
        Response response = request.execute();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getBody());
        return new JSONObject(BasicHttp.read(inputStream));
    }

    private static Namespace getNamespace(LeagueClient client) throws IOException {
        String endpoint = String.format("https://127.0.0.1:%s/lol-platform-config/v1/namespaces", client.getLeaguePort());
        Request request = new LCUCall(client, endpoint, Method.GET, false);
        Response response = request.execute();
        return new Namespace(new JSONObject(response.getBodyAsString()));
    }

    public static void browse(String target) {
        try {
            Desktop.getDesktop().browse(URI.create(target));
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}
