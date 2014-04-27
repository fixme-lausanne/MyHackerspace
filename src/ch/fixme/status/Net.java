/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 3, see README
 */

package ch.fixme.status;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import at.bitfire.davdroid.webdav.TlsSniSocketFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// From CommonsWare and Android Blog
// https://github.com/commonsguy/cw-android/tree/master/Internet
// http://android-developers.blogspot.ch/2010/07/multithreading-for-performance.html
public class Net {

    private final String USERAGENT = "Android/" + Build.VERSION.RELEASE + " ("
            + Build.MODEL + ") MyHackerspace/1.7.1";
    final private HttpClient client;
    final private HttpGet getMethod;

    public Net(String urlStr) {
        client = new DefaultHttpClient();
        client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new TlsSniSocketFactory(), 443));
        getMethod = new HttpGet(urlStr);
        getMethod.setHeader("User-Agent", USERAGENT);
    }

    public String getString() throws Throwable {
        HttpResponse response = client.execute(getMethod);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream is = null;
                try {
                    is = entity.getContent();
                    BufferedReader r = new BufferedReader(
                            new InputStreamReader(is));
                    StringBuilder str = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        str.append(line);
                    }
                    return str.toString();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    entity.consumeContent();
                }
            }
        }
        return "{}";
    }

    public Bitmap getBitmap() throws Throwable {
        HttpResponse response = client.execute(getMethod);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream is = null;
                try {
                    is = entity.getContent();
                    return BitmapFactory
                            .decodeStream(new FlushedInputStream(is));
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    entity.consumeContent();
                }
            }
        }
        return null;
    }

    public void stop() {
        client.getConnectionManager().shutdown();
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
