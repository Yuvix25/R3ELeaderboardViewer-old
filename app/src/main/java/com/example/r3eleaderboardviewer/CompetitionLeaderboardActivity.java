package com.example.r3eleaderboardviewer;

import android.content.Intent;
import android.widget.TableLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Arrays;

public class CompetitionLeaderboardActivity extends ActivityWithNavigation {
    private final R3ELeaderboard leaderboard = new R3ELeaderboard();
    private String competitionId;
    private String competitionName;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.competition_leaderboard);

        Intent i = getIntent();
        competitionId = i.getStringExtra("compId");
        competitionName = i.getStringExtra("compName");

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
                        TableLayout table = findViewById(R.id.leaderboardTable);
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
