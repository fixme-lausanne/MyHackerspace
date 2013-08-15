package ch.fixme.status;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Parse12 extends ParseGeneric {

	public Parse12(JSONObject jsonObject) throws JSONException {
		super(jsonObject);
	}

	protected HashMap<String, Object> parse() throws JSONException {

		// Mandatory fields
		mResult.put(API_STATUS, mApi.getBoolean(API_STATUS));
		mResult.put(API_NAME, mApi.getString(API_NAME));
		mResult.put(API_URL, mApi.getString(API_URL));
		mResult.put(API_LOGO, mApi.getString(API_LOGO));

		// Status icons
		JSONObject icons = mApi.getJSONObject(API_ICON);
		mResult.put(API_ICON + API_ICON_OPEN, icons.getString(API_ICON_OPEN));
		mResult.put(API_ICON + API_ICON_CLOSED,
				icons.getString(API_ICON_CLOSED));

		// Status text
		if (!mApi.isNull(API_STATUS_TXT)) {
			mResult.put(API_STATUS_TXT, mApi.getString(API_STATUS_TXT));
		}

		// Last change date
		if (!mApi.isNull(API_LASTCHANGE)) {
			Date date = new Date(mApi.getLong(API_LASTCHANGE) * 1000);
			DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
			mResult.put(API_LASTCHANGE, formatter.format(date));
		}

		// Duration (FIXME addition)
		if (!mApi.isNull(API_DURATION)) {
			mResult.put(API_EXT_DURATION, mApi.getString(API_DURATION));
		} else if (!mApi.isNull(API_EXT_DURATION)) {
			mResult.put(API_EXT_DURATION, mApi.getString(API_EXT_DURATION));
		}

		// Location
		if (!mApi.isNull(API_LON) && !mApi.isNull(API_LAT)) {
			mResult.put(API_LON, mApi.getString(API_LON));
			mResult.put(API_LAT, mApi.getString(API_LAT));
		}
		if (!mApi.isNull(API_ADDRESS)) {
			mResult.put(API_ADDRESS, mApi.getString(API_ADDRESS));
		}

		// Contact
		if (!mApi.isNull(API_CONTACT)) {
			JSONObject contact = mApi.getJSONObject(API_CONTACT);

			// Phone
			if (!contact.isNull(API_PHONE)) {
				mResult.put(API_PHONE, contact.getString(API_PHONE));
			}
			// Twitter
			if (!contact.isNull(API_TWITTER)) {
				mResult.put(API_TWITTER, contact.getString(API_TWITTER));
			}
			// IRC
			if (!contact.isNull(API_IRC)) {
				mResult.put(API_IRC, contact.getString(API_IRC));
			}
			// Email
			if (!contact.isNull(API_EMAIL)) {
				mResult.put(API_EMAIL, contact.getString(API_EMAIL));
			}
			// Mailing-List
			if (!contact.isNull(API_ML)) {
				mResult.put(API_ML, contact.getString(API_ML));
			}
		}

		if (!mApi.isNull(API_STREAM) || !mApi.isNull(API_CAM)) {
			// Stream
			if (!mApi.isNull(API_STREAM)) {
				JSONObject stream = mApi.optJSONObject(API_STREAM);
				if (stream != null) {
					HashMap<String, String> streamMap = new HashMap<String, String>(
							stream.length());
					JSONArray names = stream.names();
					for (int i = 0; i < stream.length(); i++) {
						final String type = names.getString(i);
						final String url = stream.getString(type);
						streamMap.put(type, url);
					}
					mResult.put(API_STREAM, streamMap);
				}
			}
			// Cam
			if (!mApi.isNull(API_CAM)) {
				JSONArray cam = mApi.optJSONArray(API_CAM);
				if (cam != null) {
					HashMap<String, String> camMap = new HashMap<String, String>(
							cam.length());
					for (int i = 0; i < cam.length(); i++) {
						camMap.put("http", cam.getString(i));
					}
				}
			}
		}

		return mResult;
	}

}
