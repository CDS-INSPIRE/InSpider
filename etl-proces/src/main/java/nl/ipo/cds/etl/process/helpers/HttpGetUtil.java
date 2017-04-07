/**
 * 
 */
package nl.ipo.cds.etl.process.helpers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.stream.XMLStreamException;

import nl.ipo.cds.etl.process.TagProcess;
import nl.ipo.cds.utils.AxiomUtils;
import nl.ipo.cds.utils.HttpUtils;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author Rob
 *
 */
public class HttpGetUtil {
	private static final Log log = LogFactory.getLog(HttpGetUtil.class);
	private DefaultHttpClient httpClient;
	private HttpResponse httpResponse;
	private StatusLine statusLine;
	private String url;	
	private OMElement root;

	public HttpGetUtil (String url){
		super();
		this.url = url;
	}

	private void execute() throws IOException {
		if (url.startsWith("http://")){
			httpClient = HttpUtils.createHttpClient();
		}
		else if (url.startsWith("https://"))
		{
			try {
				httpClient = HttpUtils.createHttpsClient();
			} catch (Exception e){
				log.error(e.toString());
			}
		}
		HttpGet httpRequest = new HttpGet(url);
		httpResponse = httpClient.execute(httpRequest);
		statusLine = httpResponse.getStatusLine();
	}
	
	public boolean isValidResponse() throws IOException{
		if(this.statusLine == null){
			this.execute();
		}
		int statusCode = statusLine.getStatusCode();
		return (statusCode >= 200 && statusCode < 300);
	}
	
	/**
	 * Close connection. 
	 */
	public void close(){
		if (root!=null){
			root.close(false);
		}
		if (httpClient!=null){
			httpClient.getConnectionManager().shutdown();
		}
	}

	public OMElement getEntityOMElement() throws ClientProtocolException, URISyntaxException, IOException, XMLStreamException {
		if(this.httpResponse == null){
			this.execute();
		}
		this.root = AxiomUtils.getOMElementFromHttpResponse(this.httpResponse);
		return this.root;
	}

	public int getStatusCode() throws IOException {
		if(this.statusLine == null){
			this.execute();
		}
		return this.statusLine.getStatusCode();
	}
	
}
