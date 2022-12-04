package com.example.r3eleaderboardviewer;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;


import com.jakewharton.threetenabp.AndroidThreeTen;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gr.escsoft.michaelprimez.searchablespinner.SearchableSpinner;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.OnItemSelectedListener;


public class MainActivity extends ActivityWithNavigation {
    private R3ELeaderboard leaderboard;

    private SearchableSpinner selectTrack;
    private ChipsInput selectCar;
    private SwipeRefreshLayout pullToRefresh;
    TableLayout table;

    private Comparator<ChipInterface> getCarListComparator(List<CarChip> carList) {
        return new Comparator<ChipInterface>() {
            @Override
            public int compare(ChipInterface c1, ChipInterface c2) {
                R3ECarOrClass car1 = (R3ECarOrClass) c1.getId();
                R3ECarOrClass car2 = (R3ECarOrClass) c2.getId();

                return carList.indexOf(car1) - carList.indexOf(car2);
            }
        };
    }


    private List<String> carIds = new ArrayList<>();
    private int trackId = -1;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);

        Utils.setContext(this);
        AndroidThreeTen.init(this);

        getSupportActionBar().setTitle("Leaderboards");

        table = findViewById(R.id.leaderboardTable);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCarTrack(true);
            }
        });

        selectTrack = findViewById(R.id.trackSelect);


        selectCar = findViewById(R.id.carSelect);
        selectCar.setFilterableList(new ArrayList<>());

        updateSelectItems(new ArrayList<>(), new ArrayList<>());


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                leaderboard = new R3ELeaderboard(MainActivity.this, table);

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
                            return c1.getCarName().compareTo(c2.getCarName());
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

                for (R3EClassGroup group : R3EData.classGroups.values()) {
                    cars.add(0, new R3ECarOrClass(group));
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
                        Log.d("R3E", position + " " + id);
                        if (position == 0) {
                            trackId = -1;
                        } else {
                            R3ETrackLayout layout = trackLayouts.get(adapter.getOriginalIndex(position - 1));
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
                    String icon = car.type == 2 ? null : car.getIcon();
                    Integer drawableIconId = car.type == 2 ? Integer.parseInt(car.getIcon()) : null;
                    String label;
                    String info;
                    int padding = 25;
                    if (car.type == 0) {
                        label = car.getCarName();
                        info = car.getClassName();
                    } else if (car.type == 1) {
                        label = car.getClassName();
                        info = car.getCars().size() + " cars";
                        padding = 0;
                    } else {
                        label = car.getClassName();
                        info = car.getClasses().size() + " classes";
                        padding = 0;
                    }


                    Utils.ParameterizedRunnable runnable = new Utils.ParameterizedRunnable() { // used to be a callback, no real need for it to be wrapped
                        @Override
                        protected void run(Object... params) {
                            ImageView image = (ImageView) params[0];
                            boolean success = (boolean) params[1];

                            receivedIcons[0]++;

                            Log.d("R3E", "Received icon " + receivedIcons[0] + "/" + cars.size() + " - " + (icon == null ? drawableIconId : icon) + " - " + success);

                            if (success) {
                                carList.add(new CarChip(label, info, image, car));
                            }

                            if (receivedIcons[0] == cars.size()) {
                                selectCar.setFilterableList(carList, getCarListComparator(carList));
                                selectCar.requestLayout();
                                selectCar.addChipsListener(new ChipsInput.ChipsListener() {
                                    private String[] getId(Object data) {
                                        R3ECarOrClass carObj = (R3ECarOrClass) data;
                                        String[] carId;
                                        if  (carObj.type == 0 ){
                                            carId = carObj.getIds();
                                        } else {
                                            carId = Arrays.stream(carObj.getIds()).map(id -> "class-" + id).toArray(String[]::new);
                                        }
                                        return carId;
                                    }

                                    @Override
                                    public void onChipAdded(ChipInterface chip, int newSize) {
                                        String[] carId = getId(chip.getId());
                                        carIds.addAll(Arrays.asList(carId));
                                        updateCarTrack();
                                    }

                                    @Override
                                    public void onChipRemoved(ChipInterface chip, int newSize) {
                                        String[] carId = getId(chip.getId());
                                        carIds.removeAll(Arrays.asList(carId));
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

                    ImageView img;
                    if (icon != null) {
                        img = Utils.loadImageFromUrl(icon, MainActivity.this, padding, new Utils.ParameterizedRunnable() {
                            @Override
                            protected void run(Object... params) {
                                selectCar.requestLayout();
                            }
                        });
                    } else {
                        img = new ImageView(MainActivity.this);
                        img.setImageResource(drawableIconId);
                    }
                    runnable.run(img, true);
                }
            }
        });
    }

    private void updateCarTrack(boolean force) {
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
                updateTable(null);
            }
        });

        thread.start();
    }

    private void updateCarTrack() {
        updateCarTrack(false);
    }

    private void updateTable(Runnable callback) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                leaderboard.updateTable(MainActivity.this, table, callback);
                hideLoadingCircle();
            }
        });
    }
}


