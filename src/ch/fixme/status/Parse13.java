package ch.fixme.status;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

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

		// Status icons
		if (!state.isNull(API_ICON)) {
			JSONObject icon = state.getJSONObject(API_ICON);
			mResult.put(API_ICON + API_ICON_OPEN, icon.getString(API_ICON_OPEN));
			mResult.put(API_ICON + API_ICON_CLOSED,
					icon.getString(API_ICON_CLOSED));
		}

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
		if (!state.isNull(API_EXT_DURATION)) {
			mResult.put(API_EXT_DURATION, state.getString(API_EXT_DURATION));
		}

		// Location (Mandatory)
		if (!mApi.isNull(API_LOCATION)) {
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

		// Sensors
		if (!mApi.isNull(API_SENSORS)) {
			JSONObject sensors = mApi.getJSONObject(API_SENSORS);
			JSONArray names = sensors.names();
			JSONArray elem;
            ArrayList<HashMap<String, String>> elem_value;
            HashMap<String, ArrayList<HashMap<String, String>>> result = new HashMap<String, ArrayList<HashMap<String, String>>>(
                    sensors.length());
			for (int i = 0; i < names.length(); i++) {
				elem = sensors.getJSONArray(names.getString(i));
				elem_value = new ArrayList<HashMap<String, String>>(elem.length());
				for (int j = 0; j < elem.length(); j++) {
                    HashMap<String, String> elem_value_map = new HashMap<String, String>();
					try {
						JSONObject obj = (JSONObject) elem.get(j);
						if (!obj.isNull(API_VALUE)
								&& !"".equals(obj.getString(API_VALUE))) {
                            elem_value_map.put(API_VALUE, obj.getString(API_VALUE));
						}
						if (!obj.isNull(API_UNIT)
								&& !"".equals(obj.getString(API_UNIT))) {
                            elem_value_map.put(API_UNIT, obj.getString(API_UNIT));
						}
						if (!obj.isNull(API_NAME2)
								&& !"".equals(obj.getString(API_NAME2))) {
                            elem_value_map.put(API_NAME2, obj.getString(API_NAME2));
						}
						if (!obj.isNull(API_LOCATION2)
								&& !"".equals(obj.getString(API_LOCATION2))) {
                            elem_value_map.put(API_LOCATION2, obj.getString(API_LOCATION2));
						}
						if (!obj.isNull(API_DESCRIPTION)
								&& !"".equals(obj.getString(API_DESCRIPTION))) {
                            elem_value_map.put(API_DESCRIPTION, obj.getString(API_DESCRIPTION));
						}
                        if (!obj.isNull(API_MACHINES) && obj.getJSONArray(API_MACHINES).length() > 0) {
                            elem_value_map.put(API_MACHINES, obj.get(API_MACHINES).toString());
                        }
                        if (!obj.isNull(API_NAMES) && obj.getJSONArray(API_NAMES).length() > 0) {
                            elem_value_map.put(API_NAMES, obj.get(API_NAMES).toString());
                        }
                        if (!obj.isNull(API_PROPERTIES)) {

                            JSONObject obj2 = obj.getJSONObject(API_PROPERTIES);
                            String prop = "";
                            for (int k = 0; k < obj2.length(); k++){
                                String name = (String) obj2.names().get(k);
                                JSONObject obj3 = obj2.getJSONObject(name);
                                prop += name + ": " + obj3.getString(API_VALUE) + " " + obj3.getString(API_UNIT) + ", ";
                            }
                            elem_value_map.put(API_PROPERTIES, prop.substring(0, prop.length() - 2));
                        }
					} catch (Exception e) {
						Log.e(Main.TAG, e.getMessage());
                        elem_value_map.put(API_VALUE, elem.get(j).toString());
					}
                    elem_value.add(elem_value_map);
				}
				result.put(names.getString(i), elem_value);
			}
			mResult.put(API_SENSORS, result);
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
