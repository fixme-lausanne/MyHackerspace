/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package ch.fixme.status;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class Widget_config extends Activity {

	private static final int DIALOG_LOADING = 0;
	private SharedPreferences mPrefs;
	private GetDirTask getDirTask;
	private int mAppWidgetId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_config);
		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(Widget_config.this);
		getDirTask = new GetDirTask();
		getDirTask.execute(Main.API_DIRECTORY);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		findViewById(R.id.choose_ok).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						setWidgetAlarm();
						finish();
					}
				});
		((EditText) findViewById(R.id.choose_update))
				.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						String inter = s.toString();
						if (!"".equals(inter) && !"0".equals(inter)) {
							Editor edit = mPrefs.edit();
							edit.putString(Prefs.KEY_CHECK_INTERVAL, inter);
							edit.commit();
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
					}
				});
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
		}
		return dialog;
	}

	private void setWidgetAlarm() {
		Context ctxt = getApplicationContext();
		Intent i = Widget.getIntent(ctxt, mAppWidgetId);
		setResult(RESULT_OK, i);
		Widget.setAlarm(ctxt, i, mAppWidgetId);
	}

	public class GetDirTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_LOADING);
		}

		@Override
		protected String doInBackground(String... url) {
			ByteArrayOutputStream direcOs = new ByteArrayOutputStream();
			try {
				new Net(url[0], direcOs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return direcOs.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			// Construct hackerspaces list
			Spinner s = (Spinner) findViewById(R.id.choose_hs);
			try {
				JSONObject obj = new JSONObject(result);
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
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
						Widget_config.this,
						android.R.layout.simple_spinner_item, names);
				adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
				s.setAdapter(adapter);
				s.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> adapter, View v,
							int position, long id) {
						Editor edit = mPrefs.edit();
						edit.putString(Main.PREF_API_URL_WIDGET + mAppWidgetId,
								url.get(position));
						edit.commit();
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}

			removeDialog(DIALOG_LOADING);
		}

		@Override
		protected void onCancelled() {
			removeDialog(DIALOG_LOADING);
		}
	}

}
