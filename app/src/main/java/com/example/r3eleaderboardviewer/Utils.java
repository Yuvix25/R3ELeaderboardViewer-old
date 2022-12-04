package com.example.r3eleaderboardviewer;

import static androidx.core.content.ContextCompat.getColor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Utils {

    private static Context context;

    public static void setContext(Context context) {
        Utils.context = context;
    }
    public static Context getContext() {
        return context;
    }

    public static String getItemUrl(int id) {
        return "https://game.raceroom.com/store/image_redirect?size=small&id=" + id;
    }
    public static String getItemUrl(String id) {
        return getItemUrl(Integer.parseInt(id));
    }

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

    public static String[] splitDateTimeFromTimezone(String datetime) {
        if (datetime.contains("+")) {
            String[] res = datetime.split("\\+");
            return new String[] {res[0], "+" + res[1]};
        } else if (stringCount(datetime, "-") == 3) {
            String[] res = datetime.split("\\-");
            return new String[] {res[0] + "-" + res[1] + "-" + res[2], "-" + res[3]};
        } else {
            return new String[] {datetime, "+00:00"};
        }
    }

    public static int stringCount(String string, String substring) {
        return (string.length() - string.replace(substring, "").length()) / substring.length();
    }

    public static void openCompetition(Context context, R3ECompetition comp) {
        Intent i = new Intent(context, CompetitionLeaderboardActivity.class);
        i.putExtra("compId", comp.id);
        i.putExtra("compName", comp.name);
        context.startActivity(i);
    }

    public static ImageView loadImageFromUrl(String url, Context context, int padToSquare, Utils.ParameterizedRunnable callback, boolean getUri, boolean retry) {
        ImageView img = new ImageView(context);
        RequestBuilder<Bitmap> requestBuilder = Glide.with(context)
                .asBitmap()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .load(url);

        if (getUri) {
            requestBuilder = requestBuilder.placeholder(android.R.drawable.progress_indeterminate_horizontal);
        }

        if (padToSquare != 0) {
            requestBuilder = requestBuilder.apply(RequestOptions.bitmapTransform(new BitmapTransformation() { // pad image to square
                @Override
                protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                    int size = toTransform.getWidth() + padToSquare * 2;
                    Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                    output.eraseColor(ContextCompat.getColor(context, R.color.colorChipViewBackground));
                    Canvas can = new Canvas(output);
                    can.drawBitmap(toTransform, padToSquare, ((float)(size - toTransform.getHeight())) / 2, null);
                    return output;
                }

                @Override
                public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {}
            }));
        }

        if (callback == null) {
            requestBuilder.into(img);
        } else {
            requestBuilder.into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    img.setImageBitmap(resource);

                    if (getUri) {
                        requestUriPermission((Activity) context);
                        int i = 0;
                        while (!hasUriPermission(context)) {
                            if (i > 300) { // 30 seconds
                                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                alertDialog.setTitle("Permission Denied");
                                alertDialog.setMessage("It took you too long to grant the permission, or you denied it. Please restart the app and grant the permission.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                System.exit(1);
                                            }
                                        });
                                alertDialog.show();
                                break;
                            }

                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                        String uri = getImageUri(context, resource);
                        img.setTag(uri);
                    }

                    callback.run(img, true, resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {}

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    Log.d("Glide", "onLoadFailed: " + url);
                    if (retry) {
                        loadImageFromUrl(url, context, padToSquare, callback, getUri, false);
                    } else {
                        callback.run(img, false);
                    }
                }
            });
        }


        return img;
    }

    public static ImageView loadImageFromUrl(String url, Context context) {
        return loadImageFromUrl(url, context, 0, null, false, true);
    }

    public static ImageView loadImageFromUrl(String url, Context context, Utils.ParameterizedRunnable uriCallback) {
        return loadImageFromUrl(url, context, 0, uriCallback, false, true);
    }

    public static ImageView loadImageFromUrl(String url, Context context, int padToSquare) {
        return loadImageFromUrl(url, context, padToSquare, null, false, true);
    }

    public static ImageView loadImageFromUrl(String url, Context context, int padToSquare, Utils.ParameterizedRunnable uriCallback) {
        return loadImageFromUrl(url, context, padToSquare, uriCallback, false, true);
    }



    private static void requestUriPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    private static boolean hasUriPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }


    public static String getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, UUID.randomUUID().toString() + ".png", "drawing");
        return path;
    }

    public static abstract class ParameterizedRunnable implements Runnable {
        private Object[] params;

        /**
         * @param params: parameters you want to pass the the runnable.
         */
        public ParameterizedRunnable(Object... params) {
            this.params = params;
        }

        /**
         * Code you want to run
         *
         * @param params:parameters you want to pass the the runnable.
         */
        protected abstract void run(Object... params);

        @Override
        public final void run() {
            run(params);
        }

        /**
         * setting params
         */
        public void setParams(Object... params) {
            this.params = params;
        }

        /**
         * getting params
         */
        public Object[] getParams() {
            return params;
        }
    }
}
