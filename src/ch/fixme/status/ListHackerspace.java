/*
 * Copyright (C) 2012-2015 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 3, see README
 */

package ch.fixme.status;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SectionIndexer;
import com.woozzu.android.util.StringMatcher;

public class ListHackerspace
        extends ArrayAdapter<String>
        implements SectionIndexer {

    private List<String> mData;
    private LayoutInflater mInflater;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public ListHackerspace(Context context, int resource,
            int textViewResourceId, List<String> objects) {
        super(context, resource, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        mData = objects;
    }

    @Override
    public View getView(final int pos, View cView, ViewGroup parent) {
        // Views
        final ViewHolder holder;
        if (cView == null) {
            cView = mInflater.inflate(R.layout.hs_list, null);
            holder = new ViewHolder();
            holder.img = (ImageButton) cView.findViewById(R.id.hs_list_img);
            holder.name = (TextView) cView.findViewById(R.id.hs_list_text);
            cView.setTag(holder);
        } else {
            holder = (ViewHolder) cView.getTag();
        }
        // Set values
        holder.img.setOnClickListener(null);
        holder.name.setText(mData.get(pos));
        return cView;
    }

    static class ViewHolder {
        TextView name;
        ImageButton img;
    }

    @Override
    public int getPositionForSection(int section) {
        // If there is no item for current section, previous section will be
        // selected
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(
                                String.valueOf(getItem(j).charAt(0)),
                                String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(
                            String.valueOf(getItem(j).charAt(0)),
                            String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }


}

