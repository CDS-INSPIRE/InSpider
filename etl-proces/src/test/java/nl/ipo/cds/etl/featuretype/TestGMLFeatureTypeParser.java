package nl.ipo.cds.etl.featuretype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URL;

import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;

import org.deegree.gml.GMLVersion;
import org.junit.Test;

public class TestGMLFeatureTypeParser {

	@Test
	public void testLimburgEHS () throws Exception {
		final GMLFeatureTypeParser parser = new GMLFeatureTypeParser (GMLVersion.GML_31);
		
		final FeatureType ft = parser.parseSchema (
				getClass ().getClassLoader().getResourceAsStream ("nl/ipo/cds/etl/featuretype/schemas/limburg-ehs.xsd"),
				"EcologischeHoofdstructuur",
				"ISO-8859-1"
			);

		assertFeatureTypeName (ft, "EcologischeHoofdstructuur", "http://mapserver.gis.umn.edu/mapserver");
		
		assertHasAttribute (ft, "geometry", AttributeType.GEOMETRY);
		assertHasAttribute (ft, "OBJECTID", AttributeType.STRING);
		assertHasAttribute (ft, "GFID", AttributeType.STRING);
		assertHasAttribute (ft, "applicationSchema", AttributeType.STRING);
		assertHasAttribute (ft, "legalFoundationDate", AttributeType.STRING);
		assertHasAttribute (ft, "legalFoundationDocument", AttributeType.STRING);
		assertHasAttribute (ft, "inspireID", AttributeType.STRING);
		assertHasAttribute (ft, "siteName", AttributeType.STRING);
		assertHasAttribute (ft, "siteDesignation", AttributeType.STRING);
		assertHasAttribute (ft, "siteProtectionClassification", AttributeType.STRING);
		assertHasAttribute (ft, "percentageUnderDesignation", AttributeType.STRING);
	}

	@Test
	public void testOverijsselEHS () throws Exception {
		final GMLFeatureTypeParser parser = new GMLFeatureTypeParser (null);//GMLVersion.GML_31);
		
		final FeatureType ft = parser.parseSchema (
				getClass ().getClassLoader().getResourceAsStream ("nl/ipo/cds/etl/featuretype/schemas/overijssel-ehs.xsd"),
				"EcologischeHoofdstructuur",
				"UTF-8"
			);

		assertFeatureTypeName (ft, "EcologischeHoofdstructuur", "http://gisopenbaar.overijssel.nl/arcgis/services/Beschermde_gebieden/MapServer/WFSServer");
		
		assertHasAttribute (ft, "OBJECTID_1", AttributeType.INTEGER);
		assertHasAttribute (ft, "OBJECTID", AttributeType.DOUBLE);
		assertHasAttribute (ft, "GFID", AttributeType.STRING);
		assertHasAttribute (ft, "applicationSchema", AttributeType.STRING);
		assertHasAttribute (ft, "legalFoundationDate", AttributeType.DATE_TIME);
		assertHasAttribute (ft, "legalFoundationDocument", AttributeType.STRING);
		assertHasAttribute (ft, "inspireID", AttributeType.STRING);
		assertHasAttribute (ft, "siteName", AttributeType.STRING);
		assertHasAttribute (ft, "siteDesignation", AttributeType.STRING);
		assertHasAttribute (ft, "siteProtectionClassification", AttributeType.STRING);
		assertHasAttribute (ft, "percentageUnderDesignation", AttributeType.DOUBLE);
		assertHasAttribute (ft, "omschrijving", AttributeType.STRING);
		assertHasAttribute (ft, "CODE", AttributeType.DOUBLE);
		assertHasAttribute (ft, "OPMERKING", AttributeType.STRING);
		assertHasAttribute (ft, "geometry", AttributeType.GEOMETRY);
		assertHasAttribute (ft, "SHAPE.AREA", AttributeType.DOUBLE);
		assertHasAttribute (ft, "SHAPE.LEN", AttributeType.DOUBLE);
	}
	
	@Test
	public void testAttributeTypes () throws Exception {
		final GMLFeatureTypeParser parser = new GMLFeatureTypeParser (GMLVersion.GML_31);
		
		final FeatureType ft = parser.parseSchema (
				getClass ().getClassLoader().getResourceAsStream ("nl/ipo/cds/etl/featuretype/schemas/attributetypes.xsd"),
				"EcologischeHoofdstructuur",
				"ISO-8859-1"
			);

		assertFeatureTypeName (ft, "EcologischeHoofdstructuur", "http://mapserver.gis.umn.edu/mapserver");
		
		assertHasAttribute (ft, "geometryValue", AttributeType.GEOMETRY);
		assertHasAttribute (ft, "booleanValue", AttributeType.BOOLEAN);
		assertHasAttribute (ft, "decimalValue", AttributeType.DECIMAL);
		assertHasAttribute (ft, "doubleValue", AttributeType.DOUBLE);
		assertHasAttribute (ft, "integerValue", AttributeType.INTEGER);
		assertHasAttribute (ft, "integerValue2", AttributeType.INTEGER);
		assertHasAttribute (ft, "dateValue", AttributeType.DATE);
		assertHasAttribute (ft, "dateTimeValue", AttributeType.DATE_TIME);
		assertHasAttribute (ft, "timeValue", AttributeType.TIME);
		assertHasAttribute (ft, "floatValue", AttributeType.FLOAT);
	}
	
	@Test
	public void testDrentheAW () throws Exception {
		final GMLFeatureTypeParser parser = new GMLFeatureTypeParser (null);//GMLVersion.GML_31);
		
		parser.parseSchema (
				getClass ().getClassLoader().getResourceAsStream ("nl/ipo/cds/etl/featuretype/schemas/drenthe-aw.xsd"),
				"AW-AW",
				"UTF-8"
			);
	}
	
	@Test
	public void testZuidHollandST () throws Exception {
		final GMLFeatureTypeParser parser = new GMLFeatureTypeParser (null);//GMLVersion.GML_31);
		
		parser.parseSchema (
				getClass ().getClassLoader().getResourceAsStream ("nl/ipo/cds/etl/featuretype/schemas/zuidholland-st.xsd"),
				"StilteGebieden",
				"UTF-8"
			);
	}
	
	private static void assertFeatureTypeName (final FeatureType featureType, final String localPart, final String namespace) {
		assertEquals (localPart, featureType.getName ().getLocalPart ());
		assertEquals (namespace, featureType.getName ().getNamespace ());
	}
	
	private static void assertHasAttribute (final FeatureType featureType, final String localPart, final AttributeType type) {
		for (final FeatureTypeAttribute attribute: featureType.getAttributes ()) {
			if (localPart.equals (attribute.getName ().getLocalPart()) && type.equals (attribute.getType ())) {
				return;
			}
		}
		
		fail (String.format ("Attribute %s: %s not found", localPart, type));
	}
}
