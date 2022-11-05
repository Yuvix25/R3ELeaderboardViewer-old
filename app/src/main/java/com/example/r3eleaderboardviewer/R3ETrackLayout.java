package com.example.r3eleaderboardviewer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class R3ETrackLayout {
    public final String name;
    public String trackName;
    public final URL icon;

    public R3ETrackLayout(String name, URL icon) {
        this.name = name;
        this.icon = icon;
    }

    public R3ETrackLayout(String name, String trackName, int trackId) throws MalformedURLException {
        this.name = name;
        this.trackName = trackName;
        String iconUrl = String.format(Locale.getDefault(), "https://prod.r3eassets.com/assets/content/tracklayout/%s-%s-%d-image-small.webp", Utils.r3ePath(trackName), Utils.r3ePath(name), trackId);
        this.icon = new URL(iconUrl);
    }
}
