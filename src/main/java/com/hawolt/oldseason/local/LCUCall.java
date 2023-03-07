package com.hawolt.oldseason.local;

import com.hawolt.http.Method;
import com.hawolt.http.Request;

import java.io.IOException;
import java.util.Base64;

public class LCUCall extends Request {

    private final LeagueClient client;

    public LCUCall(LeagueClient client, String endpoint, Method method, boolean output) throws IOException {
        super(endpoint, method, output);
        this.client = client;
        prepareRequest();
    }

    private void prepareRequest() {
        addHeader("User-Agent", "LeagueOfLegendsClient/");
        addHeader("Accept", "application/json");
        addHeader("Content-type", "application/json");
        addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(("riot:" + client.getLeagueAuth()).getBytes()));
    }
}
