package ch.fixme.status;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseGeneric {
	protected HashMap<String, Object> mResult = new HashMap<String, Object>();
	protected JSONObject mApi;

	protected static final String API_DIRECTORY = "http://spaceapi.net/directory.json";
	protected static final String API_NAME = "space";
	protected static final String API_LON = "lon";
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
	protected static final String API_PHONE = "phone";
	protected static final String API_TWITTER = "twitter";
	protected static final String API_ML = "ml";
	protected static final String API_STREAM = "stream";
	protected static final String API_CAM = "cam";

	protected static final String API_DEFAULT = "https://fixme.ch/cgi-bin/spaceapi.py";
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
		if ("0.12".equals(mApi.getString(ParseGeneric.API_LEVEL))) {
			mResult = new Parse12(mApi).parse();
		} else {
			throw new JSONException("API LEVEL NOT SUPPORTED: "
					+ mApi.getString(ParseGeneric.API_LEVEL));
		}
		return mResult;
	}

}
