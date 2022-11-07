package com.example.r3eleaderboardviewer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class R3ETrack {
    public final String name;
    public final String id;
    public final Map<String, R3ETrackLayout> layouts = new HashMap<>(); // key = layout name

    public R3ETrack(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public R3ETrackLayout getLayout(String layoutName) {
        return layouts.get(layoutName);
    }
}
