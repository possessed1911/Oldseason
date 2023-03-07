package com.hawolt.oldseason.local;

/**
 * Created: 19/07/2022 19:44
 * Author: Twitter @hawolt
 **/

public class LeagueClient {
    private final String riotAuth, leagueAuth, riotPort, leaguePort;

    public LeagueClient(String riotAuth, String riotPort, String leagueAuth, String leaguePort) {
        this.riotAuth = riotAuth;
        this.riotPort = riotPort;
        this.leagueAuth = leagueAuth;
        this.leaguePort = leaguePort;
    }

    public String getRiotAuth() {
        return riotAuth;
    }

    public String getLeagueAuth() {
        return leagueAuth;
    }

    public String getRiotPort() {
        return riotPort;
    }

    public String getLeaguePort() {
        return leaguePort;
    }

    @Override
    public String toString() {
        return "LeagueClient{" +
                "riotAuth='" + riotAuth + '\'' +
                ", leagueAuth='" + leagueAuth + '\'' +
                ", riotPort='" + riotPort + '\'' +
                ", leaguePort='" + leaguePort + '\'' +
                '}';
    }
}
