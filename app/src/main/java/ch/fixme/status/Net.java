/*
 * Copyright (C) 2012-2016 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 3, see README
 */

package ch.fixme.status;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;

import de.duenndns.ssl.MemorizingTrustManager;

// From CommonsWare and Android Blog
// https://github.com/commonsguy/cw-android/tree/master/Internet
// http://android-developers.blogspot.ch/2010/07/multithreading-for-performance.html
public class Net {

    private final String USERAGENT = "Android/" + Build.VERSION.RELEASE + " ("
            + Build.MODEL + ") MyHackerspace/1.8.1";

    private HttpURLConnection mUrlConnection;
    private InputStream mInputStream;
    private Context mCtxt;

    public Net(String urlStr, Context ctxt) throws Throwable {
        this(urlStr, true, ctxt);
    }

    public Net(String urlStr, boolean useCache, Context ctxt) throws Throwable {
        mCtxt = ctxt;
        // register MemorizingTrustManager for HTTPS
        SSLContext sc = SSLContext.getInstance("TLS");
        MemorizingTrustManager mtm = new MemorizingTrustManager(mCtxt);
        sc.init(null, new X509TrustManager[] { mtm }, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(
            mtm.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));

        // Create client

        // Connect to URL
        URL url;
        int responseCode;
        int redirect_limt = 10;
        do {
            Log.v(Main.TAG, "fetching " + urlStr);
            url = new URL(urlStr);
            mUrlConnection = (HttpURLConnection) url.openConnection();
            mUrlConnection.setRequestProperty("User-Agent", USERAGENT);
            mUrlConnection.setUseCaches(useCache);

            mUrlConnection.connect();
            responseCode = mUrlConnection.getResponseCode();

            // HttpsURLConnection does not support redirect with protocol switch,
            // so we take care of that here:
            if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP
            || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                urlStr = mUrlConnection.getHeaderField("Location");
                redirect_limt -= 1;
            } else {
                break;
            }
        } while(redirect_limt > 0);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            String msg = mUrlConnection.getResponseMessage();
            mUrlConnection.disconnect();
            throw new Throwable(msg);
        }

        mInputStream = mUrlConnection.getInputStream();
    }

    public String getString() throws Throwable {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(mInputStream));
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                str.append(line);
            }
            return str.toString();
        } finally {
            if (mInputStream != null) {
                mInputStream.close();
            }
            mUrlConnection.disconnect();
        }
    }

    public Bitmap getBitmap() throws Throwable {
        try {
            return BitmapFactory.decodeStream(new FlushedInputStream(mInputStream));
        } finally {
            if (mInputStream != null) {
                mInputStream.close();
            }
            mUrlConnection.disconnect();
        }
    }

    public static void setCache(Context ctxt) {
        try {
            File httpCacheDir = new File(ctxt.getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            // Use reflection for Android < 4.0
            Class.forName("android.net.http.HttpResponseCache")
                .getMethod("install", File.class, long.class)
                .invoke(null, httpCacheDir, httpCacheSize);
        } catch (Exception e) {
            Log.e(Main.TAG, e.getMessage());
        }
    }

    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

}
