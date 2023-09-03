/*
 * Copyright (C) 2012-2017 Aubort Jean-Baptiste (Rorist)
 * Copyright (C) 2020-2023 Danilo Bargen (dbrgn)
 * Licensed under GNU's GPL 3, see README
 */
package io.spaceapi.community.myhackerspace;

import static android.view.ViewGroup.LayoutParams.*;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class Prefs extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public static final String KEY_API_ENDPOINT = "api_endpoint";
    public static final String DEFAULT_API_ENDPOINT = "https://raw.githubusercontent.com/SpaceApi/directory/master/directory.json";

    public static final String KEY_API_URL = "apiurl";

    public static final String KEY_CHECK_INTERVAL = "check_interval";
    public static final String DEFAULT_CHECK_INTERVAL = "30"; // minutes

    public static final String KEY_WIDGET_TRANSPARENCY = "widget_transparency";
    public static final boolean DEFAULT_WIDGET_TRANSPARENCY = false;

    public static final String KEY_WIDGET_TEXT = "widget_text";
    public static final boolean DEFAULT_WIDGET_TEXT = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        this.getListView().addFooterView(AboutLayout.create(this)); // tried addContentView
        PreferenceScreen ps = getPreferenceScreen();
        ps.getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(KEY_WIDGET_TRANSPARENCY) || key.equals(KEY_WIDGET_TEXT)
                || key.equals(KEY_CHECK_INTERVAL)) {
            Widget.UpdateAllWidgets(getApplicationContext(), true);
        }
    }

}
