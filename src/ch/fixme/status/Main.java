package ch.fixme.status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

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

    public static final String PKG = "ch.fixme.status";
    private static final String API_DIRECTORY = "http://openspace.slopjong.de/directory.json";
    private static final String API_KEY = "apiurl";
    private static final String API_DEFAULT = "https://fixme.ch/cgi-bin/spaceapi.py";
    private static final String API_NAME = "space";
    private static final String API_STATUS = "open";
    private static final String API_STATUS_TXT = "status";
    private static final String API_ICON = "icon";
    private static final String API_ICON_OPEN = "open";
    private static final String API_ICON_CLOSED = "closed";
    private static final int DIALOG_LOADING = 0;

    private SharedPreferences mPrefs;
    private String mApiUrl;

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
        }
        return dialog;
    }

    private class GetDirTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
        }

        @Override
        protected String doInBackground(String... url) {
            ByteArrayOutputStream direcOs = new ByteArrayOutputStream();
            try {
                new Net(url[0], direcOs);
            } catch (SSLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
            }
        }
    }

    private class GetApiTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
            // Clean UI
            ((TextView) findViewById(R.id.name)).setText("");
            ((TextView) findViewById(R.id.status)).setText("");
            ((ImageView) findViewById(R.id.image)).setImageBitmap(null);
        }

        @Override
        protected String doInBackground(String... url) {
            ByteArrayOutputStream spaceOs = new ByteArrayOutputStream();
            try {
                new Net(url[0], spaceOs);
            } catch (SSLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return spaceOs.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Display current hackerspace information
                JSONObject api = new JSONObject(result);
                String status = API_ICON_CLOSED;
                if (api.getBoolean(API_STATUS)) {
                    status = API_ICON_OPEN;
                }
                new GetImage().execute(api.getJSONObject(API_ICON).getString(
                        status));
                ((TextView) findViewById(R.id.name)).setText(api
                        .getString(API_NAME));
                ((TextView) findViewById(R.id.status)).setText(api
                        .getString(API_STATUS_TXT));
                findViewById(R.id.image).setBackgroundColor(0);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                dismissDialog(DIALOG_LOADING);
            }
        }
    }

    private class GetImage extends AsyncTask<String, Void, byte[]> {

        @Override
        protected byte[] doInBackground(String... url) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                new Net(url[0], os);
            } catch (SSLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return os.toByteArray();
        }

        @Override
        protected void onPostExecute(byte[] result) {
            ((ImageView) findViewById(R.id.image)).setImageBitmap(BitmapFactory
                    .decodeByteArray(result, 0, result.length));
        }

    }

}