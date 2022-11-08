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

        cacheImage();
    }

    public R3ELivery(String name, URL icon) throws MalformedURLException {
        this.name = name;
        this.id = null;
        this.icon = new URL(icon.toString().replace("image-thumb", "image-small"));
        this.car = null;

        cacheImage();
    }

    private void cacheImage() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Utils.loadImageFromUrl(icon.toString(), Utils.getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
