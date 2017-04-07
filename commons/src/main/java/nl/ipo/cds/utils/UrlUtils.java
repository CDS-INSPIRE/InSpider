package nl.ipo.cds.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Provides fail-safe utility methods for opening URLs.
 * <p>
 * Copes with everything encountered in CDS so far:
 * <ul>
 * <li>file URLs</li>
 * <li>http URLs</li>
 * <li>https URLs with SNI (see
 * http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0)</li>
 * </ul>
 * </p>
 */
public class UrlUtils {
	private static final Log log = LogFactory.getLog(UrlUtils.class);

	public static InputStream open(URL url) throws IOException {
		String protocol = url.getProtocol();
		if (protocol.equals("http") || protocol.equals("https")) {
			return openWithHttpClient(url);
		}
		return url.openStream();
	}

	public static InputStream openWithHttpClient(URL url) throws IOException {
		HttpClient client = null;
		String protocol = url.getProtocol();
		if (protocol.equals("http")) {
			client = HttpUtils.createHttpClient();
		} else if (protocol.equals("https")) {
			try {
				client = HttpUtils.createHttpsClient();
			} catch (Exception e) {
				log.error(e.toString());
			}
		}
		HttpGet httpGet = new HttpGet(url.toString());
		HttpResponse response = client.execute(httpGet);
		int httpResponseCode = response.getStatusLine().getStatusCode();
		if (httpResponseCode / 100 != 2) {
			client.getConnectionManager().shutdown();
			throw new IOException("Error accessing URL '" + url
					+ "' server responded with code " + httpResponseCode);
		}
		return response.getEntity().getContent();
	}

	public static long getLastModifiedHeader(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		try {
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).setRequestMethod("HEAD");
			}
			return conn.getLastModified();
		} finally {
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		}
	}
	
}
