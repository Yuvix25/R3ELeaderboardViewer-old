package com.example.r3eleaderboardviewer;

import android.content.Context;
import android.util.*;

import org.json.*;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class R3EApis {
    private static final Map<String, String> xhrHeaders = new HashMap<String ,String>() {{
        put("X-Requested-With", "XMLHttpRequest");
    }};


    public static R3ELeaderboardEntry[] getLeaderboard(String url) {
        return getLeaderboard(url, 0);
    }

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
    public static R3ELeaderboardEntry[] getLeaderboard(int track, String carClass, int count, int cacheDays) {
        String url = String.format(Locale.getDefault(), "https://game.raceroom.com/leaderboard/listing/0?start=0&count=%d&track=%d&car_class=%s", count, track, carClass);
        return getLeaderboard(url, cacheDays);
    }

    /**
     * Get all leaderboard entries of a car+track combo.
     * @param track track id
     * @param carId car id
     * @param count entry count to retrieve
     * @return Array of entries.
     * @throws org.json.JSONException Invalid JSON.
     */
    public static R3ELeaderboardEntry[] getLeaderboard(int track, int carId, int count, int cacheDays) {
        return getLeaderboard(track, Integer.toString(carId), count, cacheDays);
    }

    private static R3ELeaderboardEntry[] getLeaderboard(String url, int cacheDays) {
        JSONObject res = HTTPJsonRequest(url, xhrHeaders, cacheDays, TimeUnit.DAYS);

        try {
            JSONObject content = res.getJSONObject("context").getJSONObject("c");
            JSONObject getQuery = content.getJSONObject("get");
            JSONArray jsonEntries = content.getJSONArray("results");
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
        return HTTPJsonRequest(url, 4, TimeUnit.DAYS);
    }


    public static R3ECompetition[] getActiveR3ECompetitions(int cacheDays) {
        String url = "https://game.raceroom.com/competitions?_pjax=true";
        JSONObject response = HTTPJsonRequest(url, xhrHeaders, cacheDays, TimeUnit.DAYS);
        try {
            // context -> c -> championships[]
            JSONArray competitions = response.getJSONObject("context").getJSONObject("c")
                    .getJSONArray("contests").getJSONObject(0).getJSONArray("competitions");

            R3ECompetition[] compArray = new R3ECompetition[competitions.length()];
            for (int i = 0; i < competitions.length(); i++) {
                compArray[i] = new R3ECompetition(competitions.getJSONObject(i));
            }
            Log.d("R3E", "Loaded " + compArray.length + " competitions");
            return compArray;
        } catch (Exception e) {
            Log.e("Bad API Result = ", e.toString());
            e.printStackTrace();
            return null;
        }
    }

    private static class CacheInterceptor implements Interceptor {
        public int maxAge;
        public TimeUnit timeUnit;

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());

            CacheControl cacheControl = new CacheControl.Builder()
                    .maxAge(maxAge, timeUnit)
                    .build();

            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheControl.toString())
                    .build();
        }
    }


    public static Call buildRequest(String requestURL, Map<String, String> headers, int cacheTime, TimeUnit cacheTimeUnit) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);

        Request.Builder builder = new okhttp3.Request.Builder().url(requestURL);


        if (cacheTime > 0) {
            CacheInterceptor cacheInterceptor = new CacheInterceptor();
            cacheInterceptor.maxAge = cacheTime;
            cacheInterceptor.timeUnit = cacheTimeUnit;
            clientBuilder = clientBuilder.addNetworkInterceptor(cacheInterceptor);
            clientBuilder = clientBuilder.cache(new Cache(new File(Utils.getContext().getCacheDir(), "http-cache"), 50 * 1024 * 1024));
            builder = builder.cacheControl(new okhttp3.CacheControl.Builder().maxAge(cacheTime, cacheTimeUnit).build());
        } else {
            clientBuilder = clientBuilder.cache(null);
        }

        OkHttpClient client = clientBuilder.build();


        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        Request request = builder.build();
        try {
            return client.newCall(request);
        } catch (Exception e) {
            Log.e("Error = ", e.toString());
            return null;
        }
    }

    public static String getRedirectedUrl(String url) {
        try {
            okhttp3.Response response = buildRequest(url, new HashMap<String, String>(), 7, TimeUnit.DAYS).execute();
            return response.request().url().toString();
        } catch (Exception e) {
            Log.e("Error = ", e.toString());
            return null;
        }
    }

    public static JSONObject HTTPJsonRequest(String requestURL, Map<String, String> headers, int cacheTime, TimeUnit cacheTimeUnit) {
        Log.d("HTTP Request", "Sending request to URL : " + requestURL);
        try {
            okhttp3.Response response = buildRequest(requestURL, headers, cacheTime, cacheTimeUnit).execute();
            Log.d("HTTP Request", "Response code: " + response.code());
            return new JSONObject(response.body().string());
        } catch (Exception e) {
            Log.e("Error = ", e.toString());
            return null;
        }
    }

    public static JSONObject HTTPJsonRequest(String requestURL) {
        return HTTPJsonRequest(requestURL, new HashMap<>(), 0, null);
    }

    public static JSONObject HTTPJsonRequest(String requestURL, int cacheTime, TimeUnit cacheTimeUnit) {
        return HTTPJsonRequest(requestURL, new HashMap<>(), cacheTime, cacheTimeUnit);
    }

    public static JSONObject HTTPJsonRequest(String requestURL, Map<String, String> headers) {
        return HTTPJsonRequest(requestURL, headers, 0, null);
    }
}
