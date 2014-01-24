package nl.ipo.cds.etl.featurecollection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.featurecollection.ExceptionReport;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.featurecollection.WFSResponse;
import nl.ipo.cds.etl.featurecollection.WFSResponseReader;
import nl.ipo.cds.etl.test.TestData;
import nl.ipo.cds.etl.util.LSInputUtils;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.types.AppSchema;
import org.deegree.geometry.Geometry;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.junit.Before;
import org.junit.Test;

public class WFSResponseTest {
	
	private static final String[] ids = {
		"F12729__224",
		"F12729__225",
		"F12729__19",
		"F12729__20",
		"F12729__21",
		"F12729__22",
		"F12729__23",
		"F12729__24",
		"F12729__88",
		"F12729__89"
	};
	
	private AppSchema appSchema;
	private XMLInputFactory inputFactory;
	
	@Before
	public void createInputFactory () throws Exception {
		inputFactory = XMLInputFactory.newInstance ();
	}
	
	@Before
	public void createFeatureCollectionReader () throws Exception {
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/test/appschema-overijssel-ehs.xsd");
		
		final GMLAppSchemaReader appSchemaReader = new GMLAppSchemaReader (null, null, LSInputUtils.createInput (stream, "UTF-8"));
		
		appSchema = appSchemaReader.extractAppSchema ();
	}
	
	@Test
	public void testExceptionReport() throws Exception {
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(
			"<ows:ExceptionReport " + 
				"xmlns:ows=\"http://www.opengis.net/ows\" " + 
				"version=\"1.0.0\" >" +
			  "<ows:Exception exceptionCode=\"InvalidParameterValue\" locator=\"outputFormat\">" +
			    "<ows:ExceptionText>This WFS is not configured to handle the output/input format 'text/xml; subtype=gml/3.1.1'</ows:ExceptionText>" +
			  "</ows:Exception>" +
			"</ows:ExceptionReport>"
		));
		
		WFSResponseReader wfsResponseReader = new WFSResponseReader();
		WFSResponse wfsResponse = wfsResponseReader.parseWFSResponse(streamReader, appSchema, "EcologischeHoofdstructuur");
		
		assertFalse(wfsResponse.isFeatureCollection());		
		assertTrue(wfsResponse.isExceptionReport());
		
		ExceptionReport exceptionReport = wfsResponse.getExceptionReport();
		assertNotNull(exceptionReport);
		
		assertTrue(exceptionReport.hasExceptionCode());
		assertEquals("InvalidParameterValue", exceptionReport.getExceptionCode());
		
		assertTrue(exceptionReport.hasLocator());
		assertEquals("outputFormat", exceptionReport.getLocator());
		
		assertTrue(exceptionReport.hasExceptionText());
		assertEquals("This WFS is not configured to handle the output/input format 'text/xml; subtype=gml/3.1.1'", exceptionReport.getExceptionText());
	}
	
	@Test
	public void testFeatureMembers() throws Exception {
		final InputStream inputStream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/test/featurecollection-overijssel-ehs.xml");
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(inputStream);
		
		WFSResponseReader wfsResponseReader = new WFSResponseReader();
		WFSResponse wfsResponse = wfsResponseReader.parseWFSResponse(streamReader, appSchema, "EcologischeHoofdstructuur");
		
		assertTrue(wfsResponse.isFeatureCollection());
		assertFalse(wfsResponse.isExceptionReport());
		
		FeatureCollection featureCollection = wfsResponse.getFeatureCollection ();
		
		assertNotNull(featureCollection);
		
		Geometry boundedBy = featureCollection.getBoundedBy();
		assertNotNull(boundedBy);
		
		int count = 0;
		for(GenericFeature protectedSite : featureCollection) {
			assertNotNull(protectedSite);
			assertEquals(ids[count], protectedSite.getId());
			Geometry geometry = (Geometry)protectedSite.get ("geometry");
			assertNotNull(geometry);
			ICRS crs = geometry.getCoordinateSystem();
			assertNotNull(crs);
			assertEquals (boundedBy.getCoordinateSystem().getAlias(), geometry.getCoordinateSystem().getAlias());
			
			++ count;
		}
		assertEquals(10, count);
	}
	
	@Test
	public void testFeatureCollection() throws Exception {
		TestData testData = new TestData();
		 		
		Iterable<GenericFeature> stilteGebieden = testData.getFeatureCollection();
		assertNotNull(stilteGebieden);
		
		int count = 0;
		for(GenericFeature stilteGebied : stilteGebieden) {
			assertNotNull(stilteGebied);
			assertNotNull(stilteGebied.getId());
			assertNotNull(stilteGebied.get ("inspireID"));
			assertNotNull(stilteGebied.get ("geometry"));
			assertNotNull(stilteGebied.get ("legalFoundationDate"));
			assertNotNull(stilteGebied.get ("legalFoundationDocument"));
			assertNotNull(stilteGebied.get ("siteName"));
			assertNotNull(stilteGebied.get ("siteDesignation"));
			assertNotNull(stilteGebied.get ("siteProtectionClassification"));
			count++;
		}
		
		assertEquals(33, count);
	}
}
