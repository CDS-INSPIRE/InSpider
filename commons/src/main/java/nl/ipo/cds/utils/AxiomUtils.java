/**
 * 
 */
package nl.ipo.cds.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.jaxen.JaxenException;

/**
 * Utility class voor axiom utility methods.<br>
 * 
 * @author Rob
 * 
 */
public class AxiomUtils {
	
	/*
	 * Http utilities
	 */

	private static AXIOMXPath getXPath(String xpathExpression, Properties nameSpaces) throws JaxenException {
		// from pgr-catalog nl.ipo.pgr.catalog.cswplus.axis2.Service.java
		AXIOMXPath queryXPath = new AXIOMXPath(xpathExpression);
		Set<String> keys = nameSpaces.stringPropertyNames();
		for (String key : keys) {
			queryXPath.addNamespace(key, nameSpaces.getProperty(key));			
		}
		return queryXPath;
	}

	private static OMElement getOMElementWithXPath(OMElement node, AXIOMXPath queryXPath) throws JaxenException {
		// from pgr-catalog nl.ipo.pgr.catalog.cswplus.axis2.Service.java
		OMElement queryElement = (OMElement)queryXPath.selectSingleNode(node);
		return queryElement;
	}

	public static OMElement getOMElementWithXPath(OMElement element, String xpathStr, Properties nameSpaces) throws JaxenException{
		OMElement subElement = null;
		AXIOMXPath xpath = getXPath(xpathStr, nameSpaces);
		subElement = getOMElementWithXPath(element, xpath) ;
		return  subElement;
	}

	public static OMElement getOMElementFromHttpResponse(HttpResponse httpResponse) throws URISyntaxException, ClientProtocolException, IOException, XMLStreamException 
	{
		OMElement root = null;

		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			// Stream content out
			InputStream instream = entity.getContent();
			root = getOMElementFromInputStream (instream);
		}

		return root;
	}
	
	public static OMElement getOMElementFromInputStream (final InputStream inputStream) throws IOException, XMLStreamException {
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader(in);

		StAXOMBuilder builder = new StAXOMBuilder(parser);
		return builder.getDocumentElement();
	}
}
