package com.example.r3eleaderboardviewer;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSpinner;

public class R3ETrackSelector extends AppCompatSpinner {
    public R3ETrackSelector(Context context) {
        super(context);

//        this.setAdapter(new SimpleImageArrayAdapter(context, R3ETrackLayout.getTrackLayouts()));
    }

    public R3ETrackSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
