package com.example.r3eleaderboardviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;


import com.jakewharton.threetenabp.AndroidThreeTen;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gr.escsoft.michaelprimez.searchablespinner.SearchableSpinner;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.OnItemSelectedListener;


public class MainActivity extends AppCompatActivity {
    private R3ELeaderboard leaderboard;

    private ProgressBar loadingCircle;
    private SearchableSpinner selectTrack;
    private ChipsInput selectCar;
    private SwipeRefreshLayout pullToRefresh;

    private List<String> carIds = new ArrayList<>();
    private int trackId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.setContext(this);

        AndroidThreeTen.init(this);


        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCarTrack(true, new Runnable() {
                    @Override
                    public void run() {
                        pullToRefresh.setRefreshing(false);
                    }
                });
            }
        });

//        loadingCircle = findViewById(R.id.loadingCircle);
        selectTrack = findViewById(R.id.trackSelect);


        selectCar = (ChipsInput) findViewById(R.id.carSelect);
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
                List<R3ETrackLayout> tracks = R3EData.getTrackLayoutsForChips();
                List<R3ECarOrClass> cars = R3EData.getCarsForChips();

                updateSelectItems(tracks, cars);

                Log.d("R3E", "Loaded " + tracks.size() + " tracks and " + cars.size() + " cars.");
            }
        });

        thread.start();
    }


    private void showLoadingCircle() {
        if (trackId == -1 || carIds.size() == 0) {
            return;
        }

        pullToRefresh.setRefreshing(true);
    }

    private void hideLoadingCircle() {
        pullToRefresh.setRefreshing(false);
    }

    private void updateSelectItems(List<R3ETrackLayout> trackLayouts, List<R3ECarOrClass> cars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(trackLayouts, new Comparator<R3ETrackLayout>() {
                    @Override
                    public int compare(R3ETrackLayout t1, R3ETrackLayout t2) {

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


                ArrayList<String> trackNames = new ArrayList<>();
                for (R3ETrackLayout trackLayout : trackLayouts) {
                    trackNames.add(trackLayout.getDisplayName());
                }

                ArrayList<ImageView> icons = new ArrayList<>();
                for (R3ETrackLayout trackLayout : trackLayouts) {
                    ImageView icon = Utils.loadImageFromUrl(trackLayout.icon.toString(), MainActivity.this, new Utils.ParameterizedRunnable() {
                        @Override
                        protected void run(Object... params) {
                            ImageView icon = (ImageView) params[0];
                            Log.d("R3E", "Loaded icon for " + icon.getTag());
                        }
                    });
                    icon.setTag(trackLayout.getDisplayName());
                    icons.add(icon);
                }
                SimpleArrayListAdapter adapter = new SimpleArrayListAdapter(MainActivity.this, trackNames, icons);

                selectTrack.setAdapter(adapter);
                selectTrack.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(View view, int position, long id) {
                        if (position == 0) {
                            trackId = -1;
                        } else {
                            R3ETrackLayout layout = trackLayouts.get(position-1);
                            Log.d("R3E", "Selected track " + layout.getDisplayName());
                            trackId = Integer.parseInt(layout.id);
                        }
                        updateCarTrack();
                    }

                    @Override
                    public void onNothingSelected() {
                        trackId = -1;
                        updateCarTrack();
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
                        info = car.getCars().size() + " cars";
                        padding = 0;
                    } else {
                        label = car.getName();
                        info = car.getClassName();
                    }

                    Utils.ParameterizedRunnable runnable = new Utils.ParameterizedRunnable() { // used to be a callback, no real need for it to be wrapped
                        @Override
                        protected void run(Object... params) {
                            ImageView image = (ImageView) params[0];
                            boolean success = (boolean) params[1];

                            receivedIcons[0]++;
                            Log.d("R3E", "Received icon " + receivedIcons[0] + "/" + cars.size() + " - " + icon + " - " + success);

                            if (success) {
                                carList.add(new CarChip(label, info, image, car));
                            }

                            if (receivedIcons[0] == cars.size()) {
                                selectCar.setFilterableList(carList);
                                selectCar.requestLayout();
                                selectCar.addChipsListener(new ChipsInput.ChipsListener() {
                                    private String getId(Object data) {
                                        R3ECarOrClass carObj = (R3ECarOrClass) data;
                                        String carId;
                                        if (carObj.isClass) {
                                            carId = "class-" + carObj.getId();
                                        } else {
                                            carId = carObj.getId();
                                        }
                                        return carId;
                                    }

                                    @Override
                                    public void onChipAdded(ChipInterface chip, int newSize) {
                                        String carId = getId(chip.getId());
                                        carIds.add(carId);
                                        updateCarTrack();
                                    }

                                    @Override
                                    public void onChipRemoved(ChipInterface chip, int newSize) {
                                        String carId = getId(chip.getId());
                                        carIds.remove(carId);
                                        updateCarTrack();
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence text) {
                                    }
                                });
                                Log.d("R3E", "Done loading and processing all cars.");
                            }
                        }
                    };

                    ImageView img = Utils.loadImageFromUrl(icon, MainActivity.this, padding, new Utils.ParameterizedRunnable() {
                        @Override
                        protected void run(Object... params) {
                            selectCar.requestLayout();
                        }
                    });
                    runnable.run(img, true);
                }
            }
        });
    }

    private void updateCarTrack(boolean force, Runnable callback) {
        showLoadingCircle();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (force) {
                    leaderboard.clear();
                }

                leaderboard.updateTrack(trackId);
                if (carIds != null) {
                    leaderboard.updateCars(carIds);
                }
                updateTable(callback);
            }
        });

        thread.start();
    }

    public void updateCarTrack() {
        updateCarTrack(false, null);
    }

    private void updateTable(Runnable callback) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TableLayout table = findViewById(R.id.leaderboardTable);
                table.removeAllViews();
                List<R3ELeaderboardEntry> entries = leaderboard.getAllEntries();

                final int carImgWidth = 75;

                TableRow.LayoutParams positionParams = new TableRow.LayoutParams();
                positionParams.weight = 0;

                TableRow.LayoutParams nameParams = new TableRow.LayoutParams();
                nameParams.weight = 2;

                TableRow.LayoutParams laptimeParams = new TableRow.LayoutParams();
                laptimeParams.weight = 0;

                TableRow.LayoutParams carImgParams = new TableRow.LayoutParams();
                carImgParams.weight = 0;
                carImgParams.width = (int) Utils.dpToPx(carImgWidth, MainActivity.this);

                TableRow.LayoutParams[] allParams = new TableRow.LayoutParams[]{positionParams, nameParams, laptimeParams, carImgParams};
                for (TableRow.LayoutParams params : allParams) {
                    params.rightMargin = (int) Utils.dpToPx(15f, MainActivity.this);
                    params.height = TableRow.LayoutParams.MATCH_PARENT;
                }


                int i = 0;
                for (R3ELeaderboardEntry entry : entries) {
                    table.addView(entry.getTableRow(MainActivity.this, ++i, entries.get(0).laptimeSeconds, allParams));
                }


                hideLoadingCircle();
                if (callback != null) {
                    callback.run();
                }
            }
        });
    }
}


