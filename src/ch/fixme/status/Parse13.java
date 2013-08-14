package ch.fixme.status;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Parse13 extends ParseGeneric {

	public Parse13(JSONObject jsonObject) throws JSONException {
		super(jsonObject);
	}

	protected HashMap<String, Object> parse() throws JSONException {

		// Mandatory fields
        JSONObject state = mApi.getJSONObject(API_STATE);
		mResult.put(API_STATUS, state.getBoolean(API_STATUS));
		mResult.put(API_NAME, mApi.getString(API_NAME));
		mResult.put(API_URL, mApi.getString(API_URL));
		mResult.put(API_LOGO, mApi.getString(API_LOGO));

		// Status text
		if (!state.isNull(API_STATE_MESSAGE)) {
			mResult.put(API_STATUS_TXT, state.getString(API_STATE_MESSAGE));
		}

		// Last change date
		if (!state.isNull(API_LASTCHANGE)) {
			Date date = new Date(state.getLong(API_LASTCHANGE) * 1000);
			DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
			mResult.put(API_LASTCHANGE, formatter.format(date));
		}

		// Duration (FIXME addition)
		if (!mApi.isNull(API_DURATION)) {
			mResult.put(API_EXT_DURATION, mApi.getString(API_DURATION));
		} else if (!mApi.isNull(API_EXT_DURATION)) {
			mResult.put(API_EXT_DURATION, mApi.getString(API_EXT_DURATION));
		}

		// Location (Mandatory)
        if (!mApi.isNull(API_LOCATION)){
            JSONObject loc = mApi.getJSONObject(API_LOCATION);
            if (!loc.isNull(API_LON) && !loc.isNull(API_LAT)) {
                mResult.put(API_LON, loc.getString(API_LON));
                mResult.put(API_LAT, loc.getString(API_LAT));
            }
            if (!loc.isNull(API_ADDRESS)) {
                mResult.put(API_ADDRESS, loc.getString(API_ADDRESS));
            }
        }

		// Contact
		if (!mApi.isNull(API_CONTACT)) {
			JSONObject contact = mApi.getJSONObject(API_CONTACT);

			// Phone
			if (!contact.isNull(API_PHONE)) {
				mResult.put(API_PHONE, contact.getString(API_PHONE));
			}
			// SIP
			if (!contact.isNull(API_SIP)) {
				mResult.put(API_SIP, contact.getString(API_SIP));
			}
			// Twitter
			if (!contact.isNull(API_TWITTER)) {
				mResult.put(API_TWITTER, contact.getString(API_TWITTER));
			}
			// Identica
			if (!contact.isNull(API_IDENTICA)) {
				mResult.put(API_IDENTICA, contact.getString(API_IDENTICA));
			}
			// Foursquare
			if (!contact.isNull(API_FOURSQUARE)) {
				mResult.put(API_FOURSQUARE, contact.getString(API_FOURSQUARE));
			}
			// IRC
			if (!contact.isNull(API_IRC)) {
				mResult.put(API_IRC, contact.getString(API_IRC));
			}
			// Jabber
			if (!contact.isNull(API_JABBER)) {
				mResult.put(API_JABBER, contact.getString(API_JABBER));
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
