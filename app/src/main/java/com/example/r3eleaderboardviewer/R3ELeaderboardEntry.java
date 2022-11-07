package com.example.r3eleaderboardviewer;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import org.threeten.bp.LocalDateTime;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class R3ELeaderboardEntry {
    public final LocalDateTime lapDate;
    public final Double championshipPoints;
    public final String drivingMode;
    public final Double laptimeSeconds;
    public final int position; // starts from 1
    public final R3EDriver driver;
    public final R3ETrackLayout trackLayout;
    public final R3ELivery livery;


    public R3ELeaderboardEntry(JSONObject entry, String carClassId, String trackLayoutId) throws JSONException, MalformedURLException {
        String[] fullDate = entry.getString("date_time").split("T");
        String[] date = fullDate[0].split("-");
        String[] time = fullDate[1].split(":");
        lapDate = LocalDateTime.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]), Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]));
        if (entry.has("champ_points")) {
            championshipPoints = entry.getDouble("champ_points");
        } else {
            championshipPoints = null;
        }
        drivingMode = entry.getString("driving_model");

        String laptime = entry.getString("laptime");
        laptimeSeconds = Utils.parseDuration(laptime);

        position = entry.getInt("global_index");

        JSONObject driverJson = entry.getJSONObject("driver");
        JSONObject countryJson = entry.getJSONObject("country");
        driver = new R3EDriver(
                driverJson.getString("name"),
                driverJson.getString("rank"),
                driverJson.getBoolean("vip"),
                new URL(driverJson.getString("avatar")),
                new URL(driverJson.getString("path")),
                countryJson.getString("code"),
                countryJson.getString("name"),
                entry.getString("team")
        );

        JSONObject trackJson = entry.getJSONObject("track");
        String trackName = trackJson.getString("name").split(" - ")[0];
        String trackLayoutName = trackJson.getString("name").split(" - ")[1];

        if (R3EData.trackLayouts.containsKey(trackLayoutId)) {
            trackLayout = R3EData.trackLayouts.get(trackLayoutId);
        } else {
            trackLayout = new R3ETrackLayout(trackLayoutName, new URL(trackJson.getString("image")));
        }


        JSONObject carJson = entry.getJSONObject("car_class").getJSONObject("car");
        Log.d("R3ELeaderboardEntry", "carJson: " + carJson.toString());
//        String carName = carJson.getString("name").split(" - ")[0];
//        String liveryName = carJson.getString("name").split(" - ")[1];

        R3ELivery tmpLivery = null; // for Java to know that livery is always initialized
//        if (carClassId.startsWith("class-")) {
//            String id = carClassId.substring(6);
//            if (R3EData.classes.containsKey(id)) {
//                tmpLivery = R3EData.classes.get(id).getCar(carName).getLivery(liveryName);
//            }
//        } else if (R3EData.cars.containsKey(carClassId)) {
//            tmpLivery = R3EData.cars.get(carClassId).getLivery(liveryName);
//        }
        if (tmpLivery == null) {
            tmpLivery = new R3ELivery(carJson.getString("name"), new URL(carJson.getString("icon")));
        }
        livery = tmpLivery;
    }

    public TableRow getTableRow(MainActivity activity, int position, Double bestLap, TableRow.LayoutParams[] params) {
        TableRow row = new TableRow(activity);

        List<View> views = new ArrayList<>();

        TextView positionView = new TextView(activity);
        positionView.setText(Integer.toString(position));
        views.add(positionView);

        TextView driverView = new TextView(activity);
        driverView.setText(driver.name);
        views.add(driverView);

        TextView lapTimeView = new TextView(activity);
        lapTimeView.setText(getLapTimeWithRelative(bestLap));
        views.add(lapTimeView);

        views.add(Utils.loadImageFromUrl(livery.icon.toString(), activity));

        int i = 0;
        for (View view : views) {
            view.setLayoutParams(params[i++]);
            if (view instanceof TextView) {
                ((TextView) view).setGravity(Gravity.CENTER_VERTICAL);
            }
            row.addView(view);
        }

        return row;
    }

    public String getLapTimeWithRelative(Double toLap) {
        Double diff = laptimeSeconds - toLap;
        if (diff == 0) {
            return Utils.formatDuration(laptimeSeconds);
        } else if (diff > 0) {
            return String.format("%s, +%s", Utils.formatDuration(laptimeSeconds), Utils.formatDuration(diff));
        } else { // should never happen
            return String.format("%s, -%s", Utils.formatDuration(laptimeSeconds), Utils.formatDuration(diff));
        }
    }
}
