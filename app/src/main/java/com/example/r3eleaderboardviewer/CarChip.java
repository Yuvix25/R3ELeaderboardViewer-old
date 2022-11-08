package com.example.r3eleaderboardviewer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.pchmn.materialchips.model.ChipInterface;

public class CarChip implements ChipInterface {
    private final String label;
    private final String info;
    private final ImageView avatarDrawable;
    private final Object data;

    public CarChip(String label, String info, ImageView icon, Object data) {
        super();
        this.label = label;
        this.info = info;
        this.data = data;
        this.avatarDrawable = icon;
    }

    @Override
    public Object getId() {
        return data;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return avatarDrawable.getDrawable();
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getInfo() {
        return info;
    }
}
