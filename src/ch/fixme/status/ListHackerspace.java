/*
 * Copyright (C) 2012-2015 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 3, see README
 */

package ch.fixme.status;

import java.util.ArrayList;
import android.util.Log;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SectionIndexer;
import com.woozzu.android.util.StringMatcher;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;


public class ListHackerspace
        extends ArrayAdapter<String>
        implements SectionIndexer {

    private Context mContext;
    private SharedPreferences mPrefs;
    private List<String> mData;
    private LayoutInflater mInflater;
    private ArrayList<String> mUrls;
    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public ListHackerspace(Context context, int resource,
            int textViewResourceId, List<String> objects,
            ArrayList<String> urls) {
        super(context, resource, textViewResourceId, objects);
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mInflater = LayoutInflater.from(mContext);
        mData = objects;
        mUrls = urls;
    }

    @Override
    public View getView(final int pos, View cView, ViewGroup parent) {
        // Views
        final ViewHolder holder;
        if (cView == null) {
            cView = mInflater.inflate(R.layout.hs_list, null);
            holder = new ViewHolder();
            //holder.img = (ImageButton) cView.findViewById(R.id.hs_list_img);
            holder.name = (TextView) cView.findViewById(R.id.hs_list_text);
            cView.setTag(holder);
        } else {
            holder = (ViewHolder) cView.getTag();
        }
        // Set values
        final String hs_name = mData.get(pos);
        holder.name.setText(hs_name);
        //holder.favorited = mPrefs.getBoolean(hs_name, false);
        //if(holder.favorited) {
        //    holder.img.setImageResource(R.drawable.star_on);
        //} else {
        //    holder.img.setImageResource(R.drawable.star_off);
        //}
        //holder.img.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Log.e(Main.TAG, "Clicked="+holder.favorited);
        //        Editor edit = mPrefs.edit();
        //        edit.putBoolean(hs_name, !holder.favorited);
        //        edit.commit();
        //    }
        //});
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mUrls.get(pos);
                Intent clickIntent = new Intent(mContext, Main.class);
                clickIntent.putExtra(Main.STATE_HS, url);
                mContext.startActivity(clickIntent);
            }
        });
        return cView;
    }

    static class ViewHolder {
        Boolean favorited;
        TextView name;
        //ImageButton img;
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

