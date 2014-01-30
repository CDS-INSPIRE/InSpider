package nl.ipo.cds.etl.theme.hydronode;

import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_SRS_NULL;

import java.util.Collections;

import nl.ipo.cds.etl.test.GeometryConstants;
import nl.ipo.cds.etl.test.ValidationRunner;

import org.deegree.commons.tom.ows.CodeType;
import org.junit.Before;
import org.junit.Test;

public class HydroNodeValidatorTest {

	private HydroNodeValidator validator;
	private ValidationRunner<HydroNode, Message, Context> runner;
	private GeometryConstants geom;

	@Before
	public void createValidator() throws Exception {
		validator = new HydroNodeValidator(Collections.emptyMap());
		runner = new ValidationRunner<>(validator, HydroNode.class);
		geom = new GeometryConstants("EPSG:28992");
	}

	private ValidationRunner<HydroNode, Message, Context>.Runner run(final String validationName) {
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

//		run ("inspireIdDatasetCode")
//		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility", "lgrinr")
//	        .with (new CodeType("value1", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility"))
//	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);
//
//		run ("inspireIdDatasetCode")
//		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility", "lgrinr")
//	        .with (new CodeType("lgrinr", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility"))
//            .assertNoMessages();
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
	}

	@Test
	public void getNameValidator () throws Throwable {

		run ("name")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("name")
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run ("name")
	        .with ("nl1000")
	        .assertNoMessages();
	}

}
