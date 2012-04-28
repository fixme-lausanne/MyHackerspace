/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package ch.fixme.status;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class Net {

    private static String TAG = "DownloadFile";
    private final String USERAGENT = "Android/"
            + android.os.Build.VERSION.RELEASE + " (" + android.os.Build.MODEL
            + ") MyHackerspace/";
    private HttpClient httpclient;

    public Net(String url, OutputStream out) throws SSLException, IOException,
            NullPointerException {
        httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter("http.useragent", USERAGENT);
        InputStream in = openURL(url);
        if (in == null) {
            Log.e(TAG, "Unable to download: " + url);
            return;
        }

        final ReadableByteChannel inputChannel = Channels.newChannel(in);
        final WritableByteChannel outputChannel = Channels.newChannel(out);

        try {
            Log.i(TAG, "Downloading " + url);
            fastChannelCopy(inputChannel, outputChannel);
        } finally {
            try {
                if (inputChannel != null) {
                    inputChannel.close();
                }
                if (outputChannel != null) {
                    outputChannel.close();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                if (e != null && e.getMessage() != null) {
                    Log.e(TAG, e.getMessage());
                } else {
                    Log.e(TAG, "fastChannelCopy() unknown error");
                }
            }
        }
    }

    private InputStream openURL(String url) throws ClientProtocolException,
            IOException {
        HttpGet httpget = new HttpGet(url);
        HttpResponse response;
        // try {
        response = httpclient.execute(httpget);
        // } catch (SSLException e) {
        // /*
        // * KeyStore trustStore =
        // * KeyStore.getInstance(KeyStore.getDefaultType());
        // * KeyStore.getDefaultType(); FileInputStream in = new
        // * FileInputStream(new
        // * File("data/data/ch.fixme.status/my.trustore3")); try {
        // * trustStore.load(in, "coucou".toCharArray()); } finally {
        // * in.close(); }
        // *
        // * SSLSocketFactory socketFactory = new
        // * SSLSocketFactory(trustStore); SchemeRegistry registry = new
        // * SchemeRegistry(); registry.register(new Scheme("https",
        // * socketFactory, 443));
        // */
        // response = httpclient.execute(httpget);
        // }
        Log.i(TAG, "Status:[" + response.getStatusLine().toString() + "]");
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            return entity.getContent();
        }

        return null;
    }

    public static void fastChannelCopy(final ReadableByteChannel src,
            final WritableByteChannel dest) throws IOException,
            NullPointerException {
        if (src != null && dest != null) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
            while (src.read(buffer) != -1) {
                buffer.flip();
                dest.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                dest.write(buffer);
            }
        }
    }

}
