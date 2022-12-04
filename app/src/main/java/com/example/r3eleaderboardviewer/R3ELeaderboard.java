package com.example.r3eleaderboardviewer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class R3ELeaderboard {
    private static final int MAX_ENTRIES = 5000;

    private List<String> carClassIds = new ArrayList<>(); // "class-..." for a full class, just a number for a single car.
    private int trackId = -1;
    private final Map<String, R3ELeaderboardEntry[]> entries = new HashMap<>();
    private TableLayout table;
    private Activity context;

    public R3ELeaderboard(Activity context, TableLayout table) {
        this.context = context;
        this.table = table;
    }

    public List<R3ELeaderboardEntry> getAllEntries(boolean keepDuplicateDrivers) {
        List<R3ELeaderboardEntry> res = new ArrayList<>();
        for (R3ELeaderboardEntry[] entry : entries.values()) {
            res.addAll(Arrays.asList(entry));
        }

        Collections.sort(res, new Comparator<R3ELeaderboardEntry>() {
            @Override
            public int compare(R3ELeaderboardEntry o1, R3ELeaderboardEntry o2) {
                return Double.compare(o1.laptimeSeconds, o2.laptimeSeconds);
            }
        });

        List<R3ELeaderboardEntry> resClone = new ArrayList<>(res);

        if (!keepDuplicateDrivers)
            res = res.stream().filter(new Predicate<R3ELeaderboardEntry>() {
                @Override
                public boolean test(R3ELeaderboardEntry entry1) {
                    return resClone.stream().noneMatch(new Predicate<R3ELeaderboardEntry>() {
                        @Override
                        public boolean test(R3ELeaderboardEntry entry2) {
                            return entry1.driver.name.equals(entry2.driver.name) && entry2.laptimeSeconds < entry1.laptimeSeconds;
                        }
                    });
                }
            }).collect(Collectors.toList());

        return res;
    }

    public List<R3ELeaderboardEntry> getAllEntries() {
        return getAllEntries(false);
    }

    public void clear() {
        entries.clear();
        this.trackId = -1;
        this.carClassIds.clear();
    }

    public void setTable(TableLayout table) {
        this.table = table;
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

    public boolean updateCars(List<String> carClassIds) {
        boolean res = true;
        for (String carClassId : carClassIds) {
            if (!this.carClassIds.contains(carClassId)) {
                res = res && addCarClass(carClassId);
            }
        }

        for (String carClassId : this.carClassIds) {
            if (!carClassIds.contains(carClassId)) {
                entries.remove(carClassId);
            }
        }

        this.carClassIds = new ArrayList<>(carClassIds);

        return res;
    }

    public boolean addCarClass(String carClassId) {
        if (!carClassIds.contains(carClassId)) {
            carClassIds.add(carClassId);
        }

        if (trackId != -1) {
            R3ELeaderboardEntry[] entries = R3EApis.getLeaderboard(trackId, carClassId, MAX_ENTRIES);
            if (entries == null) {
                return false;
            }
            Log.d("R3E", "Got " + entries.length + " entries for " + carClassId + " on " + trackId);

            this.entries.put(carClassId, entries);

            if (this.table != null) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTable(context, table, null);
                    }
                });
            }

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


    public boolean updateCompetition(String compId) {
        R3ELeaderboardEntry[] entries = R3EApis.getLeaderboard(Integer.parseInt(compId), MAX_ENTRIES);
        if (entries == null) {
            return false;
        }
        Log.d("R3E", "Got " + entries.length + " entries for (competition): " + compId);

        this.entries.put(compId, entries);

        return true;
    }


    public void updateTable(Context context, TableLayout table, Runnable callback) {
        table.removeAllViews();
        List<R3ELeaderboardEntry> entries = getAllEntries();

        final int carImgWidth = 75;

        TableRow.LayoutParams positionParams = new TableRow.LayoutParams();
        positionParams.weight = 0;

        TableRow.LayoutParams nameParams = new TableRow.LayoutParams();
        nameParams.weight = 2;

        TableRow.LayoutParams laptimeParams = new TableRow.LayoutParams();
        laptimeParams.weight = 0;

        TableRow.LayoutParams carImgParams = new TableRow.LayoutParams();
        carImgParams.weight = 0;
        carImgParams.width = (int) Utils.dpToPx(carImgWidth, context);

        TableRow.LayoutParams[] allParams = new TableRow.LayoutParams[]{positionParams, nameParams, laptimeParams, carImgParams};
        for (TableRow.LayoutParams params : allParams) {
            params.rightMargin = (int) Utils.dpToPx(15f, context);
            params.height = TableRow.LayoutParams.MATCH_PARENT;
        }


        int i = 0;
        for (R3ELeaderboardEntry entry : entries) {
            table.addView(entry.getTableRow(context, ++i, entries.get(0).laptimeSeconds, allParams));
        }

        if (callback != null) {
            callback.run();
        }
    }
}
