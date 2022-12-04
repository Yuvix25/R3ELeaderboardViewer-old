package com.example.r3eleaderboardviewer;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class R3ECompetition {
    public final Date startDate;
    public final Date endDate;
    public final String timezone; // "+/-XX:00" for GMT+/-XX
    public final URL bannerImage;
    public final String id;
    public final String name;
    public final boolean isFixedSetup;

    public R3ECompetition(Date startDate, Date endDate, String timezone, URL bannerImage, String id, String name, boolean isFixedSetup) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.timezone = timezone;
        this.bannerImage = bannerImage;
        this.id = id;
        this.name = name;
        this.isFixedSetup = isFixedSetup;
    }

    public R3ECompetition(JSONObject obj) throws JSONException, MalformedURLException, ParseException {
        Log.d("R3ECompetition", obj.getString("start_date"));
        String[] splittedStart = Utils.splitDateTimeFromTimezone(obj.getString("start_date"));
        this.timezone = splittedStart[1];
        String startDate = splittedStart[0];
        String endDate = Utils.splitDateTimeFromTimezone(obj.getString("end_date"))[0];

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("GMT" + timezone));

        this.startDate = isoFormat.parse(startDate);
        this.endDate = isoFormat.parse(endDate);

        this.bannerImage = new URL(obj.getString("image"));
        this.id = obj.getInt("id") + "";
        this.name = obj.getString("name");
        this.isFixedSetup = obj.getBoolean("fixed_setup");
    }

    public CountDownTimer setCompetitionTimer(TextView text) {
        CountDownTimer timer = new CountDownTimer(endDate.getTime() - Calendar.getInstance().getTime().getTime(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                millisUntilFinished -= TimeUnit.DAYS.toMillis(days);

                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                millisUntilFinished -= TimeUnit.HOURS.toMillis(hours);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes);

                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                text.setText(days + "d, " + hours + "h, " + minutes + "m " + seconds + "s");
            }

            @Override
            public void onFinish() {
                text.setText("Competition ended");
            }
        };

        timer.start();
        return timer;
    }
}
