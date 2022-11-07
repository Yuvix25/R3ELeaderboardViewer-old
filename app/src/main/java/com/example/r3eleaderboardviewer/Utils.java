package com.example.r3eleaderboardviewer;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class Utils {
    public static String r3ePath(String path) {
        path = path.replaceAll(" ", "-").toLowerCase();
        // replace all umlauts
        path = path.replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("ü", "u").replaceAll("ß", "ss").replaceAll("é", "e").replaceAll("è", "e").replaceAll("ê", "e").replaceAll("ë", "e").replaceAll("à", "a").replaceAll("â", "a").replaceAll("ä", "a").replaceAll("ç", "c").replaceAll("ï", "i").replaceAll("î", "i").replaceAll("ô", "o").replaceAll("ö", "o").replaceAll("ù", "u").replaceAll("û", "u").replaceAll("ü", "u");
        return path;
    }

    public static Double parseDuration(String duration) {
        if (!duration.contains("s")) {
            duration = "0h 0m 0s" + duration;
        }
        if (!duration.contains("m")) {
            duration = "0h 0m " + duration;
        }
        if (!duration.contains("h")) {
            duration = "0h " + duration;
        }

        String[] splitLaptime = duration.split("h");
        int hours = Integer.parseInt("0" + splitLaptime[0].replaceAll("\\s+", ""));

        splitLaptime = splitLaptime[1].split("m");
        int minutes = Integer.parseInt("0" + splitLaptime[0].replaceAll("\\s+", ""));

        splitLaptime = splitLaptime[1].split("s");
        Double seconds = Double.parseDouble("0" + splitLaptime[0].replaceAll("\\s+", ""));

        return hours * 3600 + minutes * 60 + seconds;
    }

    public static String formatDuration(Double duration) {
        int hours = (int) (duration / 3600);
        int minutes = (int) ((duration - hours * 3600) / 60);
        Double seconds = duration - hours * 3600 - minutes * 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%dh %dm %.3fs", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm %.3fs", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%.3fs", seconds);
        }
    }

    public static float dpToPx(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static ImageView loadImageFromUrl(String url, Context context) {
        ImageView img = new ImageView(context);
        Glide.with(context).asBitmap().load(url).placeholder(android.R.drawable.progress_indeterminate_horizontal).error(android.R.drawable.stat_notify_error).into(img);
        return img;
    }
}
