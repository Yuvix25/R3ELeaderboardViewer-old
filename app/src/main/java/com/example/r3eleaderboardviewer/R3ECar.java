package com.example.r3eleaderboardviewer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class R3ECar {
    public final String name;
    public final String id;
    public final R3EClass carClass;
    public final String defaultLiveryName;
    public final Map<String, R3ELivery> liveries = new HashMap<>(); // key = livery name

    public R3ECar(String name, String id, R3EClass carClass, String defaultLiveryName) {
        this.name = name;
        this.id = id;
        this.carClass = carClass;
        this.defaultLiveryName = defaultLiveryName;
    }

    public R3ELivery getLivery(String liveryName) {
        return liveries.get(liveryName);
    }

    public URL getIcon() {
        return getLivery(defaultLiveryName).icon;
    }
}
