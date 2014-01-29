package nl.ipo.cds.etl.theme.watercourselink;

import static nl.ipo.cds.etl.theme.watercourselink.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.watercourselink.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.watercourselink.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.watercourselink.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.watercourselink.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.watercourselink.Message.GEOMETRY_EMPTY_MULTIGEOMETRY;
import static nl.ipo.cds.etl.theme.watercourselink.Message.GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE;
import static nl.ipo.cds.etl.theme.watercourselink.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.watercourselink.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.watercourselink.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.watercourselink.Message.GEOMETRY_SRS_NULL;

import java.util.Collections;

import nl.ipo.cds.etl.test.GeometryConstants;
import nl.ipo.cds.etl.test.ValidationRunner;
import nl.ipo.cds.etl.theme.watercourselink.Context;
import nl.ipo.cds.etl.theme.watercourselink.Message;
import nl.ipo.cds.etl.theme.watercourselink.WatercourseLink;
import nl.ipo.cds.etl.theme.watercourselink.WatercourseLinkValidator;

import org.deegree.commons.tom.ows.CodeType;
import org.junit.Before;
import org.junit.Test;

public class WatercourseLinkValidatorTest {

	private WatercourseLinkValidator validator;
	private ValidationRunner<WatercourseLink, Message, Context> runner;
	private GeometryConstants geom;

	@Before
	public void createValidator() throws Exception {
		validator = new WatercourseLinkValidator(Collections.emptyMap());
		runner = new ValidationRunner<>(validator, WatercourseLink.class);
		geom = new GeometryConstants("EPSG:28992");
	}

	private ValidationRunner<WatercourseLink, Message, Context>.Runner run(final String validationName) {
		return runner.validation(validationName);
	}

	@Test
	public void getInspireIdDatasetCodeValidator () throws Throwable {

		run ("inspireIdDatasetCode")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("inspireIdDatasetCode")
			.with (new CodeType(""))
			.assertOnlyKey (ATTRIBUTE_EMPTY);

		run ("inspireIdDatasetCode")
		    .with (new CodeType("bogus"))
		    .assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility", "lgrinr")
	        .with (new CodeType("value1", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility"))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);

		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility", "lgrinr")
	        .with (new CodeType("lgrinr", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility"))
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
	public void getProductionFacilityIdValidator () throws Throwable {

		run ("productionFacilityId")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("productionFacilityId")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run ("productionFacilityId")
	        .with ("nl1000")
	        .assertNoMessages();
	}

	@Test
	public void getThematicIdentifierValidator () throws Throwable {

		WatercourseLink feature = new WatercourseLink();
		feature.setThematicIdentifier("a");
		feature.setThematicIdentifierScheme(null);
		run ("thematicIdentifier")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);

		feature.setThematicIdentifier(null);
		feature.setThematicIdentifierScheme("a");
		run ("thematicIdentifier")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);

		feature.setThematicIdentifier(null);
		feature.setThematicIdentifierScheme(null);
		run ("thematicIdentifier")
			.withFeature(feature )
			.assertNoMessages ();

		feature.setThematicIdentifier("a");
		feature.setThematicIdentifierScheme("b");
		run ("thematicIdentifier")
			.withFeature(feature )
			.assertNoMessages ();
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
	public void getFunctionActivityValidator () throws Throwable {

		run ("functionActivity")
			.with (null)
			.assertKey (ATTRIBUTE_NULL);

		run ("functionActivity")
	         .with (new CodeType(""))
		     .assertKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("functionActivity")
	    	.with (new CodeType("bogus"))
	    	.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("functionActivity")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/EconomicActivityNACEValue", "lgrinr")
		    .with (new CodeType("value1", "http://inspire.ec.europa.eu/codeList/EconomicActivityNACEValue"))
		    .assertNoMessages();
// code value validation deactivated 		
//		    .assertOnlyKey (ATTRIBUTE_CODE_INVALID);

		run ("functionActivity")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/EconomicActivityNACEValue", "lgrinr")
		    .with (new CodeType("lgrinr", "http://inspire.ec.europa.eu/codeList/EconomicActivityNACEValue"))
		    .assertNoMessages();
	}

	@Test
	public void getFunctionInputValidator () throws Throwable {

		run ("functionInput")
			.with (null)
			.assertNoMessages();

		run ("functionInput")
	         .with (new CodeType(""))
		     .assertKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("functionInput")
	    	.with (new CodeType("bogus"))
	    	.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("functionInput")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ProductCPAValue", "lgrinr")
		    .with (new CodeType("value1", "http://inspire.ec.europa.eu/codeList/ProductCPAValue"))
		    .assertNoMessages();
// code value validation deactivated 		
//		    .assertOnlyKey (ATTRIBUTE_CODE_INVALID);


		run ("functionInput")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ProductCPAValue", "lgrinr")
		    .with (new CodeType("lgrinr", "http://inspire.ec.europa.eu/codeList/ProductCPAValue"))
		    .assertNoMessages();
	}

	@Test
	public void getFunctionOutputValidator () throws Throwable {

		run ("functionOutput")
			.with (null)
			.assertNoMessages();

		run ("functionOutput")
	         .with (new CodeType(""))
		     .assertKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("functionOutput")
	    	.with (new CodeType("bogus"))
	    	.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("functionOutput")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ProductCPAValue", "lgrinr")
		    .with (new CodeType("value1", "http://inspire.ec.europa.eu/codeList/ProductCPAValue"))
		    .assertNoMessages();
// code value validation deactivated 		
//		    .assertOnlyKey (ATTRIBUTE_CODE_INVALID);


		run ("functionOutput")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ProductCPAValue", "lgrinr")
		    .with (new CodeType("lgrinr", "http://inspire.ec.europa.eu/codeList/ProductCPAValue"))
		    .assertNoMessages();
	}

	@Test
	public void getFunctionDescriptionValidator () throws Throwable {

		run ("functionDescription")
			.with (null)
			.assertNoMessages();

		run ("functionDescription")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run ("functionDescription")
	        .with ("nl1000")
	        .assertNoMessages();
	}

	@Test
	public void getNameValidator () throws Throwable {

		run ("name")
			.with (null)
			.assertNoMessages();

		run ("name")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run ("name")
	        .with ("nl1000")
	        .assertNoMessages();
	}

	@Test
	public void testSurfaceGeometry () throws Exception {

		run ("surfaceGeometry")
			.with (null)
			.assertNoMessages ();

		run ("surfaceGeometry")
	    	.with (geom.point (1,2))
	    	.assertOnlyKey (GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE);

		run ("surfaceGeometry")
	    	.with (geom.lineString (null))
	    	.assertOnlyKey (GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE);

		run ("surfaceGeometry")
    		.with (geom.multiPoint ())
    		.assertOnlyKey (GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE);

		run ("surfaceGeometry")
			.with (geom.emptyMultiPolygon())
			.assertOnlyKey (GEOMETRY_EMPTY_MULTIGEOMETRY);

		run ("surfaceGeometry")
			.with (geom.polygon(null))
			.assertOnlyKey (GEOMETRY_SRS_NULL);

		run ("surfaceGeometry")
			.with (geom.polygon())
			.assertNoMessages ();

		run ("surfaceGeometry")
			.with (geom.multiPolygon())
			.assertNoMessages ();
	}

	@Test
	public void getStatusValidator () throws Throwable {

		WatercourseLink feature = new WatercourseLink();
		feature.setStatusDescription("a");
		feature.setStatusType(null);
		run ("status")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);

		feature = new WatercourseLink();
		feature.setStatusDescription("a");
		feature.setStatusType(new CodeType("b"));
		run ("status")
			.withFeature(feature )
			.assertNoMessages();

		feature = new WatercourseLink();
		feature.setStatusType(new CodeType("b"));
		run ("status")
			.withFeature(feature )
			.assertNoMessages();

		feature = new WatercourseLink();
		run ("status")
			.withFeature(feature )
			.assertNoMessages();
	}

	@Test
	public void getStatusTypeValidator () throws Throwable {

		run ("statusType")
			.with (null)
			.assertNoMessages();

		run ("statusType")
	         .with (new CodeType(""))
		     .assertKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("statusType")
	    	.with (new CodeType("bogus"))
	    	.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("statusType")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue", "functional")
		    .with (new CodeType("value1", "http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue"))
		    .assertOnlyKey (ATTRIBUTE_CODE_INVALID);

		run ("statusType")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue", "functional")
		    .with (new CodeType("functional", "http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue"))
		    .assertNoMessages();
	}

	@Test
	public void getStatusDescriptionValidator () throws Throwable {

		run ("statusDescription")
			.with (null)
			.assertNoMessages();

		run ("statusDescription")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run ("statusDescription")
	        .with ("nl1000")
	        .assertNoMessages();
	}
}
