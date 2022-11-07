package com.example.r3eleaderboardviewer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class R3EClass {
    public final String name;
    public final String id;
    public final URL icon;
    public final Map<String, R3ECar> cars = new HashMap<>(); // key = car name

    public R3EClass(String name, String id) throws MalformedURLException {
        this.name = name;
        this.id = id;
        this.icon = new URL(Utils.getItemUrl(id));
    }

    public R3ECar getCar(String carName) {
        return cars.get(carName);
    }
}
