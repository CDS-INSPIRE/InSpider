/**
 * 
 */
package nl.ipo.cds.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.security.*;
import java.security.cert.CertificateException;

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

	//W1505 033 : Ritesh start
	private static final Log log = LogFactory.getLog(HttpUtils.class);

	public static KeyManager[] keyManagers;
	public static TrustManager[] trustManagers;

	// Keystore parameters
	private static String keyStoreFileName;
	private static String keyStorePath;
	private static String keyStorePassword;
	// default keystore Type
	private static String keyStoreType = "JKS";

    // Truststore parameters
    private static String trustStoreFileName;
    private static String trustStorePath;
    private static String trustStorePassword;
    private static String trustStoreType = "JKS";
    private static String ciphers;

	static {
		keyStoreFileName = System.getProperty("ssl.keystore");
		keyStorePath = System.getProperty("ssl.keystorepath");
		keyStorePassword = System.getProperty("ssl.keystorepassword");
		keyStoreType = System.getProperty("ssl.keystoretype");
        trustStoreFileName = System.getProperty("ssl.truststore");
        trustStorePath = System.getProperty("ssl.truststorepath");
        trustStorePassword = System.getProperty("ssl.truststorepassword");
        trustStoreType = System.getProperty("ssl.truststoretype");
        ciphers = System.getProperty("ssl.ciphers");
		validateSslProperties();
	}

	private static void validateSslProperties() {
		try {
			validateKeyStoreProperties();
			validateTrustStoreProperties();
		}catch (Exception e){
			log.error(e.toString());
		}
	}

	/**
	 * Method to check if the required static fileds are set to create the HTTPS client.
	 * @throws Exception with related message
     */
	private static void validateKeyStoreProperties() throws Exception {
		if (StringUtils.isEmpty(keyStoreFileName)) {
			log.fatal("ssl.keystore property is not correctly set in ssl.properties");
			throw new Exception("ssl.keystore property is not correctly set in ssl.properties");
		}

		if (StringUtils.isEmpty(keyStorePath)) {
			log.fatal("ssl.keystorepath property is not correctly set in ssl.properties");
			throw new Exception("ssl.keystorepath property is not correctly set in ssl.properties");
		}

		if (StringUtils.isEmpty(keyStorePassword)) {
			log.fatal("ssl.keystorepassword property is not correctly set in ssl.properties");
			throw new Exception("ssl.keystorepassword property is not correctly set in ssl.properties");
		}
	}

    private static void validateTrustStoreProperties() throws Exception{
        if (StringUtils.isEmpty(trustStoreFileName)) {
            log.fatal("ssl.truststore property is not correctly set in ssl.properties");
            throw new Exception("ssl.truststore property is not correctly set in ssl.properties");
        }

        if (StringUtils.isEmpty(trustStorePassword)) {
            log.fatal("ssl.truststorepassword property is not correctly set in ssl.properties");
            throw new Exception("ssl.truststorepassword property is not correctly set in ssl.properties");
        }

        if (StringUtils.isEmpty(trustStorePath)) {
            log.fatal("ssl.truststorepath property is not correctly set in ssl.properties");
            throw new Exception("ssl.truststorepath property is not correctly set in ssl.properties");
        }
    }

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

	/**
	 * Method to create HttpsClient to handle https request.
	 * @return Object of DefaultHttpClient
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws UnrecoverableKeyException
	 * @throws CertificateException
	 * @throws KeyStoreException
     * @throws IOException
     */
	public static DefaultHttpClient createHttpsClient() throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, CertificateException,
			KeyStoreException, IOException {
		// create key managers
		keyManagers = HttpUtils.createKeyManagers();
		trustManagers = HttpUtils.createTrustManagers();

		SSLContext context = SSLContext.getInstance("SSLv3");
		context.init(keyManagers, trustManagers, null);
		org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = new SSLSocketFactory(context);
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 60000);// was 10000
		HttpConnectionParams.setSoTimeout(httpParams, 60000);// was 30000
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		// http-proxy
		if (!StringUtils.isEmpty(System.getProperty("https.proxyHost")) && !StringUtils.isEmpty(System.getProperty("https.proxyPort"))) {
			HttpHost proxy = new HttpHost(System.getProperty("https.proxyHost"), Integer.parseInt(System.getProperty("https.proxyPort")));
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sslSocketFactory));
		return httpClient;
	}

	/**
	 * Create Key Managers.
	 *
	 * @return array of KeyManager.
	 * @throws CertificateException      when there are problem with a certificate
	 * @throws IOException               when I/O fails
	 * @throws KeyStoreException         when operations on the keystore fail
	 * @throws NoSuchAlgorithmException  when SSLv3 algorithm is not available
	 * @throws UnrecoverableKeyException when a key in the keystore cannot be recovered
	 */
	private static KeyManager[] createKeyManagers() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
			UnrecoverableKeyException {

		KeyStore keyStore = KeyStore.getInstance(keyStoreType);

		// create Inputstream to keystore file
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(keyStorePath + "/" + keyStoreFileName);
			// create keystore object, load it with keystorefile data
			keyStore.load(inputStream, keyStorePassword == null ? null : keyStorePassword.toCharArray());
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}

		KeyManager[] managers;

		// create keymanager factory and load the keystore object in it
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, keyStorePassword == null ? null : keyStorePassword.toCharArray());
		managers = keyManagerFactory.getKeyManagers();

		// return
		return managers;
	}

	/**
	 * Create Trustmanagers.
	 *
	 * @return the array of TrustManager.
	 * @throws KeyStoreException        when operations on the keystore fail
	 * @throws NoSuchAlgorithmException when SSLv3 algorithm is not available
	 * @throws CertificateException     when there are problem with a certificate
	 * @throws IOException              when I/O fails
	 */
	private static TrustManager[] createTrustManagers() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		// create keystore object, load it with truststorefile data
		KeyStore trustStore = KeyStore.getInstance(trustStoreType);

		// create Inputstream to truststore file
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(trustStorePath + "/" + trustStoreFileName);
			trustStore.load(inputStream, trustStorePassword == null ? null : trustStorePassword.toCharArray());
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}

		// create trustmanager factory and load the keystore object in it
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		// return
		return trustManagerFactory.getTrustManagers();
	}

}
