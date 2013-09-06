/*
 * Copyright (C) 2012 Aubort Jean-Baptiste (Rorist)
 * Licensed under GNU's GPL 2, see README
 */

package ch.fixme.status;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

// From CommonsWare and Android Blog
// https://github.com/commonsguy/cw-android/tree/master/Internet
// http://android-developers.blogspot.ch/2010/07/multithreading-for-performance.html
public class Net {

	private final String USERAGENT = "Android/" + Build.VERSION.RELEASE + " ("
			+ Build.MODEL + ") MyHackerspace/1.7";
	final private HttpClient client;
	final private HttpGet getMethod;

	public Net(String urlStr) {
		client = new DefaultHttpClient();
		getMethod = new HttpGet(urlStr);
		getMethod.setHeader("User-Agent", USERAGENT);
	}

	public String getString() {
		try {
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
		} catch (Throwable t) {
			Log.e(Main.TAG, "Exception fetching data", t);
		}
		return "";
	}

	public Bitmap getBitmap() {
		try {
			HttpResponse response = client.execute(getMethod);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				final HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream is = null;
					try {
						is = entity.getContent();
						final Bitmap bitmap = BitmapFactory
								.decodeStream(new FlushedInputStream(is));
						return bitmap;
					} finally {
						if (is != null) {
							is.close();
						}
						entity.consumeContent();
					}
				}
			}
		} catch (Throwable t) {
			Log.e(Main.TAG, "Exception fetching data", t);
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
