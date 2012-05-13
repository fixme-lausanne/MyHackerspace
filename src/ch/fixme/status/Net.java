/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package ch.fixme.status;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Build;

public class Net {

    private final String USERAGENT = "Android/" + Build.VERSION.RELEASE + " ("
            + Build.MODEL + ") MyHackerspace/1.4";

    public Net(String urlStr, OutputStream out) throws IOException {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        try {
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestProperty("User-Agent", USERAGENT);
            BufferedInputStream in = new BufferedInputStream(
                    urlConnection.getInputStream());
            byte[] buffer = new byte[2 * 1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                out.write(buffer, 0, len1);
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
