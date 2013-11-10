/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */
package ch.fixme.status;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class Prefs extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public static final String KEY_CHECK_INTERVAL = "check_interval";
    public static final String DEFAULT_CHECK_INTERVAL = "30"; // minutes
    public static final String KEY_WIDGET_TRANSPARENCY = "widget_transparency";
    public static final boolean DEFAULT_WIDGET_TRANSPARENCY = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen ps = getPreferenceScreen();
        ps.getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(KEY_WIDGET_TRANSPARENCY)
                || key.equals(KEY_CHECK_INTERVAL)) {
            Context ctxt = getApplicationContext();
            AppWidgetManager man = AppWidgetManager.getInstance(ctxt);
            int[] ids = man.getAppWidgetIds(new ComponentName(ctxt,
                    Widget.class));
            Intent ui = new Intent();
            ui.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            ui.putExtra(Widget.WIDGET_IDS, ids);
            ui.putExtra(Widget.WIDGET_FORCE, true);
            ctxt.sendBroadcast(ui);
        }
    }

}
