/**
 * 
 */
package nl.ipo.cds.utils;

import java.net.ProxySelector;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Utility class voor http utility methods.<br>
 * 
 * @author Rob
 * 
 */
public class HttpUtils {
	
	/*
	 * Http utilities
	 */

	public static DefaultHttpClient createHttpClient() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 60000);// was 10000
		HttpConnectionParams.setSoTimeout(httpParams, 60000);// was 30000

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		// http-proxy
		ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
				httpClient.getConnectionManager().getSchemeRegistry(),
				ProxySelector.getDefault());
		httpClient.setRoutePlanner(routePlanner);

		return httpClient;
	}

}
