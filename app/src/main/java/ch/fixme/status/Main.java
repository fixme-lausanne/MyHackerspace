/*
 * Copyright (C) 2012-2017 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 3, see README
 */

package ch.fixme.status;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.woozzu.android.util.StringMatcher;
import com.woozzu.android.widget.IndexableListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class Main extends Activity {

    // API: https://spaceapi.io/

    protected static final String TAG = "MyHackerspace";
    protected static final String PREF_API_URL_WIDGET = "api_url_widget_";
    protected static final String PREF_LAST_WIDGET = "last_widget_";
    protected static final String PREF_FORCE_WIDGET = "force_widget_";
    protected static final String STATE_HS = "hs";
    protected static final String STATE_DIR = "dir";
    protected static final String STATE_URL = "url";
    private static final int DIALOG_LOADING = 0;
    private static final int DIALOG_LIST = 1;
    private static final String TWITTER = "https://twitter.com/";
    private static final String FOURSQUARE = "https://foursquare.com/v/";
    private static final String MAP_SEARCH = "geo:0,0?q=";
    private static final String MAP_COORD = "geo:%s,%s?z=23&q=%s&";

    private SharedPreferences mPrefs;
    private HashMap<String, String> mResultHs;
    public String mResultDir;
    private String mApiUrl;
    private boolean finishApi = false;
    private boolean finishDir = false;

    private ArrayList<String> mHsNames;
    private ArrayList<String> mHsUrls;

    private GetDirTask getDirTask;
    private GetApiTask getApiTask;
    private GetImage getImageTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setViewVisibility(false);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
        mResultHs = new HashMap<>();
        if (checkNetwork()) {
            Log.d(TAG, "onCreate() intent="+ getIntent().toString());
            setCache();
            getHsList(savedInstanceState);
            showHsInfo(getIntent());
        } else {
            showError(getString(R.string.error_title) + getString(R.string.error_network_title),
                    getString(R.string.error_network_msg));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent()=" + intent);
        showHsInfo(intent);
    }

    @Override
    protected void onDestroy() {
        if (getApiTask != null) {
            getApiTask.cancel(true);
        }
        if (getDirTask != null) {
            getDirTask.cancel(true);
        }
        if (getImageTask != null) {
            getImageTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            if (checkNetwork()){
                showHsInfo(getIntent());
            } else {
                showError(getString(R.string.error_title) + getString(R.string.error_network_title),
                        getString(R.string.error_network_msg));
            }
            return true;
        case R.id.menu_choose:
            showDialog(DIALOG_LIST);
            return true;
        case R.id.menu_prefs:
            startActivity(new Intent(Main.this, Prefs.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Bundle onRetainNonConfigurationInstance() {
        Bundle data = new Bundle(3);
        if(mResultHs != null && mResultHs.size() > 0) {
            data.putSerializable(STATE_HS, mResultHs);
        }
        if(mResultDir != null && mResultDir.length() > 0) {
            data.putString(STATE_DIR, mResultDir);
        }
        if(mApiUrl != null && mApiUrl.length() > 0) {
            data.putString(STATE_URL, mApiUrl);
        }
        return data;
    }

    @Override
    protected void onSaveInstanceState(Bundle data) {
        if(mResultHs != null && mResultHs.size() > 0) {
            data.putSerializable(STATE_HS, mResultHs);
        }
        if(mResultDir != null && mResultDir.length() > 0) {
            data.putString(STATE_DIR, mResultDir);
        }
        if(mApiUrl != null && mApiUrl.length() > 0) {
            data.putString(STATE_URL, mApiUrl);
        }
        super.onSaveInstanceState(data);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        switch (id) {
        case DIALOG_LOADING:
            dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.msg_loading));
            dialog.setCancelable(true);
            ((ProgressDialog) dialog).setIndeterminate(true);
            break;
        case DIALOG_LIST:
            return createHsDialog();
        }
        return dialog;
    }

    @Override
    public void startActivity(Intent intent) {
        // http://stackoverflow.com/questions/13691241/autolink-not-working-on-htc-htclinkifydispatcher
        try {
            /* First attempt at fixing an HTC broken by evil Apple patents. */
            if (intent.getComponent() != null
                    && ".HtcLinkifyDispatcherActivity".equals(intent
                            .getComponent().getShortClassName()))
                intent.setComponent(null);
            super.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            /*
             * Probably an HTC broken by evil Apple patents. This is not
             * perfect, but better than crashing the whole application.
             */
            Log.e(Main.TAG, e.getMessage());
            super.startActivity(Intent.createChooser(intent, null));
        }
    }

    private void setViewVisibility(boolean show) {
        int visibility1 = View.GONE;
        int visibility2 = View.VISIBLE;
        if (show) {
            visibility1 = View.VISIBLE;
            visibility2 = View.GONE;
        }
        // Views visibility
        findViewById(R.id.space_image).setVisibility(visibility1);
        findViewById(R.id.space_name).setVisibility(visibility1);
        findViewById(R.id.space_url).setVisibility(visibility1);
        findViewById(R.id.placeholder).setVisibility(visibility2);
        findViewById(R.id.main_wrapper).invalidate();
    }

    private AlertDialog createHsDialog() {
        // Construct hackerspaces list
        if (mResultDir == null){
            return null;
        }
        try {
            JSONObject obj = new JSONObject(mResultDir);
            JSONArray arr = obj.names();
            int len = obj.length();
            mHsNames = new ArrayList<>(len);
            mHsUrls = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                mHsNames.add(arr.getString(i));
            }
            Collections.sort(mHsNames, String.CASE_INSENSITIVE_ORDER);
            for (int i = 0; i < len; i++) {
                mHsUrls.add(i, obj.getString(mHsNames.get(i)));
            }

            // Create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
            View view = getLayoutInflater().inflate(R.layout.hs_choose, null);
            ContentAdapter adapter = new ContentAdapter(Main.this,
                    android.R.layout.simple_list_item_2, mHsNames);

            IndexableListView listView = (IndexableListView) view
                    .findViewById(R.id.listview);
            listView.setAdapter(adapter);
            listView.setFastScrollEnabled(true);
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    Log.d(TAG, "hs dialog, on click");
                    String url = mHsUrls.get(position);
                    Editor edit = mPrefs.edit();
                    edit.putString(Prefs.KEY_API_URL, url);
                    getApiTask = new GetApiTask();
                    getApiTask.execute(url);
                    edit.commit();
                    setIntent(null);
                    dismissDialog(DIALOG_LIST);
                    Log.i(TAG, "Item clicked=" + url +  " (" + position + ")");
                }
            });
            builder.setView(view);
            builder.setTitle(R.string.choose_hs);

            return builder.create();
        } catch (Exception e) {
            e.printStackTrace();
            return showError(e.getClass().getCanonicalName(), e.getLocalizedMessage(), true);
        }
    }

    private void getHsList(Bundle savedInstanceState) {
        final Bundle data = (Bundle) getLastNonConfigurationInstance();
        if (data == null) {
            Log.d(TAG, "getHsList(fresh data)");
            String apiEndpoint = mPrefs.getString(Prefs.KEY_API_ENDPOINT, Prefs.DEFAULT_API_ENDPOINT);
            getDirTask = new GetDirTask();
            getDirTask.execute(apiEndpoint);
        } else {
            Log.d(TAG, "getHsList(from state)");
            finishDir = true;
            mResultDir = data.getString(STATE_DIR);
        }
    }

    private void showHsInfo(Intent intent) {
        final Bundle data = (Bundle) getLastNonConfigurationInstance();
        // Get hackerspace api url
        if(data != null && data.containsKey(STATE_URL)) {
            Log.d(TAG, "showHsInfo(uri from state)");
            mApiUrl = data.getString(STATE_URL);
        } else if (intent != null
                && intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            Log.d(TAG, "showHsInfo(uri from widget intent)");
            mApiUrl = mPrefs.getString(
                    PREF_API_URL_WIDGET
                            + intent.getIntExtra(
                                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                                    AppWidgetManager.INVALID_APPWIDGET_ID),
                    ParseGeneric.API_DEFAULT);
        } else if (intent != null && intent.hasExtra(STATE_HS)) {
            Log.d(TAG, "showHsInfo(uri from intent)");
            mApiUrl = intent.getStringExtra(STATE_HS);
        } else {
            Log.d(TAG, "showHsInfo(uri from prefs)");
            mApiUrl = mPrefs.getString(Prefs.KEY_API_URL, ParseGeneric.API_DEFAULT);
        }
        // Get Data
        if(data != null && data.containsKey(STATE_HS)) {
            Log.d(TAG, "showHsInfo(data from state)");
            finishApi = true;
            mResultHs = (HashMap<String, String>) data.getSerializable(STATE_HS);
            populateDataHs();
        } else if(mResultHs.containsKey(mApiUrl)) {
            Log.d(TAG, "showHsInfo(data from cache)");
            finishApi = true;
            populateDataHs();
        } else {
            Log.d(TAG, "showHsInfo(fresh data)");
            getApiTask = new GetApiTask();
            getApiTask.execute(mApiUrl);
        }

        // Update widget
        Widget.UpdateAllWidgets(getApplicationContext(), false);
    }

    private boolean checkNetwork() {
        return checkNetwork(getApplicationContext());
    }

    protected static boolean checkNetwork(Context ctxt) {
        ConnectivityManager cm = (ConnectivityManager) ctxt
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void setCache() {
        try {
            File httpCacheDir = new File(getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void showError(String title, String msg) {
        showError(title, msg, false);
    }

    private AlertDialog showError(String title, String msg, boolean ret) {
        if (title != null && msg != null) {
            AlertDialog dialog = new AlertDialog.Builder(Main.this)
                    .setTitle(getString(R.string.error_title) + title)
                    .setMessage(msg)
                    .setNeutralButton(getString(R.string.ok), null).create();
            if (ret) {
                return dialog;
            } else {
                dialog.show();
            }
        }
        return null;
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

    public class GetDirTask extends AsyncTask<String, Void, String> {

        private String mErrorTitle;
        private String mErrorMsg;

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
        }

        @Override
        protected String doInBackground(String... url) {
            try {
                return new Net(url[0]).getString();
            } catch (Throwable e) {
                mErrorTitle = e.getClass().getCanonicalName();
                mErrorMsg = e.getLocalizedMessage() + " " + url[0];
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            finishDir = true;
            dismissLoading();
            if (mErrorMsg == null) {
                mResultDir = result;
            } else {
                showError(mErrorTitle, mErrorMsg);
            }
        }

        @Override
        protected void onCancelled() {
            finishDir = true;
            dismissLoading();
        }
    }

    private class GetApiTask extends AsyncTask<String, Void, String> {

        private String mErrorTitle;
        private String mErrorMsg;
        private String mUrl;

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
            // Clean UI
            ((ScrollView) findViewById(R.id.scroll)).removeAllViews();
            setViewVisibility(false);
        }

        @Override
        protected String doInBackground(String... url) {
            mUrl = url[0];
            Log.d(TAG, "GetApiTask(), mUrl="+mUrl);
            try {
                return new Net(url[0], false).getString();
            } catch (Throwable e) {
                mErrorTitle = e.getClass().getCanonicalName();
                mErrorMsg = e.getLocalizedMessage() + " " + url[0];
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            finishApi = true;
            dismissLoading();
            if (mErrorMsg == null) {
                mResultHs.put(mUrl, result);
                showHsInfo(getIntent());
            } else {
                setViewVisibility(false);
                showError(mErrorTitle, mErrorMsg);
            }
        }

        @Override
        protected void onCancelled() {
            finishApi = true;
            dismissLoading();
        }
    }

    private class GetImage extends AsyncTask<String, Void, Bitmap> {

        private final int mId;
        private String mErrorTitle;
        private String mErrorMsg;

        GetImage(int id) {
            mId = id;
        }

        @Override
        protected void onPreExecute() {
            ImageView img = findViewById(mId);
            img.setImageResource(android.R.drawable.ic_popup_sync);
            AnimationDrawable anim = (AnimationDrawable) img.getDrawable();
            anim.start();
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            try {
                return new Net(url[0]).getBitmap();
            } catch (Throwable e) {
                mErrorTitle = e.getClass().getCanonicalName();
                mErrorMsg = e.getLocalizedMessage() + " " + url[0];
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (mErrorMsg == null) {
                ((ImageView) findViewById(mId)).setImageBitmap(result);
            } else {
                showError(mErrorTitle, mErrorMsg);
                ((ImageView) findViewById(mId))
                        .setImageResource(android.R.drawable.ic_menu_report_image);
            }
        }

    }

    private TextView addEntry(LayoutInflater inflater, LinearLayout vg, String value) {
        TextView tv = (TextView) inflater.inflate(R.layout.entry, null);
        tv.setAutoLinkMask(0);
        tv.setText(value);
        vg.addView(tv);
        return tv;
    }
    private TextView addTitle(LayoutInflater inflater, LinearLayout vg, int value) {
        return addTitle(inflater, vg, getString(value));
    }

    private TextView addTitle(LayoutInflater inflater, LinearLayout vg, String value) {
        TextView title = (TextView) inflater.inflate(R.layout.title, null);
        title.setText(value);
        vg.addView(title);
        inflater.inflate(R.layout.separator, vg);
        return title;
    }

    private void populateDataHs() {
        try {
            Log.i(TAG, "populateDataHs()=" + mApiUrl);
            setViewVisibility(false);
            HashMap<String, Object> data = new ParseGeneric(mResultHs.get(mApiUrl))
                    .getData();

            // Initialize views
            LayoutInflater iftr = getLayoutInflater();
            LinearLayout vg = (LinearLayout) iftr.inflate(R.layout.base, null);
            ScrollView scroll = findViewById(R.id.scroll);
            scroll.removeAllViews();
            scroll.addView(vg);

            // Mandatory fields
            ((TextView) findViewById(R.id.space_name)).setText((String) data
                    .get(ParseGeneric.API_NAME));
            ((TextView) findViewById(R.id.space_url)).setText((String) data
                    .get(ParseGeneric.API_URL));
            getImageTask = new GetImage(R.id.space_image);
            getImageTask.execute((String) data.get(ParseGeneric.API_LOGO));

            // Status text
            String status_txt;
            if (data.get(ParseGeneric.API_STATUS) == null) {
                status_txt = getString(R.string.status_unknown);
                ((TextView) findViewById(R.id.status_txt))
                        .setCompoundDrawablesWithIntrinsicBounds(
                                android.R.drawable.presence_invisible, 0, 0, 0);
            } else if ((Boolean) data.get(ParseGeneric.API_STATUS)) {
                status_txt = getString(R.string.status_open);
                ((TextView) findViewById(R.id.status_txt))
                        .setCompoundDrawablesWithIntrinsicBounds(
                                android.R.drawable.presence_online, 0, 0, 0);
            } else {
                status_txt = getString(R.string.status_closed);
                ((TextView) findViewById(R.id.status_txt))
                        .setCompoundDrawablesWithIntrinsicBounds(
                                android.R.drawable.presence_busy, 0, 0, 0);
            }
            if (data.containsKey(ParseGeneric.API_STATUS_TXT)) {
                status_txt += ": "
                        + data.get(ParseGeneric.API_STATUS_TXT);
            }
            ((TextView) findViewById(R.id.status_txt)).setText(status_txt);

            // Status last change
            if (data.containsKey(ParseGeneric.API_LASTCHANGE)) {
                addEntry(iftr, vg, getString(R.string.api_lastchange) + " "
                        + data.get(ParseGeneric.API_LASTCHANGE));
            }

            // Status duration
            if (data.containsKey(ParseGeneric.API_EXT_DURATION)
                    && data.get(ParseGeneric.API_STATUS) != null
                    && (Boolean) data.get(ParseGeneric.API_STATUS)) {
                addEntry(iftr, vg, getString(R.string.api_duration) + " "
                        + data.get(ParseGeneric.API_EXT_DURATION)
                        + getString(R.string.api_duration_hours));
            }

            // Location
            Pattern ptn = Pattern.compile("^.*$", Pattern.DOTALL);
            if (data.containsKey(ParseGeneric.API_ADDRESS)
                    || data.containsKey(ParseGeneric.API_LON)) {

                addTitle(iftr, vg, R.string.api_location);

                // Address
                if (data.containsKey(ParseGeneric.API_ADDRESS)) {
                    TextView tv = addEntry(iftr, vg, (String) data.get(ParseGeneric.API_ADDRESS));
                    Linkify.addLinks(tv, ptn, MAP_SEARCH);
                }
                // Lon/Lat
                if (data.containsKey(ParseGeneric.API_LON)
                        && data.containsKey(ParseGeneric.API_LAT)) {
                    TextView tv = addEntry(iftr, vg, data.get(ParseGeneric.API_LON) + ", "
                            + data.get(ParseGeneric.API_LAT));
                    String addr = (data.containsKey(ParseGeneric.API_ADDRESS)) ? (String) data
                            .get(ParseGeneric.API_ADDRESS) : getString(R.string.empty);
                    Linkify.addLinks(tv, ptn, String.format(MAP_COORD,
                            data.get(ParseGeneric.API_LAT), data.get(ParseGeneric.API_LON), addr));
                }
            }

            // Contact
            if (data.containsKey(ParseGeneric.API_PHONE)
                    || data.containsKey(ParseGeneric.API_TWITTER)
                    || data.containsKey(ParseGeneric.API_IRC)
                    || data.containsKey(ParseGeneric.API_EMAIL)
                    || data.containsKey(ParseGeneric.API_ML)) {

                addTitle(iftr, vg, R.string.api_contact);

                if (data.containsKey(ParseGeneric.API_PHONE)) {
                    addEntry(iftr, vg, (String) data.get(ParseGeneric.API_PHONE));
                }
                if (data.containsKey(ParseGeneric.API_SIP)) {
                    addEntry(iftr, vg, (String) data.get(ParseGeneric.API_SIP));
                }
                if (data.containsKey(ParseGeneric.API_TWITTER)) {
                    addEntry(iftr, vg, TWITTER
                            + ((String) data.get(ParseGeneric.API_TWITTER)).replace("@", ""));
                }
                if (data.containsKey(ParseGeneric.API_IDENTICA)) {
                    addEntry(iftr, vg, (String) data.get(ParseGeneric.API_IDENTICA));
                }
                if (data.containsKey(ParseGeneric.API_FOURSQUARE)) {
                    addEntry(iftr, vg, FOURSQUARE + data.get(ParseGeneric.API_FOURSQUARE));
                }
                if (data.containsKey(ParseGeneric.API_IRC)) {
                    addEntry(iftr, vg, (String) data.get(ParseGeneric.API_IRC));
                }
                if (data.containsKey(ParseGeneric.API_EMAIL)) {
                    addEntry(iftr, vg, (String) data.get(ParseGeneric.API_EMAIL));
                }
                if (data.containsKey(ParseGeneric.API_JABBER)) {
                    addEntry(iftr, vg, (String) data.get(ParseGeneric.API_JABBER));
                }
                if (data.containsKey(ParseGeneric.API_ML)) {
                    addEntry(iftr, vg, (String) data.get(ParseGeneric.API_ML));
                }
            }

            // Sensors
            if (data.containsKey(ParseGeneric.API_SENSORS)) {

                addTitle(iftr, vg, R.string.api_sensors);

                HashMap<String, ArrayList<HashMap<String, String>>> sensors =
                        (HashMap<String, ArrayList<HashMap<String, String>>>)
                                data.get(ParseGeneric.API_SENSORS);
                Set<String> names = sensors.keySet();
                for (String name : names) {

                    // Subtitle
                    String name_title = name.toLowerCase(Locale.getDefault()).replace("_", " ");
                    name_title = name_title.substring(0, 1).toUpperCase(Locale.getDefault())
                            + name_title.substring(1, name_title.length());
                    TextView subtitle = (TextView) iftr.inflate(
                            R.layout.subtitle, null);
                    subtitle.setText(name_title);
                    vg.addView(subtitle);

                    // Sensors data
                    ArrayList<HashMap<String, String>> sensorsData = sensors
                            .get(name);
                    for (HashMap<String, String> elem : sensorsData) {
                        RelativeLayout rl = (RelativeLayout) iftr.inflate(
                                R.layout.entry_sensor, null);
                        if (elem.containsKey(ParseGeneric.API_SENSOR_VALUE)) {
                            ((TextView) rl.findViewById(R.id.entry_value))
                                    .setText(elem.get(ParseGeneric.API_SENSOR_VALUE));
                        } else {
                            rl.findViewById(R.id.entry_value).setVisibility(
                                    View.GONE);
                        }
                        if (elem.containsKey(ParseGeneric.API_SENSOR_UNIT)) {
                            ((TextView) rl.findViewById(R.id.entry_unit))
                                    .setText(elem.get(ParseGeneric.API_SENSOR_UNIT));
                        } else {
                            rl.findViewById(R.id.entry_unit).setVisibility(
                                    View.GONE);
                        }
                        if (elem.containsKey(ParseGeneric.API_SENSOR_NAME)) {
                            ((TextView) rl.findViewById(R.id.entry_name))
                                    .setText(elem.get(ParseGeneric.API_SENSOR_NAME));
                        } else {
                            rl.findViewById(R.id.entry_name).setVisibility(
                                    View.GONE);
                        }
                        if (elem.containsKey(ParseGeneric.API_SENSOR_LOCATION)) {
                            ((TextView) rl.findViewById(R.id.entry_location))
                                    .setText(elem
                                            .get(ParseGeneric.API_SENSOR_LOCATION));
                        } else {
                            rl.findViewById(R.id.entry_location).setVisibility(
                                    View.GONE);
                        }
                        if (elem.containsKey(ParseGeneric.API_SENSOR_DESCRIPTION)) {
                            ((TextView) rl.findViewById(R.id.entry_description))
                                    .setText(elem
                                            .get(ParseGeneric.API_SENSOR_DESCRIPTION));
                        } else {
                            rl.findViewById(R.id.entry_description)
                                    .setVisibility(View.GONE);
                        }
                        if (elem.containsKey(ParseGeneric.API_SENSOR_PROPERTIES)) {
                            ((TextView) rl.findViewById(R.id.entry_properties))
                                    .setText(elem
                                            .get(ParseGeneric.API_SENSOR_PROPERTIES));
                        } else {
                            rl.findViewById(R.id.entry_properties)
                                    .setVisibility(View.GONE);
                        }
                        if (elem.containsKey(ParseGeneric.API_SENSOR_MACHINES)) {
                            ((TextView) rl.findViewById(R.id.entry_other))
                                    .setText(elem
                                            .get(ParseGeneric.API_SENSOR_MACHINES));
                        } else if (elem.containsKey(ParseGeneric.API_SENSOR_NAMES)) {
                            ((TextView) rl.findViewById(R.id.entry_other))
                                    .setText(elem.get(ParseGeneric.API_SENSOR_NAMES));
                        } else {
                            rl.findViewById(R.id.entry_other).setVisibility(
                                    View.GONE);
                        }
                        vg.addView(rl);
                    }
                }
            }

            // Stream and cam
            if (data.containsKey(ParseGeneric.API_STREAM)
                    || data.containsKey(ParseGeneric.API_CAM)) {

                addTitle(iftr, vg, R.string.api_stream);

                // Stream
                if (data.containsKey(ParseGeneric.API_STREAM)) {
                    HashMap<String, String> stream = (HashMap<String, String>) data
                            .get(ParseGeneric.API_STREAM);
                    for (Entry<String, String> entry : stream.entrySet()) {
                        final String type = entry.getKey();
                        final String url = entry.getValue();
                        TextView tv = addEntry(iftr, vg, url);
                        tv.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setDataAndType(Uri.parse(url), type);
                                startActivity(i);
                            }
                        });
                    }
                }
                // Cam
                if (data.containsKey(ParseGeneric.API_CAM)) {
                    ArrayList<String> cams = (ArrayList<String>) data
                            .get(ParseGeneric.API_CAM);
                    for (String value : cams) {
                        addEntry(iftr, vg, value);
                    }
                }
            }
            setViewVisibility(true);
        } catch (Exception e) {
            e.printStackTrace();
            setViewVisibility(false);
            showError(e.getClass().getCanonicalName(), e.getLocalizedMessage()
                    + getString(R.string.error_generic));
        }
    }

    static class ViewHolder {
        TextView name;
        TextView desc;
    }

    // https://github.com/woozzu/IndexableListView/
    private class ContentAdapter extends ArrayAdapter<String> implements
            SectionIndexer {

        private final LayoutInflater mInflater;
        private final String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        ContentAdapter(Context context, int textViewResourceId,
                List<String> objects) {
            super(context, textViewResourceId, objects);
            mInflater = getLayoutInflater();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.hs_entry, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.text1);
                holder.desc = (TextView) convertView.findViewById(R.id.text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(mHsNames.get(position));
            holder.desc.setText(mHsUrls.get(position));
            return convertView;
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

}
