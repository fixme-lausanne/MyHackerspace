/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

import android.preference.PreferenceActivity;
import android.os.Bundle;
import ch.fixme.status.R;

public class Prefs extends PreferenceActivity{

    public static final String KEY_CHECK_INTERVAL = "check_interval";
    public static final int DEFAULT_CHECK_INTERVAL = 15; //15mn

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
