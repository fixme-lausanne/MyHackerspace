package ch.fixme.status;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class Map extends Activity {

	private MapView mMapView;
	private MyItemizedOverlay mMarkers;
	private ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
	private String mHs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMapView = new MapView(this, 256);
		mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
		setContentView(mMapView);
		mMapView.getController().setZoom(2);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras.containsKey(Main.STATE_DIR)
				&& extras.containsKey(Main.STATE_HS)) {
			final String dir = extras.getString(Main.STATE_DIR);
			mHs = extras.getString(Main.STATE_HS);
			getHackerspacesMarker(dir);
		} else {
			Log.e(Main.TAG, "Error loading list");
			finish();
		}

	}

	private void getHackerspacesMarker(String dir) {
		mMarkers = new MyItemizedOverlay();
		mMapView.getOverlays().add(mMarkers);
		final ArrayList<String> urls = getHsUrl(dir);
		for (String url : urls) {
			new GetApiTask().execute(url);
		}
	}

	private ArrayList<String> getHsUrl(String json) {
		ArrayList<String> url = new ArrayList<String>();
		try {
			JSONObject obj = new JSONObject(json);
			JSONArray arr = obj.names();
			int len = obj.length();
			String[] names = new String[len];
			for (int i = 0; i < len; i++) {
				names[i] = arr.getString(i);
			}
			url = new ArrayList<String>(len);
			for (int i = 0; i < len; i++) {
				url.add(i, obj.getString(names[i]));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return url;
	}

	private class GetApiTask extends AsyncTask<String, Void, String> {

		private String mUrl = "";

		@Override
		protected String doInBackground(String... url) {
			mUrl = url[0];
			ByteArrayOutputStream spaceOs = new ByteArrayOutputStream();
			try {
				new Net(mUrl, spaceOs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return spaceOs.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				JSONObject api = new JSONObject(result);
				if (!api.isNull(Main.API_LAT) && !api.isNull(Main.API_LON)) {
					GeoPoint pt = new GeoPoint(Double.parseDouble(api
							.getString(Main.API_LAT)), Double.parseDouble(api
							.getString(Main.API_LON)));
					OverlayItem marker = new OverlayItem(
							api.getString(Main.API_NAME), "", pt);
					mMarkers.addMarker(marker);
                    if (!api.isNull(Main.API_ICON)) {
					    JSONObject status_icon = api.getJSONObject(Main.API_ICON);
                        String icon = status_icon.getString(Main.API_ICON_CLOSED);
                        if (api.getBoolean(Main.API_STATUS)) {
                            icon = status_icon.getString(Main.API_ICON_OPEN);
                        }
                        new GetImage(marker).execute(icon);
                    }
					if (mHs.equals(mUrl)) {
						mMapView.getController().setCenter(pt);
						mMapView.getController().setZoom(8);
					}
					mMapView.invalidate();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onCancelled() {
		}
	}

	private class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {

		public MyItemizedOverlay() {
			super(getResources().getDrawable(R.drawable.myhs),
					new DefaultResourceProxyImpl(getApplicationContext()));
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mItems.get(i);
		}

		@Override
		public int size() {
			return mItems.size();
		}

		@Override
		public boolean onSnapToItem(int arg0, int arg1, Point arg2,
				IMapView arg3) {
			return false;
		}

		public void addMarker(OverlayItem overlayItem) {
			mItems.add(overlayItem);
			populate();
		}
	}

	private static class GetImage extends AsyncTask<String, Void, byte[]> {

		private OverlayItem mMarker;

		public GetImage(OverlayItem marker) {
            mMarker = marker;
		}

		@Override
		protected byte[] doInBackground(String... url) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				Log.i(Main.TAG, "Get image from url " + url[0]);
				new Net(url[0], os);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return os.toByteArray();
		}

		@Override
		protected void onPostExecute(byte[] result) {
            mMarker.setMarker(new BitmapDrawable(BitmapFactory.decodeByteArray(result, 0, result.length)));
			//mMapView.invalidate();
		}

	}

}
