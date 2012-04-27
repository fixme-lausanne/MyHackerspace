package ch.fixme.status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		String apiUrl = PreferenceManager
				.getDefaultSharedPreferences(Main.this).getString(API_KEY,
						API_DEFAULT);
		new GetApiTask().execute(apiUrl, API_DIRECTORY);
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

	private class GetApiTask extends AsyncTask<String, Void, String[]> {

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_LOADING);
		}

		@Override
		protected String[] doInBackground(String... url) {
			// Download the current selected space api and the api directory
			ByteArrayOutputStream spaceOs = new ByteArrayOutputStream();
			ByteArrayOutputStream direcOs = new ByteArrayOutputStream();
			try {
				new Net(url[0], spaceOs);
				new Net(url[1], direcOs);
			} catch (SSLException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new String[] { spaceOs.toString(), direcOs.toString() };
		}

		@Override
		protected void onPostExecute(String[] result) {
			try {
				// Display current hackerspace information
				JSONObject api = new JSONObject(result[0]);
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
				// Construct hackerspaces list
				Spinner s = (Spinner) findViewById(R.id.choose);
				JSONObject obj = new JSONObject(result[1]);
				String[] arr = new String[obj.length()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = (String) obj.names().get(i);
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						Main.this, android.R.layout.simple_spinner_item, arr);
				adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
				s.setAdapter(adapter);
				s.setSelection(0);
				s.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> adapter, View v,
							int position, long id) {
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
			dismissDialog(DIALOG_LOADING);
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