package nl.ipo.cds.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import nl.ipo.cds.metadata.XMLRewriter;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLRewriterTest {
	
	private Document toDocument(String content) throws Exception {
		return toDocument(new ByteArrayInputStream(content.getBytes("utf-8")));
	}
	
	private Document toDocument(InputStream inputStream) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(inputStream);
	}
	
	private Node skipText(Node n) {
		while(n.getNodeType() == Node.TEXT_NODE) {
			n = n.getNextSibling();
		}
		
		return n;
	}

	@Test
	public void testModify() throws Exception {
		XMLRewriter rewriter = new XMLRewriter(toDocument(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<a xmlns=\"http://idgis.eu/myNamespace\"><b>Foo</b><c>Bar</c></a>"));
		
		rewriter.addNamespace("prefix", "http://idgis.eu/myNamespace");
		rewriter.modify("/prefix:a/prefix:c", "Hello world!");
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		rewriter.write(outputStream);
		
		Document document = toDocument(new ByteArrayInputStream(outputStream.toByteArray()));
		assertNotNull(document);
		
		Node a = document.getFirstChild();
		assertEquals("a", a.getLocalName());
		
		Node b = skipText(a.getFirstChild());
		assertEquals("b", b.getLocalName());
		assertEquals("Foo", b.getTextContent());
		
		Node c = skipText(b.getNextSibling());
		assertEquals("c", c.getLocalName());		
		assertEquals("Hello world!", c.getTextContent());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNonSingleNodeException() throws Exception {
		XMLRewriter rewriter = new XMLRewriter(toDocument(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<a xmlns=\"http://idgis.eu/myNamespace\"><b>Foo</b><c>Bar</c></a>"));
		
		rewriter.modify("//*", "Hello world!");
	}
}
