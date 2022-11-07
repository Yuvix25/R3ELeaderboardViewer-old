package com.example.r3eleaderboardviewer;

import java.net.MalformedURLException;
import java.net.URL;

public class R3ELivery {
    public final String name;
    public final String id;
    public final URL icon;
    public final R3ECar car;

    public R3ELivery(String name, String id, R3ECar car) throws MalformedURLException {
        this.name = name;
        this.id = id;
        this.icon = new URL(Utils.getItemUrl(id));
        this.car = car;
    }

    public R3ELivery(String name, URL icon) {
        this.name = name;
        this.id = null;
        this.icon = icon;
        this.car = null;
    }
}
