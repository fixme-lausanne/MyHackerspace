package ch.fixme.status;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class Main extends Activity {

    // API: http://hackerspaces.nl/spaceapi/

    public static final String PKG = "ch.fixme.status";
    private static final int DIALOG_LOADING = 0;
    private static final int DIALOG_ERROR = 1;
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    private static final String API_DIRECTORY = "http://openspace.slopjong.de/directory.json";
    private static final String API_KEY = "apiurl";
    private static final String API_DEFAULT = "https://fixme.ch/cgi-bin/spaceapi.py";
    private static final String API_NAME = "space";
    private static final String API_URL = "url";
    private static final String API_LOGO = "logo";
    private static final String API_STATUS = "open";
    private static final String API_STATUS_TXT = "status";
    private static final String API_ICON = "icon";
    private static final String API_ICON_OPEN = "open";
    private static final String API_ICON_CLOSED = "closed";
    private static final String API_ADDRESS = "address";
    private static final String API_LON = "lon";
    private static final String API_LAT = "lat";

    private SharedPreferences mPrefs;
    private String mApiUrl;
    private String mErrorMsg = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(Main.this);
        mApiUrl = mPrefs.getString(API_KEY, API_DEFAULT);
        new GetApiTask().execute(mApiUrl);
        new GetDirTask().execute(API_DIRECTORY);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        switch (id) {
            case DIALOG_LOADING:
                dialog = new ProgressDialog(this);
                dialog.setCancelable(false);
                dialog.setMessage("Loading...");
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
                        new GetApiTask().execute(url.get(position));
                        Editor edit = mPrefs.edit();
                        edit.putString(API_KEY, url.get(position));
                        edit.commit();
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
    }

    private class GetApiTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
            mErrorMsg = null;
            // Clean UI
            ((TextView) findViewById(R.id.space_name)).setText("");
            ((TextView) findViewById(R.id.space_url)).setText("");
            ((TextView) findViewById(R.id.status_txt)).setText("");
            ((TextView) findViewById(R.id.location_address)).setText("");
            ((TextView) findViewById(R.id.location_map)).setText("");
            ((ImageView) findViewById(R.id.space_image)).setImageBitmap(null);
            ((ImageView) findViewById(R.id.status_image)).setImageBitmap(null);
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
                // Mandatory fields
                new GetImage(R.id.space_image).execute(api.getString(API_LOGO));
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
                // Status text
                if (!api.isNull(API_STATUS_TXT)) {
                    status_txt += ": " + api.getString(API_STATUS_TXT);
                    ((TextView) findViewById(R.id.status_txt))
                            .setText(status_txt);
                }
                // Status icon
                JSONObject status_icon = api.getJSONObject(API_ICON);
                if (!status_icon.isNull(status)) {
                    new GetImage(R.id.status_image).execute(status_icon
                            .getString(status));
                }
                // Location
                if (!api.isNull(API_ADDRESS)) {
                    ((TextView) findViewById(R.id.location_address))
                            .setText(api.getString(API_ADDRESS));
                }
                if (!api.isNull(API_LON) && !api.isNull(API_LAT)) {
                    ((TextView) findViewById(R.id.location_map))
                            .setText(api.getString(API_LON) + ", "
                                    + api.getString(API_LAT));
                }
            } catch (JSONException e) {
                mErrorMsg = e.getLocalizedMessage();
            } finally {
                dismissDialog(DIALOG_LOADING);
                showError();
            }
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