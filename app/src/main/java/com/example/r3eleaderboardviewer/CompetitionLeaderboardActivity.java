package com.example.r3eleaderboardviewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.TableLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Arrays;

public class CompetitionLeaderboardActivity extends ActivityWithNavigation {
    private R3ELeaderboard leaderboard;
    private String competitionId;
    private String competitionName;
    TableLayout table;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.competition_leaderboard);

        Intent i = getIntent();
        competitionId = i.getStringExtra("compId");
        competitionName = i.getStringExtra("compName");

        table = findViewById(R.id.leaderboardTable);

        leaderboard = new R3ELeaderboard(this, table);

        getSupportActionBar().setTitle("Competition - " + competitionName);

        pullToRefresh.setRefreshing(true);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateLeaderboard();
            }
        });

        updateLeaderboard();
    }

    private void updateLeaderboard() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                leaderboard.updateCompetition(competitionId);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leaderboard.updateTable(CompetitionLeaderboardActivity.this, table, new Runnable() {
                            @Override
                            public void run() {
                                pullToRefresh.setRefreshing(false);
                            }
                        });
                    }
                });
            }
        });
        thread.start();
    }
}
