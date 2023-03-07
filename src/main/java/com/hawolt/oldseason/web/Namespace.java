package com.hawolt.oldseason.web;

import org.json.JSONObject;

/**
 * Created: 20/07/2022 14:30
 * Author: Twitter @hawolt
 **/

public class Namespace {
    private final String competitiveRegion;
    private final String platformId;

    public Namespace(JSONObject object) {
        JSONObject loginDataPacket = object.getJSONObject("LoginDataPacket");
        this.competitiveRegion = loginDataPacket.getString("competitiveRegion");
        this.platformId = loginDataPacket.getString("platformId");
    }

    //TODO for testing
    public Namespace(String competitiveRegion, String platformId) {
        this.competitiveRegion = competitiveRegion;
        this.platformId = platformId;
    }

    public String getCompetitiveRegion() {
        return competitiveRegion;
    }

    public String getPlatformId() {
        return platformId;
    }

    @Override
    public String toString() {
        return "Namespace{" +
                "competitiveRegion='" + competitiveRegion + '\'' +
                ", platform='" + platformId + '\'' +
                '}';
    }
}
