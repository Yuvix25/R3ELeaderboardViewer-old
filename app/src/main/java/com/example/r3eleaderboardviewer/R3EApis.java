package com.example.r3eleaderboardviewer;

import android.util.*;

import org.json.*;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class R3EApis {
    /**
     * Get all entries of a competition leaderboard
     * @param leaderboardId competition id
     * @param count entry count to retrieve
     * @return Array of entries.
     * @throws org.json.JSONException Invalid JSON.
     */
    public static R3ELeaderboardEntry[] getLeaderboard(int leaderboardId, int count) {
        String url = String.format(Locale.getDefault(), "https://game.raceroom.com/leaderboard/listing/%d?start=0&count=%d", leaderboardId, count);
        return getLeaderboard(url);
    }

    /**
     * Get all leaderboard entries of a (car class/single car)+track combo.
     * @param track track id
     * @param carClass "class-[class_id]" for an entire class, only the "car_id" for a single car.
     * @param count entry count to retrieve
     * @return Array of entries.
     * @throws org.json.JSONException Invalid JSON.
     */
    public static R3ELeaderboardEntry[] getLeaderboard(int track, String carClass, int count) {
        String url = String.format(Locale.getDefault(), "https://game.raceroom.com/leaderboard/listing/0?start=0&count=%d&track=%d&car_class=%s", count, track, carClass);
        return getLeaderboard(url);
    }

    /**
     * Get all leaderboard entries of a car+track combo.
     * @param track track id
     * @param carId car id
     * @param count entry count to retrieve
     * @return Array of entries.
     * @throws org.json.JSONException Invalid JSON.
     */
    public static R3ELeaderboardEntry[] getLeaderboard(int track, int carId, int count) {
        return getLeaderboard(track, Integer.toString(carId), count);
    }

    private static R3ELeaderboardEntry[] getLeaderboard(String url) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");

        JSONObject res = HTTPJsonRequest(url, headers);

        try {
            JSONArray jsonEntries = res.getJSONObject("context").getJSONObject("c").getJSONArray("results");
            R3ELeaderboardEntry[] entries = new R3ELeaderboardEntry[jsonEntries.length()];
            for (int i = 0; i < jsonEntries.length(); i++) {
                entries[i] = new R3ELeaderboardEntry(jsonEntries.getJSONObject(i));
            }
            return entries;
        } catch (Exception e) {
            Log.e("Bad API Result = ", e.toString());
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject getR3EDataFile() {
        String url = "https://raw.githubusercontent.com/sector3studios/r3e-spectator-overlay/master/r3e-data.json";
        return HTTPJsonRequest(url);
    }

    public static JSONObject HTTPJsonRequest(String requestURL, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            URL object = new URL(requestURL);
            connection = (HttpURLConnection) object.openConnection();
            connection.setRequestMethod("GET");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            StringBuilder stringBuilder = new StringBuilder();
            Log.d("HTTP Request", "Sending request to URL : " + requestURL);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader streamReader = new InputStreamReader(
                        connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(
                        streamReader);
                String response;
                while ((response = bufferedReader.readLine()) != null) {
                    stringBuilder.append(response);
                    stringBuilder.append("\n");
                }
                bufferedReader.close();
                return new JSONObject(stringBuilder.toString());
            } else {
                Log.e("Error = ", connection.getResponseMessage());
                return null;
            }
        } catch (Exception exception) {
            Log.e("Error = ", exception.toString());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static JSONObject HTTPJsonRequest(String requestURL) {
        return HTTPJsonRequest(requestURL, new HashMap<>());
    }
}
