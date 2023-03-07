package com.hawolt.oldseason.local;

/**
 * Created: 07/03/2023 05:14
 * Author: Twitter @hawolt
 **/

public class ClientAssociation {
    private final SummonerProfile profile;
    private final LeagueClient client;

    public ClientAssociation(LeagueClient client, SummonerProfile profile) {
        this.profile = profile;
        this.client = client;
    }

    public SummonerProfile getProfile() {
        return profile;
    }

    public LeagueClient getClient() {
        return client;
    }
}
