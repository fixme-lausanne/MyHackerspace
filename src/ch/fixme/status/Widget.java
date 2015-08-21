/*
 * Copyright (C) 2012-2013 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 3, see README
 */

package ch.fixme.status;

import java.util.HashMap;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Widget extends AppWidgetProvider {

    static final String WIDGET_IDS = "widget_ids";
    static final String WIDGET_FORCE = "widget_force";

    public void onReceive(Context ctxt, Intent intent) {
        String action = intent.getAction();
        if ((intent.hasExtra(WIDGET_IDS)
                && AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) ||
                Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Get all widgets ids
            AppWidgetManager manager = AppWidgetManager.getInstance(ctxt);
            int[] ids = manager.getAppWidgetIds(new ComponentName(ctxt, Widget.class));
            // Set prefs
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(ctxt);
            Editor edit = prefs.edit();
            final int N = ids.length;
            for (int i = 0; i < N; i++) {
                final int widgetId = ids[i];
                // Set force to false by default
                edit.putBoolean(Main.PREF_FORCE_WIDGET + widgetId,
                        intent.getBooleanExtra(Widget.WIDGET_FORCE, false));
                // Start alarm
                Intent newIntent = getIntent(ctxt, widgetId);
                setAlarm(ctxt, newIntent, widgetId);
            }
            edit.commit();
        } else {
            super.onReceive(ctxt, intent);
        }
    }

    @Override
    public void onDeleted(Context ctxt, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int widgetId = appWidgetIds[i];
            // Remove widget alarm
            PendingIntent pi = PendingIntent.getService(ctxt, widgetId,
                    getIntent(ctxt, widgetId), 0);
            AlarmManager am = (AlarmManager) ctxt
                    .getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);

            // remove preference
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(ctxt);
            Editor edit = prefs.edit();
            edit.remove(Main.PREF_API_URL_WIDGET + widgetId);
            edit.remove(Main.PREF_LAST_WIDGET + widgetId);
            edit.remove(Main.PREF_FORCE_WIDGET + widgetId);
            edit.commit();

            Log.i(Main.TAG, "Remove widget alarm for id=" + widgetId);
        }
    }

    protected static Intent getIntent(Context ctxt, int widgetId) {
        Intent i = new Intent(ctxt, UpdateService.class);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return i;
    }

    protected static void setAlarm(Context ctxt, Intent i, int widgetId) {
        setAlarm(ctxt, i, widgetId, 200);
    }

    protected static void setAlarm(Context ctxt, Intent i, int widgetId,
            int delay) {
        // Get interval
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(ctxt);
        long update_interval = Long.parseLong(prefs.getString(
                Prefs.KEY_CHECK_INTERVAL, Prefs.DEFAULT_CHECK_INTERVAL)) * 60L * 1000L;
        // Set alarm
        AlarmManager am = (AlarmManager) ctxt
                .getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getService(ctxt, widgetId, i, 0);
        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay, update_interval, pi);
        // Log.i(Main.TAG, "start notification every " + update_interval / 1000
        // + "s");
    }

    private static class GetImage extends AsyncTask<String, Void, Bitmap> {

        private int mId;
        private Context mCtxt;
        private String mText;

        public GetImage(Context ctxt, int id, String text) {
            mCtxt = ctxt;
            mId = id;
            mText = text;
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            try {
                return new Net(url[0], mCtxt).getBitmap();
            } catch (Throwable e) {
                e.printStackTrace();
                String msg = e.getMessage();
                printMessage(mCtxt, msg);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            AppWidgetManager manager = AppWidgetManager.getInstance(mCtxt);
            updateWidget(mCtxt, mId, manager, result, mText);
        }

    }

    protected static void updateWidget(final Context ctxt, int widgetId,
            AppWidgetManager manager, Bitmap bitmap, String text) {
        RemoteViews views = new RemoteViews(ctxt.getPackageName(),
                R.layout.widget);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(ctxt);
        Editor edit = prefs.edit();
        if (prefs.getBoolean(Prefs.KEY_WIDGET_TRANSPARENCY,
                Prefs.DEFAULT_WIDGET_TRANSPARENCY)) {
            views.setInt(R.id.widget_image, "setBackgroundResource", 0);
        } else {
            views.setInt(R.id.widget_image, "setBackgroundResource",
                    android.R.drawable.btn_default_small);
        }
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.widget_image, bitmap);
            edit.putBoolean(Main.PREF_FORCE_WIDGET + widgetId, false);
        } else {
            // Something went wrong
            views.setImageViewResource(R.id.widget_image,
                    android.R.drawable.ic_popup_sync);
            edit.putBoolean(Main.PREF_FORCE_WIDGET + widgetId, true);
        }
        if (text != null) {
            views.setTextViewText(R.id.widget_status, text);
            views.setViewVisibility(R.id.widget_status, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_status, View.GONE);
        }
        edit.commit();
        Intent clickIntent = new Intent(ctxt, Main.class);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctxt, widgetId,
                clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_image, pendingIntent);
        manager.updateAppWidget(widgetId, views);
    }

    private static class GetApiTask extends AsyncTask<String, Void, String> {

        private int mId;
        private Context mCtxt;

        public GetApiTask(Context ctxt, int id) {
            mCtxt = ctxt;
            mId = id;
        }

        @Override
        protected String doInBackground(String... url) {
            try {
                return new Net(url[0], false, mCtxt).getString();
            } catch (Throwable e) {
                e.printStackTrace();
                String msg = e.getMessage();
		Log.e(Main.TAG, msg);
            }
            return "";
        }

        @Override
        protected void onCancelled() {
            // Set alarm 5 seconds in the future
            Log.i(Main.TAG, "Set alarm in 1 seconds");
            Intent intent = getIntent(mCtxt, mId);
            setAlarm(mCtxt, intent, mId, 1000);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mCtxt);

                HashMap<String, Object> api = new ParseGeneric(result)
                        .getData();
                boolean statusBool = (Boolean) api.get(ParseGeneric.API_STATUS);
                // Update only if different than last status or not forced
                if (prefs.contains(Main.PREF_LAST_WIDGET + mId)
                        && prefs.getBoolean(Main.PREF_LAST_WIDGET + mId, false) == statusBool
                        && !prefs.getBoolean(Main.PREF_FORCE_WIDGET + mId,
                                false)) {
                    // Log.i(Main.TAG, "Nothing to update");
                    return;
                }

                // Mandatory fields
                String status = ParseGeneric.API_ICON
                        + ParseGeneric.API_ICON_CLOSED;
                if (statusBool) {
                    status = ParseGeneric.API_ICON + ParseGeneric.API_ICON_OPEN;
                }
                Editor edit = prefs.edit();
                edit.putBoolean(Main.PREF_LAST_WIDGET + mId, statusBool);
                edit.commit();

                String status_text = null;
                if (prefs.getBoolean(Prefs.KEY_WIDGET_TEXT,
                        Prefs.DEFAULT_WIDGET_TEXT)) {
                    if (api.containsKey(ParseGeneric.API_STATUS_TXT)) {
                        status_text = (String) api
                                .get(ParseGeneric.API_STATUS_TXT);
                    } else {
                        status_text = statusBool ? Main.OPEN : Main.CLOSED;
                    }
                }

                // Status icon or space icon
                if (api.containsKey(ParseGeneric.API_ICON
                        + ParseGeneric.API_ICON_OPEN)
                        && api.containsKey(ParseGeneric.API_ICON
                                + ParseGeneric.API_ICON_CLOSED)) {
                    new GetImage(mCtxt, mId, status_text).execute((String) api
                            .get(status));
                } else {
                    new GetImage(mCtxt, mId, status_text).execute((String) api
                            .get(ParseGeneric.API_LOGO));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                String msg = e.getMessage();
                printMessage(mCtxt, msg);
            }
        }
    }

    public static class UpdateService extends IntentService {

        public UpdateService() {
            super("MyHackerspaceWidgetService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            final Context ctxt = UpdateService.this;
            int widgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(ctxt);
            if (Main.checkNetwork(ctxt) && prefs.contains(Main.PREF_API_URL_WIDGET + widgetId)) {
                String url = prefs.getString(Main.PREF_API_URL_WIDGET
                        + widgetId, ParseGeneric.API_DEFAULT);
                Log.i(Main.TAG, "Update widgetid " + widgetId + " with url "
                        + url);
                new GetApiTask(ctxt, widgetId).execute(url);
            }
            stopSelf();
        }
    }

    public static void UpdateAllWidgets(final Context ctxt, boolean force) {
        AppWidgetManager man = AppWidgetManager.getInstance(ctxt);
        int[] ids = man.getAppWidgetIds(new ComponentName(ctxt, Widget.class));
        Intent ui = new Intent();
        ui.setClass(ctxt, Widget.class);
        ui.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        ui.putExtra(Widget.WIDGET_IDS, ids);
        ui.putExtra(Widget.WIDGET_FORCE, force);
        ctxt.sendBroadcast(ui);
        // Log.i(Main.TAG, "UpdateAllWidgets force=" + force);
    }

    private static void printMessage(final Context ctxt, String msg){
        if(msg == null){
            return;
        }
        Log.e(Main.TAG, msg);
        Toast.makeText(ctxt, msg, Toast.LENGTH_SHORT).show();
    }

}
