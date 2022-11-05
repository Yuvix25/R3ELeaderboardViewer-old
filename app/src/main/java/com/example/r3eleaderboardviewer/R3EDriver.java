package com.example.r3eleaderboardviewer;

import java.net.URL;

public class R3EDriver {
    public final String name;
    public final String rank;
    public final boolean isVIP;
    public final URL avatarImage;
    public final URL profileUrl;
    public final String countryCode;
    public final String countryName;
    public final String team;

    public R3EDriver(String name, String rank, boolean isVIP, URL avatarImage, URL profileUrl, String countryCode, String countryName, String team) {
        this.name = name;
        this.rank = rank;
        this.isVIP = isVIP;
        this.avatarImage = avatarImage;
        this.profileUrl = profileUrl;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.team = team;
    }
}
