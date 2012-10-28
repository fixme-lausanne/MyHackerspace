/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package ch.fixme.status;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.lang.IllegalArgumentException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class Main extends Activity {

    // API: http://hackerspaces.nl/spaceapi/

    protected static String TAG = "MyHackerspace";
    protected static final String PKG = "ch.fixme.status";
    protected static final String OPEN = "Open";
    protected static final String CLOSED = "Closed";
    protected static final String PREF_API_URL_WIDGET = "api_url_widget_";
    protected static final String PREF_INIT_WIDGET = "init_widget_";
    protected static final String PREF_LAST_WIDGET = "last_widget_";
    private static final String PREF_API_URL = "apiurl";
    private static final int DIALOG_LOADING = 0;
    private static final int DIALOG_ERROR = 1;
    private static final String STATE_HS = "hs";
    private static final String STATE_DIR = "dir";
    private static final String TWITTER = "https://twitter.com/#!/";
    private static final String MAP_SEARCH = "geo:0,0?q=";
    private static final String MAP_COORD = "geo:%s,%s?z=23&";

    private static final String API_DIRECTORY = "http://openspace.slopjong.de/directory.json";
    private static final String API_NAME = "space";
    private static final String API_URL = "url";
    private static final String API_STATUS_TXT = "status";
    private static final String API_DURATION = "duration";
    private static final String API_ADDRESS = "address";
    private static final String API_LON = "lon";
    private static final String API_LAT = "lat";
    private static final String API_CONTACT = "contact";
    private static final String API_EMAIL = "email";
    private static final String API_IRC = "irc";
    private static final String API_PHONE = "phone";
    private static final String API_TWITTER = "twitter";
    private static final String API_ML = "ml";
    private static final String API_STREAM = "stream";
    private static final String API_CAM = "cam";

    protected static final String API_DEFAULT = "https://fixme.ch/cgi-bin/spaceapi.py";
    protected static final String API_ICON = "icon";
    protected static final String API_ICON_OPEN = "open";
    protected static final String API_ICON_CLOSED = "closed";
    protected static final String API_LOGO = "logo";
    protected static final String API_STATUS = "open";
    protected static final String API_LASTCHANGE = "lastchange";

    private SharedPreferences mPrefs;
    private String mResultHs;
    private String mResultDir;
    private String mApiUrl;
    private String mErrorApiMsg = null;
    private String mErrorApiTitle = null;
    private String mErrorDirTitle = null;
    private String mErrorDirMsg = null;
    private int mAppWidgetId;
    private boolean initialize = true;
    private boolean finishApi = false;
    private boolean finishDir = false;

    private GetDirTask getDirTask;
    private GetApiTask getApiTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Before onCreate for Android 1.5
        setTheme(android.R.style.Theme_Light_NoTitleBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(Main.this);

        Intent intent = getIntent();
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent
                .getAction())) {
            finishApi = true;
            // Configure the widget
            setContentView(R.layout.main);
            getDirTask = new GetDirTask();
            getDirTask.execute(API_DIRECTORY);

            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            findViewById(R.id.main_view).setVisibility(View.GONE);
            findViewById(R.id.scroll).setVisibility(View.GONE);
            findViewById(R.id.choose_msg).setVisibility(View.VISIBLE);
            findViewById(R.id.choose_ok).setVisibility(View.VISIBLE);
            findViewById(R.id.choose_ok).setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            setWidgetAlarm();
                            finish();
                        }
                    });
        } else {
            // Network check
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo == null || !netInfo.isConnected()) {
                mErrorDirTitle = "Network";
                mErrorDirMsg = "Network unreachable";
                showError();
                return;
            }

            // Show current hackerspace information
            if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                mApiUrl = mPrefs.getString(
                        PREF_API_URL_WIDGET
                                + intent.getIntExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        AppWidgetManager.INVALID_APPWIDGET_ID),
                        API_DEFAULT);
            } else {
                mApiUrl = mPrefs.getString(PREF_API_URL, API_DEFAULT);
            }
            final Bundle data = (Bundle) getLastNonConfigurationInstance();
            if (data == null
                    || !(savedInstanceState.containsKey(STATE_HS) && savedInstanceState
                            .containsKey(STATE_DIR))) {
                getDirTask = new GetDirTask();
                getDirTask.execute(API_DIRECTORY);
                getApiTask = new GetApiTask();
                getApiTask.execute(mApiUrl);
            } else {
                // Recover from saved instance
                finishApi = true;
                finishDir = true;
                mErrorDirMsg = null;
                mErrorApiMsg = null;
                mResultHs = data.getString(STATE_HS);
                mResultDir = data.getString(STATE_DIR);
                populateDataHs();
                populateDataDir();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        if(getApiTask != null){
            getApiTask.cancel(true);
        }
        if(getDirTask != null){
            getDirTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public Bundle onRetainNonConfigurationInstance() {
        Bundle data = new Bundle(2);
        data.putString(STATE_HS, mResultHs);
        data.putString(STATE_DIR, mResultDir);
        return data;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_HS, mResultHs);
        outState.putString(STATE_DIR, mResultDir);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        switch (id) {
        case DIALOG_LOADING:
            dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Loading...");
            dialog.setCancelable(true);
            ((ProgressDialog) dialog).setIndeterminate(true);
            break;
        case DIALOG_ERROR:
            if (mErrorApiMsg != null) {
                dialog = new AlertDialog.Builder(this)
                        .setTitle("Error: " + mErrorApiTitle)
                        .setMessage(mErrorApiMsg).setNeutralButton("Ok", null)
                        .create();
            } else if (mErrorDirMsg != null) {
                dialog = new AlertDialog.Builder(this)
                        .setTitle("Error: " + mErrorDirTitle)
                        .setMessage(mErrorDirMsg).setNeutralButton("Ok", null)
                        .create();
            }
            break;
        }
        return dialog;
    }

    private void showError() {
        if (mErrorDirMsg != null || mErrorApiMsg != null) {
            showDialog(DIALOG_ERROR);
        }
    }

    private void dismissLoading() {
        if (finishApi && finishDir) {
            try {
                removeDialog(DIALOG_LOADING);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void setWidgetAlarm() {
        Context ctxt = getApplicationContext();
        Intent i = Widget.getIntent(ctxt, mAppWidgetId);
        setResult(RESULT_OK, i);
        Widget.setAlarm(ctxt, i, mAppWidgetId);
    }

    private class GetDirTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
            mErrorDirMsg = null;
        }

        @Override
        protected String doInBackground(String... url) {
            ByteArrayOutputStream direcOs = new ByteArrayOutputStream();
            try {
                new Net(url[0], direcOs);
            } catch (Exception e) {
                mErrorDirTitle = e.getClass().getCanonicalName();
                mErrorDirMsg = e.getLocalizedMessage();
                e.printStackTrace();
            }
            return direcOs.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            finishDir = true;
            dismissLoading();
            if (mErrorDirMsg == null) {
                mResultDir = result;
                populateDataDir();
            } else {
                showError();
            }
        }

        @Override
        protected void onCancelled() {
            finishDir = true;
            dismissLoading();
        }
    }

    private class GetApiTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
            mErrorApiMsg = null;
            // Clean UI
            ((ScrollView) findViewById(R.id.scroll)).removeAllViews();
            ((TextView) findViewById(R.id.space_name)).setText("");
            ((TextView) findViewById(R.id.space_url)).setText("");
            ((ImageView) findViewById(R.id.space_image)).setImageBitmap(null);
        }

        @Override
        protected String doInBackground(String... url) {
            ByteArrayOutputStream spaceOs = new ByteArrayOutputStream();
            try {
                new Net(url[0], spaceOs);
            } catch (Exception e) {
                mErrorApiTitle = e.getClass().getCanonicalName();
                mErrorApiMsg = e.getLocalizedMessage();
                e.printStackTrace();
            }
            return spaceOs.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            finishApi = true;
            dismissLoading();
            if (mErrorApiMsg == null) {
                mResultHs = result;
                populateDataHs();
            } else {
                showError();
            }
        }

        @Override
        protected void onCancelled() {
            finishApi = true;
            dismissLoading();
        }
    }

    private class GetImage extends AsyncTask<String, Void, byte[]> {

        private int mId;

        public GetImage(int id) {
            mId = id;
        }

        @Override
        protected void onPreExecute() {
            // TODO: Show that the image is loading
            mErrorApiMsg = null;
        }

        @Override
        protected byte[] doInBackground(String... url) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                new Net(url[0], os);
            } catch (Exception e) {
                mErrorApiTitle = e.getClass().getCanonicalName();
                mErrorApiMsg = e.getLocalizedMessage();
                e.printStackTrace();
            }
            return os.toByteArray();
        }

        @Override
        protected void onPostExecute(byte[] result) {
            if (mErrorApiMsg == null) {
                ((ImageView) findViewById(mId)).setImageBitmap(BitmapFactory
                        .decodeByteArray(result, 0, result.length));
            } else {
                showError();
            }
        }

    }

    private void populateDataDir() {
        try {
            // Construct hackerspaces list
            Spinner s = (Spinner) findViewById(R.id.choose);
            JSONObject obj = new JSONObject(mResultDir);
            JSONArray arr = obj.names();
            int len = obj.length();
            String[] names = new String[len];
            final ArrayList<String> url = new ArrayList<String>(len);
            for (int i = 0; i < len; i++) {
                names[i] = arr.getString(i);
            }
            Arrays.sort(names);
            for (int i = 0; i < len; i++) {
                url.add(i, obj.getString(names[i]));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Main.this,
                    android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
            s.setAdapter(adapter);
            s.setSelection(url.indexOf(mApiUrl));
            s.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapter, View v,
                        int position, long id) {
                    Editor edit = mPrefs.edit();
                    if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
                            .equals(getIntent().getAction())) {
                        edit.putString(PREF_API_URL_WIDGET + mAppWidgetId,
                                url.get(position));
                    } else {
                        if (!initialize) {
                            edit.putString(PREF_API_URL, url.get(position));
                            getApiTask = new GetApiTask();
                            getApiTask.execute(url.get(position));
                        } else {
                            initialize = false;
                        }
                    }
                    edit.commit();
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            showError();
        }
    }

    private void populateDataHs() {
        try {
            JSONObject api = new JSONObject(mResultHs);
            // Initialize views
            LayoutInflater inflater = getLayoutInflater();
            LinearLayout vg = (LinearLayout) inflater.inflate(R.layout.base,
                    null);
            ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
            scroll.removeAllViews();
            scroll.addView(vg);
            // Mandatory fields
            String status_txt = "";
            // String status = API_ICON_CLOSED;
            if (api.getBoolean(API_STATUS)) {
                // status = API_ICON_OPEN;
                status_txt = OPEN;
                ((TextView) findViewById(R.id.status_txt))
                        .setCompoundDrawablesWithIntrinsicBounds(
                                android.R.drawable.presence_online, 0, 0, 0);
            } else {
                status_txt = CLOSED;
                ((TextView) findViewById(R.id.status_txt))
                        .setCompoundDrawablesWithIntrinsicBounds(
                                android.R.drawable.presence_busy, 0, 0, 0);
            }
            ((TextView) findViewById(R.id.space_name)).setText(api
                    .getString(API_NAME));
            ((TextView) findViewById(R.id.space_url)).setText(api
                    .getString(API_URL));
            // Status icon or space icon
            // if (!api.isNull(API_ICON)) {
            // JSONObject status_icon = api.getJSONObject(API_ICON);
            // if (!status_icon.isNull(status)) {
            // new GetImage(R.id.space_image).execute(status_icon
            // .getString(status));
            // }
            // } else {
            new GetImage(R.id.space_image).execute(api.getString(API_LOGO));
            // }
            // Status
            if (!api.isNull(API_STATUS_TXT)) {
                status_txt += ": " + api.getString(API_STATUS_TXT);
            }
            ((TextView) findViewById(R.id.status_txt)).setText(status_txt);
            if (!api.isNull(API_LASTCHANGE)) {
                Date date = new Date(api.getLong(API_LASTCHANGE) * 1000);
                SimpleDateFormat formatter = new SimpleDateFormat();
                TextView tv = (TextView) inflater.inflate(R.layout.entry, null);
                tv.setText("Last change: " + formatter.format(date));
                vg.addView(tv);
            }
            if (!api.isNull(API_DURATION) && api.getBoolean(API_STATUS)) {
                TextView tv = (TextView) inflater.inflate(R.layout.entry, null);
                tv.setText("Duration: " + api.getString(API_DURATION)
                        + " hour(s)");
                vg.addView(tv);
            }
            // Location
            Pattern ptn = Pattern.compile("^.*$", Pattern.DOTALL);
            if (!api.isNull(API_ADDRESS)
                    || (!api.isNull(API_LAT) && !api.isNull(API_LON))) {
                TextView title = (TextView) inflater.inflate(R.layout.title,
                        null);
                title.setText("Location");
                vg.addView(title);
                inflater.inflate(R.layout.separator, vg);
                if (!api.isNull(API_ADDRESS)) {
                    TextView tv = (TextView) inflater.inflate(R.layout.entry,
                            null);
                    tv.setAutoLinkMask(0);
                    tv.setText(api.getString(API_ADDRESS));
                    Linkify.addLinks(tv, ptn, MAP_SEARCH);
                    vg.addView(tv);
                }
                if (!api.isNull(API_LON) && !api.isNull(API_LAT)) {
                    TextView tv = (TextView) inflater.inflate(R.layout.entry,
                            null);
                    tv.setAutoLinkMask(0);
                    tv.setText(api.getString(API_LON) + ", "
                            + api.getString(API_LAT));
                    Linkify.addLinks(tv, ptn, String.format(MAP_COORD,
                            api.getString(API_LON), api.getString(API_LAT)));
                    vg.addView(tv);
                }
            }
            // Contact
            if (!api.isNull(API_CONTACT)) {
                TextView title = (TextView) inflater.inflate(R.layout.title,
                        null);
                title.setText("Contact");
                vg.addView(title);
                inflater.inflate(R.layout.separator, vg);
                JSONObject contact = api.getJSONObject(API_CONTACT);
                // Phone
                if (!contact.isNull(API_PHONE)) {
                    TextView tv = (TextView) inflater.inflate(R.layout.entry,
                            null);
                    tv.setText(contact.getString(API_PHONE));
                    vg.addView(tv);
                }
                // Twitter
                if (!contact.isNull(API_TWITTER)) {
                    TextView tv = (TextView) inflater.inflate(R.layout.entry,
                            null);
                    tv.setText(TWITTER + contact.getString(API_TWITTER));
                    vg.addView(tv);
                }
                // IRC
                if (!contact.isNull(API_IRC)) {
                    TextView tv = (TextView) inflater.inflate(R.layout.entry,
                            null);
                    tv.setAutoLinkMask(0);
                    tv.setText(contact.getString(API_IRC));
                    vg.addView(tv);
                }
                // Email
                if (!contact.isNull(API_EMAIL)) {
                    TextView tv = (TextView) inflater.inflate(R.layout.entry,
                            null);
                    tv.setText(contact.getString(API_EMAIL));
                    vg.addView(tv);
                }
                // Mailing-List
                if (!contact.isNull(API_ML)) {
                    TextView tv = (TextView) inflater.inflate(R.layout.entry,
                            null);
                    tv.setText(contact.getString(API_ML));
                    vg.addView(tv);
                }
            }
            // Stream and cam
            if (!api.isNull(API_STREAM) || !api.isNull(API_CAM)) {
                TextView title = (TextView) inflater.inflate(R.layout.title,
                        null);
                title.setText("Stream");
                vg.addView(title);
                inflater.inflate(R.layout.separator, vg);
                // Stream
                if (!api.isNull(API_STREAM)) {
                    JSONObject stream = api.optJSONObject(API_STREAM);
                    if (stream != null) {
                        JSONArray names = stream.names();
                        for (int i = 0; i < stream.length(); i++) {
                            final String type = names.getString(i);
                            final String url = stream.getString(type);
                            TextView tv = (TextView) inflater.inflate(
                                    R.layout.entry, null);
                            tv.setText(url);
                            tv.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setDataAndType(Uri.parse(url), type);
                                    startActivity(i);
                                }
                            });
                            vg.addView(tv);
                        }
                    } else {
                        String streamStr = api.optString(API_STREAM);
                        TextView tv = (TextView) inflater.inflate(
                                R.layout.entry, null);
                        tv.setText(streamStr);
                        vg.addView(tv);
                    }
                }
                // Cam
                if (!api.isNull(API_CAM)) {
                    JSONArray cam = api.optJSONArray(API_CAM);
                    if (cam != null) {
                        for (int i = 0; i < cam.length(); i++) {
                            TextView tv = (TextView) inflater.inflate(
                                    R.layout.entry, null);
                            tv.setText(cam.getString(i));
                            vg.addView(tv);
                        }
                    } else {
                        String camStr = api.optString(API_CAM);
                        TextView tv = (TextView) inflater.inflate(
                                R.layout.entry, null);
                        tv.setText(camStr);
                        vg.addView(tv);
                    }
                }
            }
        } catch (Exception e) {
            mErrorApiTitle = e.getClass().getCanonicalName();
            mErrorApiMsg = e.getLocalizedMessage();
            e.printStackTrace();
        } finally {
            showError();
        }
    }

}
