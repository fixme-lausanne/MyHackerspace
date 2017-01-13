/*
 * Copyright (C) 2012-2017 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 3, see README
 */
package ch.fixme.status;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class Parse13 extends ParseGeneric {

    public Parse13(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    protected HashMap<String, Object> parse() throws JSONException {

        // Mandatory fields
        JSONObject state = mApi.getJSONObject(API_STATE);
        if (!state.isNull(API_STATUS)){
            mResult.put(API_STATUS, state.getBoolean(API_STATUS));
        } else {
            mResult.put(API_STATUS, null);
        }
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
                String sensor_name = names.getString(i);
                if (sensor_name.startsWith(API_EXT) || sensor_name.startsWith(API_RADIATION)) {
                    continue;
                }
                elem = sensors.getJSONArray(sensor_name);
                elem_value = new ArrayList<HashMap<String, String>>(elem.length());
                for (int j = 0; j < elem.length(); j++) {
                    HashMap<String, String> elem_value_map = new HashMap<String, String>();
                    try {
                        JSONObject obj = (JSONObject) elem.get(j);
                        if (!obj.isNull(API_SENSOR_VALUE)
                                && !"".equals(obj.getString(API_SENSOR_VALUE))) {
                            elem_value_map.put(API_SENSOR_VALUE, obj.getString(API_SENSOR_VALUE));
                        }
                        if (!obj.isNull(API_SENSOR_UNIT)
                                && !"".equals(obj.getString(API_SENSOR_UNIT))) {
                            elem_value_map.put(API_SENSOR_UNIT, obj.getString(API_SENSOR_UNIT));
                        }
                        if (!obj.isNull(API_SENSOR_NAME)
                                && !"".equals(obj.getString(API_SENSOR_NAME))) {
                            elem_value_map.put(API_SENSOR_NAME, obj.getString(API_SENSOR_NAME));
                        }
                        if (!obj.isNull(API_SENSOR_LOCATION)
                                && !"".equals(obj.getString(API_SENSOR_LOCATION))) {
                            elem_value_map.put(API_SENSOR_LOCATION, obj.getString(API_SENSOR_LOCATION));
                        }
                        if (!obj.isNull(API_SENSOR_DESCRIPTION)
                                && !"".equals(obj.getString(API_SENSOR_DESCRIPTION))) {
                            elem_value_map.put(API_SENSOR_DESCRIPTION, obj.getString(API_SENSOR_DESCRIPTION));
                        }
                        if (!obj.isNull(API_SENSOR_MACHINES) && obj.getJSONArray(API_SENSOR_MACHINES).length() > 0) {
                            elem_value_map.put(API_SENSOR_MACHINES, obj.get(API_SENSOR_MACHINES).toString());
                        }
                        if (!obj.isNull(API_SENSOR_NAMES) && obj.getJSONArray(API_SENSOR_NAMES).length() > 0) {
                            elem_value_map.put(API_SENSOR_NAMES, obj.get(API_SENSOR_NAMES).toString());
                        }
                        if (!obj.isNull(API_SENSOR_PROPERTIES)) {

                            JSONObject obj2 = obj.getJSONObject(API_SENSOR_PROPERTIES);
                            String prop = "";
                            for (int k = 0; k < obj2.length(); k++) {
                                String name = (String) obj2.names().get(k);
                                JSONObject obj3 = obj2.getJSONObject(name);
                                prop += name + ": " + obj3.getString(API_SENSOR_VALUE) + " " + obj3.getString(API_SENSOR_UNIT) + ", ";
                            }
                            elem_value_map.put(API_SENSOR_PROPERTIES, prop.substring(0, prop.length() - 2));
                        }
                    } catch (Exception e) {
                        Log.e(Main.TAG, e.getMessage());
                        elem_value_map.put(API_SENSOR_VALUE, elem.get(j).toString());
                    }
                    elem_value.add(elem_value_map);
                }
                result.put(sensor_name, elem_value);
            }
            mResult.put(API_SENSORS, result);
        }

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
                ArrayList<String> camList = new ArrayList<String>(cam.length());
                for (int i = 0; i < cam.length(); i++) {
                    camList.add(cam.getString(i));
                }
                mResult.put(API_CAM, camList);
            }
        }

        return mResult;
    }
}
