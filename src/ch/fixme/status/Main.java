/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package ch.fixme.status;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
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
    private static final String PREF_API_URL = "apiurl";
    private static final int DIALOG_LOADING = 0;
    private static final int DIALOG_ERROR = 1;

    private static final String API_DIRECTORY = "http://openspace.slopjong.de/directory.json";
    private static final String API_NAME = "space";
    private static final String API_URL = "url";
    private static final String API_LASTCHANGE = "lastchange";
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
    private static final String TWITTER = "https://twitter.com/#!/";

    protected static final String API_DEFAULT = "https://fixme.ch/cgi-bin/spaceapi.py";
    protected static final String API_ICON = "icon";
    protected static final String API_ICON_OPEN = "open";
    protected static final String API_ICON_CLOSED = "closed";
    protected static final String API_LOGO = "logo";
    protected static final String API_STATUS = "open";

    private SharedPreferences mPrefs;
    private String mApiUrl;
    private String mErrorMsg = null;
    private int mAppWidgetId;
    private boolean initialize = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
        new GetDirTask().execute(API_DIRECTORY);

        // Configure the widget
        Intent intent = getIntent();
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent
                .getAction())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            findViewById(R.id.main_view).setVisibility(View.GONE);
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
            mApiUrl = mPrefs.getString(PREF_API_URL, API_DEFAULT);
            new GetApiTask().execute(mApiUrl);
        }
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
                dialog = new AlertDialog.Builder(this).setTitle("Error")
                        .setMessage(mErrorMsg).setNeutralButton("Ok", null)
                        .create();
                break;
        }
        return dialog;
    }

    private void showError() {
        if (mErrorMsg != null) {
            showDialog(DIALOG_ERROR);
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
            mErrorMsg = null;
        }

        @Override
        protected String doInBackground(String... url) {
            ByteArrayOutputStream direcOs = new ByteArrayOutputStream();
            try {
                new Net(url[0], direcOs);
            } catch (Exception e) {
                mErrorMsg = e.getLocalizedMessage();
                e.printStackTrace();
            }
            return direcOs.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Construct hackerspaces list
                Spinner s = (Spinner) findViewById(R.id.choose);
                JSONObject obj = new JSONObject(result);
                JSONArray arr = obj.names();
                int len = obj.length();
                String[] names = new String[len];
                final ArrayList<String> url = new ArrayList<String>(len);
                for (int i = 0; i < len; i++) {
                    names[i] = arr.getString(i);
                    url.add(i, obj.getString(names[i]));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        Main.this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                s.setAdapter(adapter);
                s.setSelection(url.indexOf(mApiUrl));
                s.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapter, View v,
                            int position, long id) {
                        if (!initialize) {
                            Editor edit = mPrefs.edit();
                            if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
                                    .equals(getIntent().getAction())) {
                                edit.putString(PREF_API_URL_WIDGET
                                        + mAppWidgetId, url.get(position));
                            } else {
                                edit.putString(PREF_API_URL, url.get(position));
                                new GetApiTask().execute(url.get(position));
                            }
                            edit.commit();
                        } else {
                            initialize = false;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                dismissDialog(DIALOG_LOADING);
                showError();
            }
        }

        @Override
        protected void onCancelled() {
            dismissDialog(DIALOG_LOADING);
        }
    }

    private class GetApiTask extends AsyncTask<String, Void, String> {
        private LayoutInflater mInflater;
        private LinearLayout mVg;

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
            mErrorMsg = null;
            // Initialize views
            mInflater = getLayoutInflater();
            mVg = (LinearLayout) mInflater.inflate(R.layout.base, null);
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
                mErrorMsg = e.getLocalizedMessage();
                e.printStackTrace();
            }
            return spaceOs.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject api = new JSONObject(result);
                ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                scroll.removeAllViews();
                scroll.addView(mVg);
                // Mandatory fields
                String status_txt = "";
                String status = API_ICON_CLOSED;
                if (api.getBoolean(API_STATUS)) {
                    status = API_ICON_OPEN;
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
                    TextView tv = (TextView) mInflater.inflate(R.layout.entry,
                            null);
                    tv.setText("Last change: " + formatter.format(date));
                    mVg.addView(tv);
                }
                if (!api.isNull(API_DURATION) && api.getBoolean(API_STATUS)) {
                    TextView tv = (TextView) mInflater.inflate(R.layout.entry,
                            null);
                    tv.setText("Duration: " + api.getString(API_DURATION)
                            + " hour(s)");
                    mVg.addView(tv);
                }
                // Location
                if (!api.isNull(API_ADDRESS)
                        || (!api.isNull(API_LAT) && !api.isNull(API_LON))) {
                    TextView title = (TextView) mInflater.inflate(
                            R.layout.title, null);
                    title.setText("Location");
                    mVg.addView(title);
                    mInflater.inflate(R.layout.separator, mVg);
                    if (!api.isNull(API_ADDRESS)) {
                        TextView tv = (TextView) mInflater.inflate(
                                R.layout.entry, null);
                        tv.setAutoLinkMask(Linkify.MAP_ADDRESSES);
                        tv.setText(api.getString(API_ADDRESS));
                        mVg.addView(tv);
                    }
                    if (!api.isNull(API_LON) && !api.isNull(API_LAT)) {
                        TextView tv = (TextView) mInflater.inflate(
                                R.layout.entry, null);
                        tv.setAutoLinkMask(0);
                        tv.setText(api.getString(API_LON) + ", "
                                + api.getString(API_LAT));
                        mVg.addView(tv);
                    }
                }
                // Contact
                if (!api.isNull(API_CONTACT)) {
                    TextView title = (TextView) mInflater.inflate(
                            R.layout.title, null);
                    title.setText("Contact");
                    mVg.addView(title);
                    mInflater.inflate(R.layout.separator, mVg);
                    JSONObject contact = api.getJSONObject(API_CONTACT);
                    // Phone
                    if (!contact.isNull(API_PHONE)) {
                        TextView tv = (TextView) mInflater.inflate(
                                R.layout.entry, null);
                        tv.setText(contact.getString(API_PHONE));
                        mVg.addView(tv);
                    }
                    // Twitter
                    if (!contact.isNull(API_TWITTER)) {
                        TextView tv = (TextView) mInflater.inflate(
                                R.layout.entry, null);
                        tv.setText(TWITTER + contact.getString(API_TWITTER));
                        mVg.addView(tv);
                    }
                    // IRC
                    if (!contact.isNull(API_IRC)) {
                        TextView tv = (TextView) mInflater.inflate(
                                R.layout.entry, null);
                        tv.setAutoLinkMask(0);
                        tv.setText(contact.getString(API_IRC));
                        mVg.addView(tv);
                    }
                    // Email
                    if (!contact.isNull(API_EMAIL)) {
                        TextView tv = (TextView) mInflater.inflate(
                                R.layout.entry, null);
                        tv.setText(contact.getString(API_EMAIL));
                        mVg.addView(tv);
                    }
                    // Mailing-List
                    if (!contact.isNull(API_ML)) {
                        TextView tv = (TextView) mInflater.inflate(
                                R.layout.entry, null);
                        tv.setText(contact.getString(API_ML));
                        mVg.addView(tv);
                    }
                }
            } catch (JSONException e) {
                mErrorMsg = e.getLocalizedMessage();
                e.printStackTrace();
            } finally {
                dismissDialog(DIALOG_LOADING);
                showError();
            }
        }

        @Override
        protected void onCancelled() {
            dismissDialog(DIALOG_LOADING);
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
            mErrorMsg = null;
        }

        @Override
        protected byte[] doInBackground(String... url) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                new Net(url[0], os);
            } catch (Exception e) {
                mErrorMsg = e.getLocalizedMessage();
                e.printStackTrace();
            }
            return os.toByteArray();
        }

        @Override
        protected void onPostExecute(byte[] result) {
            ((ImageView) findViewById(mId)).setImageBitmap(BitmapFactory
                    .decodeByteArray(result, 0, result.length));
            showError();
        }

    }

}
