package com.example.r3eleaderboardviewer;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

class CompetitionListAdapter implements ListAdapter {
    List<R3ECompetition> arrayList;
    Context context;
    public CompetitionListAdapter(Context context, List<R3ECompetition> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    @Override
    public boolean isEnabled(int position) {
        return true;
    }
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return position;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        R3ECompetition comp = arrayList.get(position);
        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.competition_list_row, null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.openCompetition(context, comp);
                }
            });
            TextView title = convertView.findViewById(R.id.competition_list_name);
            TextView endTime = convertView.findViewById(R.id.competition_list_end_time);
            ImageView image = convertView.findViewById(R.id.competition_list_image);
            
            title.setText(comp.name);
            comp.setCompetitionTimer(endTime);
            Utils.loadImageFromUrl(comp.bannerImage.toString(), context, new Utils.ParameterizedRunnable() {
                @Override
                protected void run(Object... params) {
                    image.setImageBitmap((android.graphics.Bitmap) params[2]);
                }
            });
        }
        return convertView;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getViewTypeCount() {
        return arrayList.size();
    }
    @Override
    public boolean isEmpty() {
        return false;
    }
}
