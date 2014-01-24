package nl.ipo.cds.etl.theme.areamanagement;

import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_NOT_URL;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_VALUE_TOO_HIGH;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_VALUE_TOO_LOW;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_POINT_NOT_ALLOWED;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_SRS_NULL;

import java.util.Collections;

import nl.ipo.cds.etl.test.GeometryConstants;
import nl.ipo.cds.etl.test.ValidationRunner;

import org.deegree.commons.tom.ows.CodeType;
import org.junit.Before;
import org.junit.Test;

public class AreaManagementValidatorTest {

	private AreaManagementValidator validator;
	private ValidationRunner<AreaManagement, Message, Context> runner;
	private GeometryConstants geom;

	@Before
	public void createValidator() throws Exception {
		validator = new AreaManagementValidator(Collections.emptyMap());
		runner = new ValidationRunner<>(validator, AreaManagement.class);
		geom = new GeometryConstants("EPSG:28992");
	}

	private ValidationRunner<AreaManagement, Message, Context>.Runner run(final String validationName) {
		return runner.validation(validationName);
	}

	@Test
	public void getInspireIdDatasetCodeValidator () throws Throwable {

		run ("inspireIdDatasetCode")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement", "glwgn")
			.with (new CodeType(""))
			.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);
		
		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement", "glwgn")
		    .with (new CodeType("glwgn"))
		    .assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("inspireIdDatasetCode")
	        .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement", "glwgn")
            .with (new CodeType("", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement"))
            .assertOnlyKey (ATTRIBUTE_EMPTY);
		
		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement", "glwgn")
	        .with (new CodeType("value1", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement"))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);
		
		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement", "glwgn")
	        .with (new CodeType("glwgn", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement"))
            .assertNoMessages();		
	}

	@Test
	public void getInspireIdLocalIdValidator () throws Throwable {

		run ("inspireIdLocalId")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);
		
		run ("inspireIdLocalId")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);		

		run ("inspireIdLocalId")
	        .with ("nl1000")
	        .assertNoMessages();		
	}	

	@Test	
	public void testGeometry () throws Exception {

		run ("geometry")
		    .with (null)
		    .assertOnlyKey (ATTRIBUTE_NULL);

		run ("geometry")
			.with (geom.lineString (null))
			.assertOnlyKey (GEOMETRY_SRS_NULL);
	
		run ("geometry")
			.with (geom.lineString (geom.getSrs ("EPSG:3857")))
			.assertOnlyKey (GEOMETRY_SRS_NOT_RD);		
		
		run ("geometry")
			.with (geom.point (1,2))
			.assertNoMessages ();

		run ("geometry")
   			.with (geom.lineString ())
   			.assertNoMessages ();

		run ("geometry")
			.with (geom.multiPolygon())
			.assertNoMessages ();		
			
		run ("geometry")
			.with (geom.lineStringDuplicatePoint ())
			.assertOnlyKey (GEOMETRY_POINT_DUPLICATION);
		
		run ("geometry")
			.with (geom.lineStringSelfIntersection ())
			.assertOnlyKey (GEOMETRY_SELF_INTERSECTION);
	}
	
	@Test
	public void getZoneTypeCodeValidator () throws Throwable {

		run ("zoneTypeCode")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("zoneTypeCode")
	        .with (new CodeType("bogus"))
	        .assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);		
		
		run ("zoneTypeCode")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ZoneTypeCode", "airQualityManagementZone")
			.with (new CodeType("", "http://inspire.ec.europa.eu/codeList/ZoneTypeCode"))
			.assertOnlyKey (ATTRIBUTE_EMPTY);		
		
		run ("zoneTypeCode")
            .withCodeList ("http://inspire.ec.europa.eu/codeList/ZoneTypeCode", "airQualityManagementZone")		
	        .with (new CodeType("airQualityManagementZonea", "http://inspire.ec.europa.eu/codeList/ZoneTypeCode"))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);
		
		run ("zoneTypeCode")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ZoneTypeCode", "airQualityManagementZone")
	        .with (new CodeType("airQualityManagementZone", "http://inspire.ec.europa.eu/codeList/ZoneTypeCode"))
            .assertNoMessages();		
	}

	@Test
	public void getEnvironmentalDomainCodeValidator () throws Throwable {

		run ("environmentalDomainCode")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);
	
		run ("environmentalDomainCode")
		    .with (new CodeType("bogus"))
		    .assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("environmentalDomainCode")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/EnvironmentalDomain", "soil")
		    .with (new CodeType("", "http://inspire.ec.europa.eu/codeList/EnvironmentalDomain"))
		    .assertOnlyKey (ATTRIBUTE_EMPTY);		
		
		run ("environmentalDomainCode")
            .withCodeList ("http://inspire.ec.europa.eu/codeList/EnvironmentalDomain", "soil")		
	        .with (new CodeType("soils", "http://inspire.ec.europa.eu/codeList/EnvironmentalDomain"))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);
		
		run ("environmentalDomainCode")
            .withCodeList ("http://inspire.ec.europa.eu/codeList/EnvironmentalDomain", "soil")		
	        .with (new CodeType("soil", "http://inspire.ec.europa.eu/codeList/EnvironmentalDomain"))
            .assertNoMessages();			
	}	

	@Test
	public void getThematicIdIdentifierValidator () throws Throwable {

		AreaManagement feature = new AreaManagement();
		feature.setThematicIdIdentifier("a");
		feature.setThematicIdIdentifierScheme(null);
		run ("thematicIdIdentifier")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);

		feature.setThematicIdIdentifier(null);
		feature.setThematicIdIdentifierScheme("a");
		run ("thematicIdIdentifier")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);

		feature.setThematicIdIdentifier(null);
		feature.setThematicIdIdentifierScheme(null);
		run ("thematicIdIdentifier")
			.withFeature(feature )
			.assertNoMessages ();
		
		feature.setThematicIdIdentifier("a");
		feature.setThematicIdIdentifierScheme("b");
		run ("thematicIdIdentifier")
			.withFeature(feature )
			.assertNoMessages ();
	}
	
	@Test
	public void getLegalBasisNameValidator () throws Throwable {

		run ("legalBasisName")
			.with (null)
			.assertNoMessages();

		run ("legalBasisName")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);
	
	    run ("legalBasisName")
	        .with ("name")
	        .assertNoMessages();	
	}
	
	@Test
	public void getLegalBasisLinkValidator () throws Throwable {

		run ("legalBasisLink")
			.with (null)
			.assertNoMessages();

		run ("legalBasisLink")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_NOT_URL);
	
	    run ("legalBasisLink")
	        .with ("http://www.idgis.nl")
	        .assertNoMessages();	
	}

	@Test
	public void getSpecialisedZoneTypeCodeValidator () throws Throwable {

		run ("specialisedZoneTypeCode")
			.with (null)
			.assertNoMessages();

		run ("specialisedZoneTypeCode")
	         .with (new CodeType(""))
	         .assertOnlyKey (ATTRIBUTE_EMPTY);
	
		run ("specialisedZoneTypeCode")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ZoneTypeCode", "airQualityManagementZone")
            .with (new CodeType("airQualityManagementZone", "http://inspire.ec.europa.eu/codeList/ZoneTypeCode"))
            .assertNoMessages();		
	}

	@Test
	public void getNoiseLowValueValidator () throws Throwable {

		run ("noiseLowValue")
			.with (null)
			.assertNoMessages();
		
		run ("noiseLowValue")
		    .with (0.0)
		    .assertNoMessages();
		
		run ("noiseLowValue")
	        .with (150.0)
	        .assertNoMessages();
		
		run ("noiseLowValue")
            .with (-0.00001)
            .assertOnlyKey(ATTRIBUTE_VALUE_TOO_LOW);
		
		run ("noiseLowValue")
	        .with (150.000001)
	        .assertOnlyKey(ATTRIBUTE_VALUE_TOO_HIGH);		
	}
		
	@Test
	public void getNoiseHighValueValidator () throws Throwable {

		run ("noiseHighValue")
			.with (null)
			.assertNoMessages();
		
		run ("noiseHighValue")
		    .with (0.0)
		    .assertNoMessages();
		
		run ("noiseHighValue")
	        .with (150.0)
	        .assertNoMessages();
		
		run ("noiseHighValue")
            .with (-0.00001)
            .assertOnlyKey(ATTRIBUTE_VALUE_TOO_LOW);
		
		run ("noiseHighValue")
	        .with (150.000001)
	        .assertOnlyKey(ATTRIBUTE_VALUE_TOO_HIGH);		
	}	
}
