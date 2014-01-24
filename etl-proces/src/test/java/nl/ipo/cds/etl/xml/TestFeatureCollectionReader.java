package nl.ipo.cds.etl.xml;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.etl.util.LSInputUtils;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.StreamFeatureCollection;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.junit.Test;

public class TestFeatureCollectionReader {

	private static final GMLVersion gmlVersion = GMLVersion.GML_31;
	
	@Test
	public void testReadFeatureCollection () throws Exception {
		// Appschema:
		final InputStream appSchemaInputStream = getClass ().getClassLoader().getResourceAsStream ("nl/ipo/cds/etl/test/appschema-overijssel-ehs.xsd");
		final GMLAppSchemaReader appSchemaReader = new GMLAppSchemaReader (gmlVersion, null, LSInputUtils.createInput (appSchemaInputStream, "windows-1252"));
		final AppSchema appSchema = appSchemaReader.extractAppSchema ();
		
		// GML stream reader:
		final InputStream inputStream = getClass ().getClassLoader().getResourceAsStream ("nl/ipo/cds/etl/test/featurecollection-overijssel-ehs.xml");
		final XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader (inputStream);
		final GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader (gmlVersion, reader);
		
		gmlStreamReader.setApplicationSchema (appSchema);
		
		final StreamFeatureCollection collection = gmlStreamReader.readFeatureCollectionStream ();
		
		for (final Feature feature: collection) {
			System.out.println (feature);
			for (final Property prop: feature.getProperties ()) {
				System.out.println (String.format ("- %s = %s (%s)", prop.getName ().getLocalPart (), prop.getValue(), prop.getValue ().getClass ()));
				if (prop.getValue () instanceof PrimitiveValue) {
					final PrimitiveValue pv = (PrimitiveValue)prop.getValue ();
					
					System.out.println (String.format ("  = %s (%s)", pv.getValue (), pv.getValue ().getClass ()));
				}
			}
		}
	}
}
