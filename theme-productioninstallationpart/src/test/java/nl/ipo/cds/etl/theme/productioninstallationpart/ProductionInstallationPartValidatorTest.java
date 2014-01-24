package nl.ipo.cds.etl.theme.productioninstallationpart;

import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.GEOMETRY_EMPTY_MULTIGEOMETRY;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.GEOMETRY_ONLY_POINT_OR_MULTIPOINT;

import java.util.Collections;

import nl.ipo.cds.etl.test.GeometryConstants;
import nl.ipo.cds.etl.test.ValidationRunner;

import org.deegree.commons.tom.ows.CodeType;
import org.junit.Before;
import org.junit.Test;

public class ProductionInstallationPartValidatorTest {

	private ProductionInstallationPartValidator validator;

	private ValidationRunner<ProductionInstallationPart, Message, Context> runner;

	private GeometryConstants geom;

	@Before
	public void createValidator() throws Exception {
		validator = new ProductionInstallationPartValidator(Collections.emptyMap());
		runner = new ValidationRunner<>(validator, ProductionInstallationPart.class);
		geom = new GeometryConstants("EPSG:28992");
	}

	private ValidationRunner<ProductionInstallationPart, Message, Context>.Runner run(final String validationName) {
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
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallationPart", "lgrinr")
	        .with (new CodeType("value1", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallationPart"))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);

		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallationPart", "lgrinr")
	        .with (new CodeType("lgrinr", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallationPart"))
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
	public void getStatusValidator () throws Throwable {

		ProductionInstallationPart feature = new ProductionInstallationPart();
		feature.setStatusDescription("a");
		feature.setStatusType(null);
		run ("status")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);

		feature = new ProductionInstallationPart();
		feature.setStatusDescription("a");
		feature.setStatusType(new CodeType("b"));
		run ("status")
			.withFeature(feature )
			.assertNoMessages();

		feature = new ProductionInstallationPart();
		feature.setStatusType(new CodeType("b"));
		run ("status")
			.withFeature(feature )
			.assertNoMessages();

		feature = new ProductionInstallationPart();
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

	@Test
	public void getTechniqueValidator () throws Throwable {

		run ("technique")
			.with (null)
			.assertNoMessages ();

		run ("technique")
	         .with (new CodeType(""))
		     .assertKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run ("technique")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/PollutionAbatementTechniqueValue", "gravitation")
		    .with (new CodeType("gravitations", "http://inspire.ec.europa.eu/codeList/PollutionAbatementTechniqueValue"))
		    .assertOnlyKey (ATTRIBUTE_CODE_INVALID);

		run ("technique")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/PollutionAbatementTechniqueValue", "gravitation")
		    .with (new CodeType("gravitation", "http://inspire.ec.europa.eu/codeList/PollutionAbatementTechniqueValue"))
		    .assertNoMessages();
	}
}
