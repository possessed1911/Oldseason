package com.hawolt.oldseason.local;

import com.hawolt.http.Method;
import com.hawolt.http.Request;
import com.hawolt.http.Response;
import com.hawolt.logger.Logger;
import com.hawolt.oldseason.Main;
import com.hawolt.oldseason.web.Browser;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created: 20/07/2022 10:10
 * Author: Twitter @hawolt
 **/

public class SessionTracker implements Runnable {
    public static void launch() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new SessionTracker(), 0, 5, TimeUnit.SECONDS);
    }

    public static final Map<Long, ClientAssociation> cache = new HashMap<>();
    private static final List<Long> games = new ArrayList<>();

    public static void check(long gameId, long summonerId) {
        Logger.debug("Assert summoner {} is us for gameId {}", summonerId, gameId);
        if (!cache.containsKey(summonerId) || !Main.automatic.getState()) return;
        if (games.contains(gameId)) return;
        Logger.debug("Mark game {} as visited for {}", gameId, summonerId);
        SessionTracker.games.add(gameId);
        Logger.debug("Attempting to fetch participants for {} in game {}", summonerId, gameId);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> Browser.open(cache.get(summonerId)));
        service.shutdown();
    }

    @Override
    public void run() {
        try {
            List<LeagueClient> list = WMIC.retrieve();
            List<String> names = new ArrayList<>();
            for (LeagueClient client : list) {
                try {
                    String endpoint = String.format("https://127.0.0.1:%s/lol-summoner/v1/current-summoner/", client.getLeaguePort());
                    Request request = new LCUCall(client, endpoint, Method.GET, false);
                    Response response = request.execute();
                    JSONObject object = new JSONObject(response.getBodyAsString());
                    if (!object.has("displayName")) continue;
                    String name = object.getString("displayName");
                    names.add(name);
                    boolean match = cache.values().stream().anyMatch(o -> o.getProfile().getDisplayName().equals(name));
                    if (match) continue;
                    SummonerProfile profile = new SummonerProfile(object);
                    Logger.debug("Associate ourself as {}", profile.getSummonerId());
                    cache.put(profile.getSummonerId(), new ClientAssociation(client, profile));
                } catch (Exception e) {
                    //TODO ignored
                }
            }
            List<Long> tmp = new ArrayList<>(cache.keySet());
            for (int i = tmp.size() - 1; i >= 0; i--) {
                long summonerId = tmp.get(i);
                ClientAssociation association = cache.get(summonerId);
                String target = association.getProfile().getDisplayName();
                boolean match = false;
                for (String name : names) {
                    if (name.equals(target)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    Logger.debug("Remove our association as {}", summonerId);
                    cache.remove(summonerId);
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        }
    }

}
