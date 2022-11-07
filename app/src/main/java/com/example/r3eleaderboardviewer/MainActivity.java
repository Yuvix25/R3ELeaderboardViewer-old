package com.example.r3eleaderboardviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.androidbuts.multispinnerfilter.SingleSpinnerListener;
import com.androidbuts.multispinnerfilter.SingleSpinnerSearch;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    R3ELeaderboard leaderboard;

    SingleSpinnerSearch selectTrack;
    MultiSpinnerSearch selectCar;

    int[] carIds;
    int trackId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidThreeTen.init(this);

        selectTrack = findViewById(R.id.trackSelect);
        selectTrack.setColorseparation(false);
        selectTrack.setSearchEnabled(true);
        selectTrack.setSearchHint("Find track");


        selectCar = findViewById(R.id.carSelect);
        selectCar.setSearchEnabled(true);
        selectCar.setSearchHint("Find car(s)");
        selectCar.setEmptyTitle("No cars found");
        selectCar.setShowSelectAllButton(false);
        selectCar.setClearText("Clear Selection");

        // get ChipsInput view
        ChipsInput chipsInput = (ChipsInput) findViewById(R.id.carSelector);

        List<CarChip> contactList = new ArrayList<>();
        contactList.add(new CarChip("Test", "Info", "https://prod.r3eassets.com/assets/content/carlivery/no-regrets-racing-1-257-image-thumb.png", "data", this));

        chipsInput.setFilterableList(contactList);

        updateSelectItems(new ArrayList<>(), new ArrayList<>());


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                leaderboard = new R3ELeaderboard();

                boolean res = R3EData.load();
                if (!res) {
                    Log.e("R3E", "Failed to load data");
                    return;
                }
                List<KeyPairBoolData> tracks = new ArrayList<>(R3EData.trackLayouts.values());
                List<KeyPairBoolData> cars = new ArrayList<>(R3EData.cars.values());

                updateSelectItems(tracks, cars);

                Log.d("R3E", "Loaded " + tracks.size() + " tracks and " + cars.size() + " cars.");
            }
        });

        thread.start();
    }

    private void updateSelectItems(List<KeyPairBoolData> tracks, List<KeyPairBoolData> cars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(tracks, new Comparator<KeyPairBoolData>() {
                    @Override
                    public int compare(KeyPairBoolData o1, KeyPairBoolData o2) {
                        R3ETrackLayout t1 = (R3ETrackLayout) o1.getObject();
                        R3ETrackLayout t2 = (R3ETrackLayout) o2.getObject();

                        if (t1.trackName.equals(t2.trackName)) {
                            return t1.name.compareTo(t2.name);
                        } else {
                            return t1.trackName.compareTo(t2.trackName);
                        }
                    }
                });

                Collections.sort(cars, new Comparator<KeyPairBoolData>() {
                    @Override
                    public int compare(KeyPairBoolData o1, KeyPairBoolData o2) {
                        R3ECar c1 = (R3ECar) o1.getObject();
                        R3ECar c2 = (R3ECar) o2.getObject();

                        if (c1.className.equals(c2.className)) {
                            return c1.name.compareTo(c2.name);
                        } else {
                            return c1.className.compareTo(c2.className);
                        }
                    }
                });

                for (int i = cars.size() - 1; i >= 0; i--) {
                    R3ECar car = (R3ECar) cars.get(i).getObject();
                    if (i < cars.size() - 1) {
                        R3ECar prevCar = (R3ECar) cars.get(i + 1).getObject();
                        if (!car.className.equals(prevCar.className)) {
                            KeyPairBoolData newClass = new KeyPairBoolData(prevCar.className, false);
                            cars.add(i + 1, newClass);
                        }
                    }
                }
                if (cars.size() > 0) {
                    R3ECar firstCar = (R3ECar) cars.get(0).getObject();
                    KeyPairBoolData newClass = new KeyPairBoolData(firstCar.className, false);
                    cars.add(0, newClass);
                }

                selectTrack.setItems(tracks, new SingleSpinnerListener() {
                    @Override
                    public void onItemsSelected(KeyPairBoolData selectedItem) {
                        trackId = (int) selectedItem.getId();
                        updateCarTrack();
                    }

                    @Override
                    public void onClear() {
                    }
                });

                selectCar.setItems(cars, new MultiSpinnerListener() {
                    @Override
                    public void onItemsSelected(List<KeyPairBoolData> items) {
                        carIds = new int[items.size()];
                        for (int i = 0; i < items.size(); i++) {
                            if (items.get(i).getObject() == null) { // full class, select each car in it.
                                continue;
                            }
                            carIds[i] = (int) items.get(i).getId();
                        }
                        updateCarTrack();
                    }
                });
            }
        });
    }

    private void updateCarTrack() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                leaderboard.updateTrack(trackId);
                if (carIds != null) {
                    leaderboard.updateCars(carIds);
                }
                updateTable();
            }
        });

        thread.start();
    }

    private void updateTable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TableLayout table = findViewById(R.id.leaderboardTable);
                table.removeAllViews();
                List<R3ELeaderboardEntry> entries = leaderboard.getAllEntries();

                final int carImgWidth = 90;

                TableRow.LayoutParams positionParams = new TableRow.LayoutParams();
                positionParams.weight = 0;

                TableRow.LayoutParams nameParams = new TableRow.LayoutParams();
                nameParams.weight = 2;

                TableRow.LayoutParams laptimeParams = new TableRow.LayoutParams();
                laptimeParams.weight = 1;

                TableRow.LayoutParams carImgParams = new TableRow.LayoutParams();
                carImgParams.weight = 1;
                carImgParams.width = (int) Utils.dpToPx(carImgWidth, MainActivity.this);

                TableRow.LayoutParams[] allParams = new TableRow.LayoutParams[]{positionParams, nameParams, laptimeParams, carImgParams};
                for (TableRow.LayoutParams params : allParams) {
                    params.rightMargin = (int) Utils.dpToPx(10f, MainActivity.this);
                    params.height = (int) Utils.dpToPx(((float)carImgWidth)/2, MainActivity.this);
                }


                int i = 0;
                for (R3ELeaderboardEntry entry : entries) {
                    table.addView(entry.getTableRow(MainActivity.this, ++i, entries.get(0).laptimeSeconds, allParams));
                }
            }
        });
    }
}


