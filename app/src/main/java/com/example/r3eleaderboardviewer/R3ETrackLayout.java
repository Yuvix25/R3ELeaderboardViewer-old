package com.example.r3eleaderboardviewer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class R3ETrackLayout {
    public final String name;
    public final String id;
    public final URL icon;
    public final R3ETrack track;

    public R3ETrackLayout(String name, String id, R3ETrack track) throws MalformedURLException {
        this.name = name;
        this.id = id;
        this.icon = new URL(Utils.getItemUrl(id));
        this.track = track;
    }

    public R3ETrackLayout(String name, URL icon) {
        this.name = name;
        this.id = null;
        this.icon = icon;
        this.track = null;
    }

    public String getDisplayName() {
        return track.name + " - " + name;
    }
}
