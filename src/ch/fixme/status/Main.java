package ch.fixme.status;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Main extends Activity {

	private static final int DIALOG_LOADING = 0;
	private static final String API_KEY = "apiurl";
	private static final String API_DEFAULT = "https://fixme.ch/cgi-bin/spaceapi.py";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		String apiUrl = PreferenceManager
				.getDefaultSharedPreferences(Main.this).getString(API_KEY,
						API_DEFAULT);

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
	
    private class GetAPITask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_LOADING);
        }

        @Override
        protected String doInBackground(String... url) {
        	return "";
        }

        @Override
        protected void onPostExecute(String result) {
        	// Do something with result
            dismissDialog(DIALOG_LOADING);
        }

    }

}