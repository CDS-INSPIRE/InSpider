package nl.ipo.cds.metadata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLRewriter {
	
	private final Document document;
	private final XPath xp;
	
	private final HashMap<String, String> uris = new HashMap<String, String>();
	private final HashMap<String, String> prefixes = new HashMap<String, String>();

	public XMLRewriter(final Document document) throws ParserConfigurationException, SAXException, IOException {
		this.document = document;
		
		final XPathFactory xpf = XPathFactory.newInstance();
		xp = xpf.newXPath();
		
		xp.setNamespaceContext(new NamespaceContext() {

			@Override
			public String getNamespaceURI(String prefix) {				
				return uris.get(prefix);
			}

			@Override
			public String getPrefix(String uri) {
				return prefixes.get(uri);
			}
			
			@Override
			@SuppressWarnings("rawtypes")
			public Iterator getPrefixes(String uri) {
				return Arrays.asList(getPrefix(uri)).iterator();
			}
			
		});
	}
	
	public void addNamespace(final String prefix, final String uri) {
		uris.put(prefix, uri);
		prefixes.put(uri, prefix);
	}
	
	public void modify(final String xpath, final String value) throws XPathExpressionException {
		final XPathExpression xpe = xp.compile(xpath);
		final NodeList nl = (NodeList)xpe.evaluate(document, XPathConstants.NODESET);
		
		if(nl.getLength() == 0) {
			throw new IllegalArgumentException("XPath expression " + xpath + " didn't yield any results.");
		}
		
		if(nl.getLength() != 1) {
			throw new IllegalArgumentException("XPath expression " + xpath + " should point to a single node.");
		}
		
		final Node n = nl.item(0);
		n.setTextContent(value);
	}
	
	public void write(final OutputStream outputStream) throws TransformerException {
		final TransformerFactory tf = TransformerFactory.newInstance();
		final Transformer t = tf.newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		t.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.transform(new DOMSource(document), new StreamResult(outputStream));
	}
}
