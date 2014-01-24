package nl.ipo.cds.etl.featurecollection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.QName;
import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.featurecollection.FeatureCollectionReader;
import nl.ipo.cds.etl.util.LSInputUtils;

import org.deegree.feature.types.AppSchema;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.LineString;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.junit.Before;
import org.junit.Test;

public class FeatureCollectionTest {
	
	private AppSchema appSchema;
	private XMLInputFactory inputFactory;
	private FeatureCollectionReader featureCollectionReader;
	
	@Before
	public void createInputFactory () throws Exception {
		inputFactory = XMLInputFactory.newInstance ();
	}
	
	@Before
	public void createFeatureCollectionReader () throws Exception {
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/test/appschema-overijssel-ehs.xsd");
		
		final GMLAppSchemaReader appSchemaReader = new GMLAppSchemaReader (null, null, LSInputUtils.createInput (stream, "UTF-8"));
		
		appSchema = getAppSchema ("nl/ipo/cds/etl/test/appschema-overijssel-ehs.xsd");
		
		featureCollectionReader = new FeatureCollectionReader (appSchema);
	}
	
	private AppSchema getAppSchema (final String resourceName) throws Exception {
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream (resourceName);
		
		final GMLAppSchemaReader appSchemaReader = new GMLAppSchemaReader (null, null, LSInputUtils.createInput (stream, "UTF-8"));
		
		return appSchemaReader.extractAppSchema ();
	}
	
	@Test
	public void testEmpty() throws Exception {
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(
				"<wfs:FeatureCollection " +
					"xmlns:flat=\"urn:cds-inspire:flat\" " +
					"xmlns:wfs=\"http://www.opengis.net/wfs\" " +					
					"xmlns:gml=\"http://www.opengis.net/gml\">" +
					"<gml:boundedBy>" +
						"<gml:Envelope srsName=\"EPSG:28992\">" +
							"<gml:lowerCorner>168677.001000 306846.245300</gml:lowerCorner>" +
							"<gml:upperCorner>213104.760100 421212.541000</gml:upperCorner>" +
						"</gml:Envelope>" +
			      	"</gml:boundedBy>" +
				"</wfs:FeatureCollection>"
		));
		
		streamReader.nextTag();
		
		FeatureCollection featureCollection = featureCollectionReader.parseCollection(streamReader, getFeatureType ());
		Envelope envelope = featureCollection.getBoundedBy();
		
		assertNotNull(featureCollection);		
		assertNotNull(envelope);
		for(@SuppressWarnings("unused") GenericFeature genericFeature : featureCollection) {
			fail();
		}
	}
	
	@Test
	public void testReadFeatures () throws Exception {
		final InputStream inputStream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/test/featurecollection-overijssel-ehs.xml");
		final XMLStreamReader streamReader = inputFactory.createXMLStreamReader (inputStream);
		
		streamReader.nextTag ();
		
		final FeatureCollection featureCollection = featureCollectionReader.parseCollection (streamReader, getFeatureType ());
		
		assertNotNull (featureCollection);
		assertNotNull (featureCollection.getBoundedBy ());
		
		for (final GenericFeature feature: featureCollection) {
			assertNotNull (feature);
			assertNotNull (feature.getId ());
			
			assertTrue (feature.get ("geometry") instanceof Geometry);
			assertTrue (feature.get ("legalFoundationDate") instanceof Timestamp);
			assertTrue (feature.get ("inspireID") instanceof String);
			assertTrue (feature.get ("percentageUnderDesignation") instanceof BigDecimal);
		}
	}
	
	@Test
	public void testReadFeaturesGML () throws Exception {
		final FeatureCollectionReader featureCollectionReaderTransportroutedelen = new FeatureCollectionReader (getAppSchema ("nl/ipo/cds/etl/test/appschema-gasunie-transportroutedeel.xsd"));
		final InputStream inputStream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/test/featurecollection-gasunie-transportroutedeel.gml");
		final XMLStreamReader streamReader = inputFactory.createXMLStreamReader (inputStream);
		
		streamReader.nextTag ();
		
		final FeatureCollection featureCollection = featureCollectionReaderTransportroutedelen.parseCollection (streamReader, getFeatureType ());
		
		assertNotNull (featureCollection);
		assertNotNull (featureCollection.getBoundedBy ());
		
		for (final GenericFeature feature: featureCollection) {
			assertNotNull (feature);
			assertNotNull (feature.getId ());
			
			assertTrue (feature.get ("curveProperty") instanceof LineString);
			assertNotNull (feature.get ("transportrouteId"));
			assertNotNull (feature.get ("transportroutedeelId"));
		}
	}
	
	private FeatureType getFeatureType () {
		return new FeatureType () {
			@Override
			public QName getName() {
				return new QName () {

					@Override
					public int compareTo (final QName o) {
						return getLocalPart ().compareTo (o.getLocalPart ());
					}

					@Override
					public String getNamespace () {
						return "http://www.idgis.nl";
					}

					@Override
					public String getLocalPart () {
						return "TestFeatureType";
					}
				};
			}

			@Override
			public Set<FeatureTypeAttribute> getAttributes() {
				return new HashSet<FeatureTypeAttribute> ();
			}
		};
	}
}
