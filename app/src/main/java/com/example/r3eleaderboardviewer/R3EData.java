package com.example.r3eleaderboardviewer;

import android.util.Log;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class R3EData {
    public static Map<Integer, KeyPairBoolData> cars;
    public static Map<Integer, KeyPairBoolData> classes;
    public static Map<Integer, KeyPairBoolData> tracks;
    public static Map<Integer, KeyPairBoolData> trackLayouts;

    public static boolean load() {
        JSONObject r3eData = R3EApis.getR3EDataFile();
        if (r3eData == null) {
            return false;
        }

        try {
            Iterator<String> carKeys = r3eData.getJSONObject("cars").keys();
            cars = new HashMap<>();

            while (carKeys.hasNext()) {
                int carId = Integer.parseInt(carKeys.next());
                JSONObject car = r3eData.getJSONObject("cars").getJSONObject(Integer.toString(carId));

                String carName = car.getString("Name");
                int classId = car.getInt("Class");
                String liveryName = car.getJSONArray("liveries").getJSONObject(0).getString("Name");
                String liveryTeamName = car.getJSONArray("liveries").getJSONObject(0).getString("TeamName");
                int liveryId = car.getJSONArray("liveries").getJSONObject(0).getInt("Id");
                int manufacturerId = car.getInt("CarManufacturer");

                String carClass = r3eData.getJSONObject("classes").getJSONObject(Integer.toString(classId)).getString("Name");
                String manufacturer = r3eData.getJSONObject("manufacturers").getJSONObject(Integer.toString(manufacturerId)).getString("Name");

                KeyPairBoolData carData = new KeyPairBoolData(car.getString("Name"), false);
                carData.setId(carId);
                carData.setObject(new R3ECar(carName, carClass, liveryName, liveryTeamName, liveryId, manufacturer));

                cars.put(carId, carData);
            }

            Iterator<String> trackLayoutKeys = r3eData.getJSONObject("layouts").keys();
            trackLayouts = new HashMap<>();

            while (trackLayoutKeys.hasNext()) {
                int trackLayoutId = Integer.parseInt(trackLayoutKeys.next());
                JSONObject trackLayout = r3eData.getJSONObject("layouts").getJSONObject(Integer.toString(trackLayoutId));

                int trackId = trackLayout.getInt("Track");
                String trackName = r3eData.getJSONObject("tracks").getJSONObject(Integer.toString(trackId)).getString("Name");
                String trackLayoutName = trackLayout.getString("Name");

                KeyPairBoolData trackLayoutData = new KeyPairBoolData(trackName + " - " + trackLayoutName, false);
                trackLayoutData.setId(trackLayoutId);
                trackLayoutData.setObject(new R3ETrackLayout(trackLayoutName, trackName, trackId));

                trackLayouts.put(trackLayoutId, trackLayoutData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
