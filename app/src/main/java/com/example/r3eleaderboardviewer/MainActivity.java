package com.example.r3eleaderboardviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.SingleSpinnerListener;
import com.androidbuts.multispinnerfilter.SingleSpinnerSearch;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    R3ELeaderboard leaderboard;

    SingleSpinnerSearch selectTrack;
    ChipsInput selectCar;

    List<String> carIds = new ArrayList<>();
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


        selectCar = (ChipsInput) findViewById(R.id.carSelector);
        selectCar.setFilterableList(new ArrayList<>());


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
                List<KeyPairBoolData> tracks = R3EData.getTrackLayoutsForChips();
                List<R3ECarOrClass> cars = R3EData.getCarsForChips();

                updateSelectItems(tracks, cars);

                Log.d("R3E", "Loaded " + tracks.size() + " tracks and " + cars.size() + " cars.");
            }
        });

        thread.start();
    }

    private void updateSelectItems(List<KeyPairBoolData> tracks, List<R3ECarOrClass> cars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(tracks, new Comparator<KeyPairBoolData>() {
                    @Override
                    public int compare(KeyPairBoolData o1, KeyPairBoolData o2) {
                        R3ETrackLayout t1 = (R3ETrackLayout) o1.getObject();
                        R3ETrackLayout t2 = (R3ETrackLayout) o2.getObject();

                        if (t1.track.name.equals(t2.track.name)) {
                            return t1.name.compareTo(t2.name);
                        } else {
                            return t1.track.name.compareTo(t2.track.name);
                        }
                    }
                });

                Collections.sort(cars, new Comparator<R3ECarOrClass>() {
                    @Override
                    public int compare(R3ECarOrClass c1, R3ECarOrClass c2) {
                        if (c1.getClassName().equals(c2.getClassName())) {
                            return c1.getName().compareTo(c2.getName());
                        } else {
                            return c1.getClassName().compareTo(c2.getClassName());
                        }
                    }
                });

                for (int i = cars.size() - 1; i >= 0; i--) {
                    R3ECarOrClass car = cars.get(i);
                    if (i < cars.size() - 1) {
                        R3ECarOrClass prevCar = cars.get(i + 1);
                        if (!car.getClassName().equals(prevCar.getClassName())) {
                            cars.add(i + 1, new R3ECarOrClass(prevCar.getCarClass()));
                        }
                    }
                }
                if (cars.size() > 0) {
                    cars.add(0, new R3ECarOrClass(cars.get(0).getCarClass()));
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

                List<CarChip> carList = new ArrayList<>();

                final int[] receivedIcons = {0};
                for (R3ECarOrClass car : cars) {
                    String icon = car.getIcon().toString();
                    String label;
                    String info;
                    int padding = 25;
                    if (car.isClass) {
                        label = car.getClassName();
                        info = "";
                        padding = 0;
                    } else {
                        label = car.getName();
                        info = car.getClassName();
                    }

                    Utils.loadImageFromUrl(icon, MainActivity.this, padding, new Utils.ParameterizedRunnable() {
                        @Override
                        protected void run(Object... params) {
                            ImageView image = (ImageView) params[0];
                            boolean success = (boolean) params[1];

                            receivedIcons[0]++;
                            Log.d("R3E", "Received icon " + receivedIcons[0] + "/" + cars.size() + " - " + icon + " - " + success);

                            if (success) {
                                carList.add(new CarChip(label, info, image.getDrawable(), car));
                            }

                            if (receivedIcons[0] == cars.size()) {
                                selectCar.setFilterableList(carList);
                                selectCar.requestLayout();
                                selectCar.addChipsListener(new ChipsInput.ChipsListener() {
                                    @Override
                                    public void onChipAdded(ChipInterface chip, int newSize) {
                                        R3ECarOrClass carObj = (R3ECarOrClass) chip.getId();
                                        String carId;
                                        if (carObj.isClass) {
                                            carId = "class-" + carObj.getId();
                                        } else {
                                            carId = carObj.getId();
                                        }
                                        carIds.add(carId);
                                        updateCarTrack();
                                    }

                                    @Override
                                    public void onChipRemoved(ChipInterface chip, int newSize) {
                                        updateCarTrack();
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence text) {
                                    }
                                });
                                Log.d("R3E", "Done loading and processing all cars.");
                            }
                        }
                    });
                }
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


