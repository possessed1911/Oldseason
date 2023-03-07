package com.hawolt.oldseason.web;

/**
 * Created: 07/03/2023 05:53
 * Author: Twitter @hawolt
 **/

public enum Provider {
    OPGG("https://www.op.gg/multisearch/%s?summoners=%s", false, false),
    UGG("https://u.gg/multisearch?region=%s&summoners=%s", true, false),
    POROFESSOR("https://porofessor.gg/pregame/%s/%s", false, false),
    PORO("https://poro.gg/multi?region=%s&q=%s", false, true);
    //https://u.gg/multisearch?region=euw1&summoners=hawolt v4,hawolt v2
    private final String base;
    private final boolean platform, uppercase;

    Provider(String base, boolean platform, boolean uppercase) {
        this.uppercase = uppercase;
        this.platform = platform;
        this.base = base;
    }

    public String format(Namespace locale, String query) {
        String region = platform ? locale.getPlatformId() : locale.getCompetitiveRegion();
        return String.format(base, !uppercase ? region.toLowerCase() : region.toUpperCase(), query);
    }

    private static final Provider[] PROVIDERS = Provider.values();

    public static Provider find(String name) {
        for (Provider provider : PROVIDERS) {
            if (provider.name().equalsIgnoreCase(name)) return provider;
        }
        return null;
    }
}
