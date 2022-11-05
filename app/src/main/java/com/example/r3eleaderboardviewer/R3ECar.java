package com.example.r3eleaderboardviewer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class R3ECar {
    public final URL storeUrl;
    public final String name;
    public final String className;
    public final URL icon;

    public R3ECar(String name, String className, URL icon, URL storeUrl) {
        this.name = name;
        this.className = className;
        this.icon = icon;
        this.storeUrl = storeUrl;
    }

    public R3ECar(String name, String className, URL icon) {
        this(name, className, icon, null);
    }

    public R3ECar(String name, String className, String liveryName, String liveryTeamName, int liveryId, String manufacturer) throws MalformedURLException {
        String iconUrl = String.format(Locale.getDefault(), "https://prod.r3eassets.com/assets/content/carlivery/%s-%s-%d-image-big.webp", Utils.r3ePath(liveryTeamName), liveryName.replaceAll("#", ""), liveryId);
        String storeUrl = String.format(Locale.getDefault(), "https://game.raceroom.com/store/cars/%s/%s/%s", Utils.r3ePath(className), Utils.r3ePath(manufacturer), Utils.r3ePath(name));

        this.name = name;
        this.className = className;
        this.icon = new URL(iconUrl);
        this.storeUrl = new URL(storeUrl);
    }
}
