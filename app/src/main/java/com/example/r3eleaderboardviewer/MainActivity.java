package com.example.r3eleaderboardviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TableLayout;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.androidbuts.multispinnerfilter.SingleSpinnerListener;
import com.androidbuts.multispinnerfilter.SingleSpinnerSearch;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Handler mHandler;

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

                Log.d("R3E", "Loaded " + tracks.size() + " tracks and " + cars.size() + " cars.");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        selectTrack = findViewById(R.id.trackSelect);
                        selectTrack.setColorseparation(false);
                        selectTrack.setSearchEnabled(true);
                        selectTrack.setSearchHint("Choose track");

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


                        selectCar = findViewById(R.id.carSelect);
                        selectCar.setSearchEnabled(true);
                        selectCar.setSearchHint("Choose car(s)");
                        selectCar.setEmptyTitle("No cars found");
                        selectCar.setShowSelectAllButton(false);
                        selectCar.setClearText("Clear Selection");

                        selectCar.setItems(cars, new MultiSpinnerListener() {
                            @Override
                            public void onItemsSelected(List<KeyPairBoolData> items) {
                                carIds = new int[items.size()];
                                for (int i = 0; i < items.size(); i++) {
                                    carIds[i] = (int) items.get(i).getId();
                                }
                                updateCarTrack();
                            }
                        });
                    }
                });
            }
        });

        thread.start();
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
                R3ELeaderboardEntry[] entries = leaderboard.getAllEntries();

                int i = 0;
                for (R3ELeaderboardEntry entry : entries) {
                    table.addView(entry.getTableRow(MainActivity.this, ++i, entries[0].laptimeSeconds));
                }
            }
        });
    }
}


