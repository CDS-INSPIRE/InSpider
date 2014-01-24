package nl.ipo.cds.etl.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.Timestamp;

import javax.xml.stream.XMLStreamException;

import nl.ipo.cds.etl.process.helpers.HttpGetUtil;
import nl.ipo.cds.utils.AxiomUtils;

import org.apache.axiom.om.OMElement;
import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;

public class TestMetadataHarvester {

	private MetadataHarvester harvester;
	
	@Before
	public void createHarvester () {
		harvester = new MetadataHarvester ("http://www.provinciaalgeoregister-test.nl/pgr-csw/");
	}

	@Test
	public void testGetFeatureCollectionUrl () throws Exception {
		final String url = "	http://www.provinciaalgeoregister.nl/pgr-csw/services?request=GetRecordById&service=CSW&version=2.0.2&OUTPUTSCHEMA=http://www.isotc211.org/2005/gmd&ID=89a5cd04-8c75-5361-a0c0-203dc6682812";
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/process/89a5cd04-8c75-5361-a0c0-203dc6682812.xml");
		
		try {
			final PgrMetadata metadata = harvester.parseMetadataFromUrl (url, getUrl (url, stream));
			assertNotNull (metadata);
			String fcUrl = harvester.getFeatureCollectionUrl(metadata);
		} finally {
			stream.close ();
		}
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testParseMetadataUrl () throws Exception {
		final String url = "http://192.168.122.21/polact_ontgrondingen_v.xml";
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/process/polact_ontgrondingen_v.xml");
		
		try {
			final PgrMetadata metadata = harvester.parseMetadataFromUrl (url, getUrl (url, stream));
			
			// Test the metadata:
			assertNotNull (metadata);
			assertEquals ("http://192.168.122.21/polact_ontgrondingen_v.xml", metadata.getMetadataUrl ());
			assertEquals ("http://portal.prvlimburg.nl/geoservices/polact_4b_groene_waarden", metadata.getWfsUrl ());
			assertEquals ("polact_ontgrondingen_v", metadata.getFeatureTypeName ());
			assertEquals (new Timestamp (new Date (109, 02, 06).getTime ()), metadata.getTimestamp ());
			assertEquals ("http://portal.prvlimburg.nl/geoservices/polact_4b_groene_waarden?request=DescribeFeatureType&typename=polact_ontgrondingen_v&service=WFS&version=1.1.0", metadata.getSchemaUrl ());
			
		} finally {
			stream.close ();
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@Test
	public void testParseMetadataXsd_withprotocol () throws Exception {
		final String url = "http://5.9.86.120/main.html?download&weblink=1ce45bdc72413758b4af31237bf42bbc&realfilename=Transportroutedeel_md_bewerkt.xml";
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/process/Transportroutedeel_md_bewerkt.xml");
//			final InputStream stream = new URL(url).openStream();
		
		try {
			final PgrMetadata metadata = harvester.parseMetadataFromUrl (url, getUrl (url, stream));
			
			// Test the metadata:
			assertNotNull (metadata);
			assertEquals (url, metadata.getMetadataUrl ());
			assertEquals ("http://5.9.86.120/main.html?download&weblink=245133580414d3563cfa25f1e170043e&realfilename=Transportroutedeel_tst2.gml", metadata.getWfsUrl ());
			assertEquals ("Transportroutedeel", metadata.getFeatureTypeName ());
			assertEquals (new Timestamp (new Date (113, 9, 4).getTime ()), metadata.getTimestamp ());
			// xsd is retrieved from metadata
			assertEquals ("http://5.9.86.120/main.html?download&weblink=248ee0b02370782301f92b302b4a3131&realfilename=Transportroutedeel_tst2.xsd", metadata.getSchemaUrl ());
			
		} finally {
			stream.close ();
		}
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testParseMetadataXsd_noprotocol () throws Exception {
		final String url = "http://5.9.86.120/main.html?download&weblink=1ce45bdc72413758b4af31237bf42bbc&realfilename=Transportroutedeel_md_bewerkt.xml";
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/process/Transportroutedeel_md_bewerkt-no-w3c-xsd.xml");
		
		try {
			final PgrMetadata metadata = harvester.parseMetadataFromUrl (url, getUrl (url, stream));
			
			// Test the metadata:
			assertNotNull (metadata);
			assertEquals (url, metadata.getMetadataUrl ());
			assertEquals ("http://5.9.86.120/main.html?download&weblink=245133580414d3563cfa25f1e170043e&realfilename=Transportroutedeel_tst2.gml", metadata.getWfsUrl ());
			assertEquals ("Transportroutedeel", metadata.getFeatureTypeName ());
			assertEquals (new Timestamp (new Date (113, 9, 4).getTime ()), metadata.getTimestamp ());
			// xsd should have exact same url and name as gml, because it is not retrieved from metadata
			assertEquals ("http://5.9.86.120/main.html?download&weblink=245133580414d3563cfa25f1e170043e&realfilename=Transportroutedeel_tst2.xsd", metadata.getSchemaUrl ());
			
		} finally {
			stream.close ();
		}
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testParseMetadataLimburg () throws Exception {
		final String uuid = "79ae44d2-1bd9-487a-a622-80214051d5fa";
		final String url = "http://www.provinciaalgeoregister.nl/pgr-csw/services?request=GetRecordById&service=CSW&version=2.0.2&OUTPUTSCHEMA=http://www.isotc211.org/2005/gmd&ID=79ae44d2-1bd9-487a-a622-80214051d5fa";
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/process/limburg-ehs-csw.xml");
		
		try {
			final PgrMetadata metadata = harvester.parseMetadataFromPgr (uuid, url, getUrl (url, stream));
			
			// Test the metadata:
			assertNotNull (metadata);
			assertEquals ("http://www.provinciaalgeoregister.nl/pgr-csw/services?request=GetRecordById&service=CSW&version=2.0.2&OUTPUTSCHEMA=http://www.isotc211.org/2005/gmd&ID=79ae44d2-1bd9-487a-a622-80214051d5fa", metadata.getMetadataUrl ());
			assertEquals ("http://portal.prvlimburg.nl/geoservices/inspire?", metadata.getWfsUrl ());
			assertEquals ("EcologischeHoofdstructuur", metadata.getFeatureTypeName ());
			assertEquals (new Timestamp (new Date (112, 10, 30).getTime ()), metadata.getTimestamp ());
			assertEquals ("http://portal.prvlimburg.nl/geoservices/inspire?request=DescribeFeatureType&typename=EcologischeHoofdstructuur&service=WFS&version=1.1.0", metadata.getSchemaUrl ());
			
		} finally {
			stream.close ();
		}
	}
	

	private HttpGetUtil getUrl (final String url, final InputStream stream) {
		return new MockHttpGetUtil (url, stream);
	}
	
	private static class MockHttpGetUtil extends HttpGetUtil {
		private final InputStream inputStream;
		
		public MockHttpGetUtil (final String url, final InputStream stream) {
			super (url);
			
			this.inputStream = stream;
		}

		@Override
		public boolean isValidResponse() throws IOException {
			return true;
		}
		
		@Override
		public void close () {
		}

		@Override
		public OMElement getEntityOMElement () throws ClientProtocolException, URISyntaxException, IOException, XMLStreamException {
			return AxiomUtils.getOMElementFromInputStream (inputStream);
		}
		
		@Override
		public int getStatusCode() throws IOException {
			return 200;
		}
	}

}
