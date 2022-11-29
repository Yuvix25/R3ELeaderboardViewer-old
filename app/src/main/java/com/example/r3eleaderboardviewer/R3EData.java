package com.example.r3eleaderboardviewer;

import android.util.Log;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class R3EData {
    public static final Map<String, R3ELivery> liveries = new HashMap<>();
    public static final Map<String, R3ECar> cars = new HashMap<>();
    public static final Map<String, R3EClass> classes = new HashMap<>();
    public static final Map<String, Set<String>> classGroupDefinitions = Map.of(
            "GT3 Group", Set.of("GTR 3", "ADAC GT Masters 2013", "ADAC GT Masters 2014", "ADAC GT Masters 2015", "ADAC GT Masters 2018", "ADAC GT Masters 2020", "ADAC GT Masters 21", "ADAC Esports GT Masters", "ADAC Esports GT Masters 2021", "DTM 2021", "DTM Esports 2022"),
            "WTCR Group", Set.of("WTCR 2018", "WTCR 2019", "WTCR 2020", "WTCR 2021", "WTCR 2022", "eSports WTCR", "Esports WTCR Prologue")
    );
    public static final Map<String, Integer> classGroupIcons = Map.of(
            "GT3 Group", R.drawable.gt3_group_icon,
            "WTCR Group", R.drawable.wtcr_group_icon_dark
    );
    public static final Map<String, R3EClassGroup> classGroups = new HashMap<>();
    public static final Map<String, R3ETrack> tracks = new HashMap<>();
    public static final Map<String, R3ETrackLayout> trackLayouts = new HashMap<>();

    public static boolean load() {
        JSONObject r3eData = R3EApis.getR3EDataFile();
        if (r3eData == null) {
            return false;
        }

        try {
            Iterator<String> classKeys = r3eData.getJSONObject("classes").keys();
            while (classKeys.hasNext()) {
                String classId = classKeys.next();
                JSONObject carClass = r3eData.getJSONObject("classes").getJSONObject(classId);

                R3EClass r3eClass = new R3EClass(carClass.getString("Name"), classId);

                classes.put(classId, r3eClass);

                JSONArray carIds = carClass.getJSONArray("Cars");
                for (int i = 0; i < carIds.length(); i++) {
                    String carId = Integer.toString(carIds.getJSONObject(i).getInt("Id"));
                    JSONObject car = r3eData.getJSONObject("cars").getJSONObject(carId);

                    JSONArray liveries = car.getJSONArray("liveries");

                    R3ECar r3eCar = new R3ECar(car.getString("Name"), carId, r3eClass, liveries.getJSONObject(0).getString("Name"));

                    r3eClass.cars.put(r3eCar.name, r3eCar);
                    cars.put(carId, r3eCar);

                    for (int j = 0; j < liveries.length(); j++) {
                        JSONObject livery = liveries.getJSONObject(j);
                        String liveryId = Integer.toString(livery.getInt("Id"));

                        R3ELivery r3eLivery = new R3ELivery(livery.getString("Name"), liveryId, r3eCar);

                        r3eCar.liveries.put(r3eLivery.name, r3eLivery);
                        R3EData.liveries.put(liveryId, r3eLivery);
                    }
                }
            }

            for (String classGroup : classGroupDefinitions.keySet()) {
                R3EClass[] ids = new R3EClass[classGroupDefinitions.get(classGroup).size()];

                int i = 0;
                for (String classId : classes.keySet()) {
                    if (classGroupDefinitions.get(classGroup).contains(classes.get(classId).name)) {
                        ids[i++] = classes.get(classId);
                    }
                }

                R3EClassGroup r3eClassGroup = new R3EClassGroup(classGroup, classGroupIcons.get(classGroup), ids);
                classGroups.put(classGroup, r3eClassGroup);
            }



            Iterator<String> trackKeys = r3eData.getJSONObject("tracks").keys();
            while (trackKeys.hasNext()) {
                String trackId = trackKeys.next();
                JSONObject track = r3eData.getJSONObject("tracks").getJSONObject(trackId);

                R3ETrack r3eTrack = new R3ETrack(track.getString("Name"), trackId);

                tracks.put(trackId, r3eTrack);

                JSONArray layouts = r3eData.getJSONObject("tracks").getJSONObject(trackId).getJSONArray("layouts");
                for (int i = 0; i < layouts.length(); i++) {
                    JSONObject layout = layouts.getJSONObject(i);
                    String layoutId = Integer.toString(layout.getInt("Id"));

                    R3ETrackLayout r3eTrackLayout = new R3ETrackLayout(layout.getString("Name"), layoutId, r3eTrack);

                    r3eTrack.layouts.put(r3eTrackLayout.name, r3eTrackLayout);
                    R3EData.trackLayouts.put(layoutId, r3eTrackLayout);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static List<R3ECarOrClass> getCarsForChips() {
        List<R3ECarOrClass> carPairs = new java.util.ArrayList<>();
        for (R3ECar car : cars.values()) {
            carPairs.add(new R3ECarOrClass(car));
        }
        return carPairs;
    }

    public static List<R3ETrackLayout> getTrackLayoutsForChips() {
        return new java.util.ArrayList<>(trackLayouts.values());
    }
}
