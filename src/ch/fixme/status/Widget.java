/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package ch.fixme.status;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {

    public void onUpdate(Context ctxt, AppWidgetManager manager,
            int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent(ctxt, UpdateService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            ctxt.startService(intent);
        }
        super.onUpdate(ctxt, manager, appWidgetIds);
    }

    private static class GetImage extends AsyncTask<String, Void, byte[]> {

        private int mId;
        private Context mCtxt;

        public GetImage(Context ctxt, int id) {
            mCtxt = ctxt;
            mId = id;
        }

        @Override
        protected byte[] doInBackground(String... url) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                new Net(url[0], os);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return os.toByteArray();
        }

        @Override
        protected void onPostExecute(byte[] result) {
            AppWidgetManager manager = AppWidgetManager.getInstance(mCtxt);
            updateWidget(mCtxt, mId, manager,
                    BitmapFactory.decodeByteArray(result, 0, result.length));
        }

    }

    protected static void updateWidget(final Context ctxt, int widgetId,
            AppWidgetManager manager, Bitmap bitmap) {
        RemoteViews views = new RemoteViews(ctxt.getPackageName(),
                R.layout.widget);
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.widget_image, bitmap);
        } else {
            views.setImageViewResource(R.id.widget_image,
                    android.R.drawable.ic_popup_sync);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(ctxt, 0,
                new Intent(ctxt, Main.class), 0);
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
            ByteArrayOutputStream spaceOs = new ByteArrayOutputStream();
            try {
                new Net(url[0], spaceOs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return spaceOs.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject api = new JSONObject(result);
                // Mandatory fields
                String status = Main.API_ICON_CLOSED;
                if (api.getBoolean(Main.API_STATUS)) {
                    status = Main.API_ICON_OPEN;
                }
                // Status icon or space icon
                if (!api.isNull(Main.API_ICON)) {
                    JSONObject status_icon = api.getJSONObject(Main.API_ICON);
                    if (!status_icon.isNull(status)) {
                        new GetImage(mCtxt, mId).execute(status_icon
                                .getString(status));
                    }
                } else {
                    new GetImage(mCtxt, mId).execute(api
                            .getString(Main.API_LOGO));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public static class UpdateService extends IntentService {

        public UpdateService() {
            super("MyHackerspaceWidgetService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.i(Main.TAG, "UpdateService started");
            int widgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            new GetApiTask(getApplicationContext(), widgetId)
                    .execute(PreferenceManager.getDefaultSharedPreferences(
                            UpdateService.this).getString(Main.API_KEY,
                            Main.API_DEFAULT));
            stopSelf();
        }
    }
}
