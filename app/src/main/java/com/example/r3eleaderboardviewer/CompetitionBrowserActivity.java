package com.example.r3eleaderboardviewer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Arrays;

public class CompetitionBrowserActivity extends ActivityWithNavigation {
    R3ECompetition[] comps;
    ListView listView;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.competition_browser);

        getSupportActionBar().setTitle("Active Competitions");

        listView = findViewById(R.id.competition_list);
        pullToRefresh.setRefreshing(true);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCompetitionList(0);
            }
        });


        updateCompetitionList(4);
        updateCompetitionList(0);
    }

    private void updateCompetitionList(int cacheDays) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                comps = R3EApis.getActiveR3ECompetitions(cacheDays);
                if (comps == null) {
                    Log.e("R3ELeaderboardViewer", "Failed to get competitions");
                    return;
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CompetitionListAdapter customAdapter = new CompetitionListAdapter(CompetitionBrowserActivity.this, Arrays.asList(comps));
                        listView.setAdapter(customAdapter);

                        pullToRefresh.setRefreshing(false);
                    }
                });
            }
        });
        thread.start();
    }
}
