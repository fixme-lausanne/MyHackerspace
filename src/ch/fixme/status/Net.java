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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.os.Build;

public class Net {

    private final String USERAGENT = "Android/" + Build.VERSION.RELEASE + " ("
            + Build.MODEL + ") MyHackerspace/1.6.1";
    private static final int BUFSIZE = 2 * 1024;

    public Net(String urlStr, OutputStream out)
            throws NoSuchAlgorithmException, KeyManagementException,
            SSLHandshakeException, SSLException, IOException {

        // HTTP connection reuse which was buggy pre-froyo
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        // Accept all SSL certificates
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Connect and get data
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        try {
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestProperty("User-Agent", USERAGENT);
            BufferedInputStream in = new BufferedInputStream(
                    urlConnection.getInputStream(), BUFSIZE);
            byte[] buffer = new byte[BUFSIZE];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                out.write(buffer, 0, len1);
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    } };
}
