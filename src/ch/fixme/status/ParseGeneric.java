/*
 * Copyright (C) 2013 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 3, see README
 */
package ch.fixme.status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ParseGeneric {
    protected HashMap<String, Object> mResult = new HashMap<String, Object>();
    protected JSONObject mApi;

    protected static final String API_DIRECTORY = "http://spaceapi.net/directory.json";
    protected static final String API_NAME = "space";
    protected static final String API_LON = "lon";
    protected static final String API_LOCATION = "location";
    protected static final String API_LAT = "lat";
    protected static final String API_URL = "url";
    protected static final String API_LEVEL = "api";
    protected static final String API_STATE = "state";
    protected static final String API_STATE_MESSAGE = "message";
    protected static final String API_STATUS_TXT = "status";
    protected static final String API_DURATION = "duration";
    protected static final String API_EXT_DURATION = "ext_duration";
    protected static final String API_ADDRESS = "address";
    protected static final String API_CONTACT = "contact";
    protected static final String API_EMAIL = "email";
    protected static final String API_IRC = "irc";
    protected static final String API_JABBER = "jabber";
    protected static final String API_PHONE = "phone";
    protected static final String API_SIP = "sip";
    protected static final String API_TWITTER = "twitter";
    protected static final String API_IDENTICA = "identica";
    protected static final String API_FOURSQUARE = "foursquare";
    protected static final String API_ML = "ml";
    protected static final String API_STREAM = "stream";
    protected static final String API_CAM = "cam";
    protected static final String API_SENSORS = "sensors";
    protected static final String API_EXT = "ext_";
    protected static final String API_RADIATION = "radiation";

    // Sensors
    protected static final String API_VALUE = "value";
    protected static final String API_UNIT = "unit";
    protected static final String API_LOCATION2 = "location";
    protected static final String API_NAME2 = "name";
    protected static final String API_DESCRIPTION = "description";
    protected static final String API_MACHINES = "machines";
    protected static final String API_NAMES = "names";
    protected static final String API_PROPERTIES = "properties";

    // State
    protected static final String API_DEFAULT = "https://fixme.ch/status.json";
    protected static final String API_ICON = "icon";
    protected static final String API_ICON_OPEN = "open";
    protected static final String API_ICON_CLOSED = "closed";
    protected static final String API_LOGO = "logo";
    protected static final String API_STATUS = "open";
    protected static final String API_LASTCHANGE = "lastchange";

    public ParseGeneric(JSONObject jsonObject) {
        mApi = jsonObject;
    }

    public ParseGeneric(String jsonString) throws JSONException {
        mApi = new JSONObject(jsonString);
    }

    public HashMap<String, Object> getData() throws JSONException {
        if ("0.13".equals(mApi.getString(ParseGeneric.API_LEVEL))) {
            mResult = new Parse13(mApi).parse();
        } else if ("0.12".equals(mApi.getString(ParseGeneric.API_LEVEL))) {
            mResult = new Parse12(mApi).parse();
        } else {
            throw new JSONException("API LEVEL NOT SUPPORTED: "
                    + mApi.getString(ParseGeneric.API_LEVEL));
        }
        return mResult;
    }

}
