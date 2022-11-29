package com.example.r3eleaderboardviewer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class R3EClassGroup {
    public final String name;
    public final int iconDrawableId;
    public final URL[] icons;
    public final Map<String, R3EClass> classes; // key = car name

    public R3EClassGroup(String name, int iconDrawableId, R3EClass[] classes) {
        this.name = name;
        this.iconDrawableId = iconDrawableId;
        this.icons = Arrays.stream(classes).map(c -> c.icon).toArray(URL[]::new);
        this.classes = new HashMap<>();
        for (R3EClass c : classes) {
            this.classes.put(c.name, c);
        }
    }
}
