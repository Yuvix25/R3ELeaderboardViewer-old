package com.example.r3eleaderboardviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

public class ActivityWithNavigation extends AppCompatActivity {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected ActionBarDrawerToggle actionBarDrawerToggle;
    protected SwipeRefreshLayout pullToRefresh;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState, int layoutId) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        pullToRefresh = findViewById(R.id.pullToRefresh);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_competitions:
                        if (!(ActivityWithNavigation.this instanceof CompetitionBrowserActivity)) {
                            Intent intent = new Intent(ActivityWithNavigation.this, CompetitionBrowserActivity.class);
                            startActivity(intent);
                        }
                    case R.id.nav_leaderboard:
                        if (!(ActivityWithNavigation.this instanceof MainActivity)) {
                            Intent intent = new Intent(ActivityWithNavigation.this, MainActivity.class);
                            startActivity(intent);
                        }
                }
                return false;
            }
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
