package com.example.r3eleaderboardviewer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class R3ELeaderboard {
    private static final int MAX_ENTRIES = 5000;

    private List<String> carClassIds = new ArrayList<>(); // "class-..." for a full class, just a number for a single car.
    private int trackId = -1;
    private final Map<String, R3ELeaderboardEntry[]> entries = new HashMap<>();

    public R3ELeaderboard(int trackId, List<String> carClassIds) {
        this.trackId = trackId;
        this.carClassIds = carClassIds;
    }
    public R3ELeaderboard(int trackId, String carClassId) {
        this.trackId = trackId;
        this.carClassIds.add(carClassId);
    }
    public R3ELeaderboard(int trackId, int carId) {
        this.trackId = trackId;
        this.carClassIds.add(Integer.toString(carId));
    }
    public R3ELeaderboard(int trackId) {
        this.trackId = trackId;
    }

    public R3ELeaderboard() {}

    public R3ELeaderboardEntry[] getAllEntries() {
        int count = 0;
        for (R3ELeaderboardEntry[] entry : entries.values()) {
            count += entry.length;
        }
        R3ELeaderboardEntry[] res = new R3ELeaderboardEntry[count];
        int i = 0;
        for (R3ELeaderboardEntry[] entry : entries.values()) {
            for (R3ELeaderboardEntry e : entry) {
                res[i++] = e;
            }
        }

        Arrays.sort(res, new Comparator<R3ELeaderboardEntry>() {
            @Override
            public int compare(R3ELeaderboardEntry o1, R3ELeaderboardEntry o2) {
                return Double.compare(o1.laptimeSeconds, o2.laptimeSeconds);
            }
        });

        return res;
    }

    public boolean updateTrack(int trackId) {
        if (this.trackId != trackId) {
            this.trackId = trackId;
            this.entries.clear();

            boolean res = true;
            for (String carClassId : this.carClassIds) {
                res = res && addCarClass(carClassId);
            }

            return res;
        }
        return false;
    }

    public boolean updateCars(String[] carClassIds) {
        boolean res = true;
        for (String carClassId : carClassIds) {
            if (!this.carClassIds.contains(carClassId)) {
                res = res && addCarClass(carClassId);
            }
        }

        for (String carClassId : this.carClassIds) {
            if (!Arrays.asList(carClassIds).contains(carClassId)) {
                entries.remove(carClassId);
            }
        }

        this.carClassIds = new ArrayList<>(Arrays.asList(carClassIds));

        return res;
    }

    public boolean updateCars(int[] carIds) {
        String[] carClassIds = new String[carIds.length];
        for (int i = 0; i < carIds.length; i++) {
            carClassIds[i] = Integer.toString(carIds[i]);
        }
        return updateCars(carClassIds);
    }

    public boolean addCarClass(String carClassId) {
        if (!carClassIds.contains(carClassId)) {
            carClassIds.add(carClassId);
        }

        if (trackId != -1) {
            R3ELeaderboardEntry[] entries = R3EApis.getLeaderboard(trackId, carClassId, MAX_ENTRIES);
            Log.d("R3E", "Got " + entries.length + " entries for " + carClassId + " on " + trackId);
            if (entries == null) {
                return false;
            }

            this.entries.put(carClassId, entries);

            return true;
        }
        return false;
    }

    public boolean addCarClass(int carId) {
        return addCarClass(Integer.toString(carId));
    }

    public boolean removeCarClass(String carClassId) {
        if (carClassIds.contains(carClassId)) {
            carClassIds.remove(carClassId);
            entries.remove(carClassId);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeCarClass(int carId) {
        return removeCarClass(Integer.toString(carId));
    }
}
