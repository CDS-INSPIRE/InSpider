package nl.ipo.cds.etl.theme.productioninstallation;

import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_EMPTY_MULTIGEOMETRY;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_ONLY_CURVE_OR_MULTICURVE;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_ONLY_POINT_OR_MULTIPOINT;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_SRS_NULL;

import java.util.Collections;

import nl.ipo.cds.etl.test.GeometryConstants;
import nl.ipo.cds.etl.test.ValidationRunner;

import org.deegree.commons.tom.ows.CodeType;
import org.junit.Before;
import org.junit.Test;

public class ProductionInstallationValidatorTest {

	private ProductionInstallationValidator validator;
	private ValidationRunner<ProductionInstallation, Message, Context> runner;
	private GeometryConstants geom;

	@Before
	public void createValidator() throws Exception {
		validator = new ProductionInstallationValidator(Collections.emptyMap());
		runner = new ValidationRunner<>(validator, ProductionInstallation.class);
		geom = new GeometryConstants("EPSG:28992");
	}

	private ValidationRunner<ProductionInstallation, Message, Context>.Runner run(final String validationName) {
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
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallation", "lgrinr")
	        .with (new CodeType("value1", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallation"))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);

		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallation", "lgrinr")
	        .with (new CodeType("lgrinr", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallation"))
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
	public void getProductionInstallationIdValidator () throws Throwable {

		run ("productionInstallationId")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("productionInstallationId")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run ("productionInstallationId")
	        .with ("nl1000")
	        .assertNoMessages();
	}

	@Test
	public void getThematicIdentifierValidator () throws Throwable {

		ProductionInstallation feature = new ProductionInstallation();
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
	public void testPointGeometry () throws Exception {

		run ("pointGeometry")
			.with (null)
			.assertNoMessages ();

		run ("pointGeometry")
		    .with (geom.lineString (null))
		    .assertOnlyKey (GEOMETRY_ONLY_POINT_OR_MULTIPOINT);

		run ("pointGeometry")
			.with (geom.multiPolygon())
			.assertOnlyKey (GEOMETRY_ONLY_POINT_OR_MULTIPOINT);

		run ("pointGeometry")
			.with (geom.emptyMultiPolygon())
			.assertOnlyKey (GEOMETRY_EMPTY_MULTIGEOMETRY);

		run ("pointGeometry")
			.with (geom.point (1,2))
			.assertNoMessages ();

		run ("pointGeometry")
			.with (geom.multiPoint())
			.assertNoMessages ();
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
	public void testLineGeometry () throws Exception {

		run ("lineGeometry")
			.with (null)
			.assertNoMessages ();

		run ("lineGeometry")
	    	.with (geom.point (1,2))
	    	.assertOnlyKey (GEOMETRY_ONLY_CURVE_OR_MULTICURVE);

		run ("lineGeometry")
			.with (geom.multiPoint ())
			.assertOnlyKey (GEOMETRY_ONLY_CURVE_OR_MULTICURVE);

		run ("lineGeometry")
			.with (geom.polygon())
			.assertOnlyKey (GEOMETRY_ONLY_CURVE_OR_MULTICURVE);

		run ("lineGeometry")
			.with (geom.multiPolygon())
			.assertOnlyKey (GEOMETRY_ONLY_CURVE_OR_MULTICURVE);

		run ("lineGeometry")
			.with (geom.emptyMultiPolygon())
			.assertOnlyKey (GEOMETRY_EMPTY_MULTIGEOMETRY);

		run ("lineGeometry")
		    .with (geom.lineString (null))
		    .assertOnlyKey (GEOMETRY_SRS_NULL);

		run ("lineGeometry")
			.with (geom.lineString (geom.getSrs ("EPSG:3857")))
			.assertOnlyKey (GEOMETRY_SRS_NOT_RD);

		run ("lineGeometry")
			.with (geom.lineStringDuplicatePoint ())
			.assertOnlyKey (GEOMETRY_POINT_DUPLICATION);

		run ("lineGeometry")
			.with (geom.lineStringSelfIntersection ())
			.assertOnlyKey (GEOMETRY_SELF_INTERSECTION);

		run ("lineGeometry")
   			.with (geom.lineString ())
   			.assertNoMessages ();
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
	public void getDescriptionValidator () throws Throwable {

		run ("description")
			.with (null)
			.assertNoMessages();

		run ("description")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run ("description")
	        .with ("nl1000")
	        .assertNoMessages();
	}

	@Test
	public void getStatusValidator () throws Throwable {

		ProductionInstallation feature = new ProductionInstallation();
		feature.setStatusDescription("a");
		feature.setStatusType(null);
		run ("status")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);

		feature = new ProductionInstallation();
		feature.setStatusDescription("a");
		feature.setStatusType(new CodeType("b"));
		run ("status")
			.withFeature(feature )
			.assertNoMessages();

		feature = new ProductionInstallation();
		feature.setStatusType(new CodeType("b"));
		run ("status")
			.withFeature(feature )
			.assertNoMessages();

		feature = new ProductionInstallation();
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

	@Test
	public void getTypeValidator () throws Throwable {

		run ("type")
			.with (null)
			.assertNoMessages ();

		run ("type")
	         .with (new CodeType(""))
		     .assertKey (ATTRIBUTE_EMPTY);

		run ("type")
		    .withCodeList ("http://www.risicokaart.nl/codelist/Installatietype", "lgrinr")
		    .with (new CodeType("lgrinr", "http://www.risicokaart.nl/codelist/Installatietype"))
		    .assertNoMessages();

		run ("type")
		    .withCodeList ("https://www.lgronline.nl/codelist/Installatietype", "lgrinr")
		    .with (new CodeType("lgrinr", "https://www.lgronline.nl/codelist/Installatietype"))
		    .assertNoMessages();
	}
}
