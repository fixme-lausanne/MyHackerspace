package ch.fixme.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class Net {

	private final static String TAG = "Net";

	static public String get(String url) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(TAG, "Failed to download file: Status " + statusCode);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
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
			if (src != null) {
				src.close();
			}
			if (dest != null) {
				dest.close();
			}
		}
	}

}
