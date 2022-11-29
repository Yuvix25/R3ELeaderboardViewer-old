package com.example.r3eleaderboardviewer;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gr.escsoft.michaelprimez.searchablespinner.interfaces.ISpinnerSelectedView;

/**
 * Created by michael on 1/8/17.
 */

public class SimpleArrayListAdapter extends ArrayAdapter<String> implements Filterable, ISpinnerSelectedView {

    private final Context mContext;
    private final ArrayList<String> mBackupStrings;
    private ArrayList<String> mStrings;
    private final ArrayList<ImageView> mIcons;
    private final StringFilter mStringFilter = new StringFilter();

    public SimpleArrayListAdapter(Context context, ArrayList<String> strings, ArrayList<ImageView> icons) {
        super(context, R.layout.view_list_item);
        mContext = context;
        mStrings = strings;
        mBackupStrings = strings;
        mIcons = icons;
    }

    @Override
    public int getCount() {
        return mStrings == null ? 0 : mStrings.size() + 1;
    }

    @Override
    public String getItem(int position) {
        if (mStrings != null && position > 0)
            return mStrings.get(position - 1);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        if (mStrings == null && position > 0)
            return mStrings.get(position).hashCode();
        else
            return -1;
    }

    public int getOriginalIndex(int index) { // regular counting from 0
        int res = this.mStringFilter.getOriginalPosition(index);
        if (res == -1) {
            return index;
        } else {
            return res;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (position == 0) {
            view = getNoSelectionView();
        } else {
            view = View.inflate(mContext, R.layout.view_list_item, null);
            ImageView icons = (ImageView) view.findViewById(R.id.ImgVw_Icons);
            TextView displayName = (TextView) view.findViewById(R.id.TxtVw_DisplayName);
            icons.setImageDrawable(mIcons.get(position - 1).getDrawable());
            displayName.setText(mStrings.get(position-1));
        }
        return view;
    }

    @Override
    public View getSelectedView(int position) {
        View view;
        if (position == 0) {
            view = getNoSelectionView();
        } else {
            view = View.inflate(mContext, R.layout.view_list_item, null);
            ImageView icons = (ImageView) view.findViewById(R.id.ImgVw_Icons);
            TextView displayName = (TextView) view.findViewById(R.id.TxtVw_DisplayName);
            icons.setImageDrawable(mIcons.get(position-1).getDrawable());
            displayName.setText(mStrings.get(position-1));
        }
        return view;
    }

    @Override
    public View getNoSelectionView() {
        return View.inflate(mContext, R.layout.view_list_no_selection_item, null);
    }

    @Override
    public Filter getFilter() {
        return mStringFilter;
    }

    public class StringFilter extends Filter {

        private Map<Integer, Integer> mPositions;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults filterResults = new FilterResults();
            mPositions = new HashMap<>();
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = mBackupStrings.size();
                filterResults.values = mBackupStrings;
                return filterResults;
            }
            final ArrayList<String> filterStrings = new ArrayList<>();
            int i = 0;
            for (String text : mBackupStrings) {
                if (text.toLowerCase().contains(constraint)) {
                    filterStrings.add(text);
                    mPositions.put(filterStrings.size() - 1, i);
                }
                i++;
            }
            filterResults.count = filterStrings.size();
            filterResults.values = filterStrings;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mStrings = (ArrayList) results.values;
            notifyDataSetChanged();
        }

        protected int getOriginalPosition(int position) {
            if (mPositions != null && mPositions.containsKey(position))
                return mPositions.get(position);
            else
                return -1;
        }
    }

    private class ItemView {
        public ImageView mImageView;
        public TextView mTextView;
    }

    public enum ItemViewType {
        ITEM, NO_SELECTION_ITEM
    }
}