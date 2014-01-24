package nl.ipo.cds.etl.theme.exposedelements;

import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTES_EXCLUSIVE;
import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTE_VALUE_INVALID;
import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTE_VALUE_NEGATIVE;
import static nl.ipo.cds.etl.theme.exposedelements.Message.ATTRIBUTE_VALUE_TOO_HIGH;
import static nl.ipo.cds.etl.theme.exposedelements.Message.GEOMETRY_EMPTY_MULTIGEOMETRY;
import static nl.ipo.cds.etl.theme.exposedelements.Message.GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE;
import static nl.ipo.cds.etl.theme.exposedelements.Message.GEOMETRY_SRS_NULL;
import static nl.ipo.cds.etl.theme.exposedelements.Message.QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_EXCLUSIVE;
import static nl.ipo.cds.etl.theme.exposedelements.Message.QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_REQUIRED;

import java.util.Collections;

import nl.ipo.cds.etl.test.GeometryConstants;
import nl.ipo.cds.etl.test.ValidationRunner;
import nl.ipo.cds.etl.theme.exposedelements.Context;
import nl.ipo.cds.etl.theme.exposedelements.Message;
import nl.ipo.cds.etl.theme.exposedelements.ExposedElements;
import nl.ipo.cds.etl.theme.exposedelements.ExposedElementsValidator;

import org.deegree.commons.tom.ows.CodeType;
import org.junit.Before;
import org.junit.Test;

public class ExposedElementsValidatorTest {

	private ExposedElementsValidator validator;
	private ValidationRunner<ExposedElements, Message, Context> runner;
	private GeometryConstants geom;

	@Before
	public void createValidator() throws Exception {
		validator = new ExposedElementsValidator(Collections.emptyMap());
		runner = new ValidationRunner<>(validator, ExposedElements.class);
		geom = new GeometryConstants("EPSG:28992");
	}

	private ValidationRunner<ExposedElements, Message, Context>.Runner run(final String validationName) {
		return runner.validation(validationName);
	}

	@Test
	public void testInspireIdDatasetCodeValidation () throws Throwable {
		assertNotNullCodeValidation("inspireIdDatasetCode", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/HazardArea");
	}

	@Test
	public void testInspireIdLocalIdValidation () throws Throwable {
		assertNotNullNotEmptyStringValidation("inspireIdLocalId");
	}

	@Test
	public void testHazardAreaIdValidation () throws Throwable {
		assertNotNullNotEmptyStringValidation("hazardAreaId");
	}

	@Test
	public void testDeterminationMethodValidation () throws Throwable {

		run ("determinationMethod")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_VALUE_INVALID);

		run ("determinationMethod")
			.with ("")
			.assertOnlyKey (ATTRIBUTE_VALUE_INVALID);

		run ("determinationMethod")
			.with ("bogus")
			.assertOnlyKey (ATTRIBUTE_VALUE_INVALID);

		run ("determinationMethod")
			.with ("modelling")
			.assertNoMessages();

		run ("determinationMethod")
			.with ("indirectDetermination")
			.assertNoMessages();
	}

	@Test
	public void getTypeOfHazardHazardCategoryValidation () throws Throwable {
		assertNotNullCodeValidation("typeOfHazardHazardCategory", "http://inspire.ec.europa.eu/codeList/RiskOrHazardCategoryValue");
	}

	@Test
	public void testGeometryValidation () throws Exception {
		assertNotNullSurfaceGeometryValidation("geometry");
	}

	@Test
	public void testLikelihoodOfOccurrenceValidation () throws Exception {

		ExposedElements feature = new ExposedElements();
		run ("likelihoodOfOccurrence")
			.withFeature(feature)
			.assertOnlyKey (QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_REQUIRED);

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceQualitativeLikelihood("a");
		feature.setLikelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence(1.0);
		run ("likelihoodOfOccurrence")
			.withFeature(feature)
			.assertOnlyKey (QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_EXCLUSIVE);

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceQualitativeLikelihood("a");
		feature.setLikelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod(1.0);
		run ("likelihoodOfOccurrence")
			.withFeature(feature)
			.assertOnlyKey (QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_EXCLUSIVE);

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceQualitativeLikelihood("a");
		run ("likelihoodOfOccurrence")
			.withFeature(feature)
			.assertNoMessages();

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence(1.0);
		run ("likelihoodOfOccurrence")
			.withFeature(feature)
			.assertNoMessages();

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod(1.0);
		run ("likelihoodOfOccurrence")
			.withFeature(feature)
			.assertNoMessages();
	}

	@Test
	public void testLikelihoodOfOccurrenceAssessmentMethodValidation () throws Exception {
		ExposedElements feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceAssessmentMethodLink("b");
		run ("likelihoodOfOccurrenceAssessmentMethod")
			.withFeature(feature)
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceAssessmentMethodName("a");
		feature.setLikelihoodOfOccurrenceAssessmentMethodLink("b");
		run ("likelihoodOfOccurrenceAssessmentMethod")
			.withFeature(feature)
			.assertNoMessages();

		feature = new ExposedElements();
		run ("likelihoodOfOccurrenceAssessmentMethod")
			.withFeature(feature)
			.assertNoMessages();

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceAssessmentMethodName("a");
		run ("likelihoodOfOccurrenceAssessmentMethod")
			.withFeature(feature)
			.assertNoMessages();
	}

	@Test
	public void testLikelihoodOfOccurrenceQualitativeLikelihoodValidation () throws Exception {
		assertNullOrEmptyStringValidation("likelihoodOfOccurrenceQualitativeLikelihood");
	}

	@Test
	public void testLikelihoodOfOccurrenceAssessmentMethodNameValidation () throws Exception {
		assertNullOrEmptyStringValidation("likelihoodOfOccurrenceAssessmentMethodName");
	}

	@Test
	public void testLikelihoodOfOccurrenceAssessmentMethodLinkValidation () throws Exception {
		assertNullOrEmptyStringValidation("likelihoodOfOccurrenceAssessmentMethodLink");
	}

	@Test
	public void testLikelihoodOfOccurrenceQuantitativeLikelihoodValidation () throws Exception {

		ExposedElements feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence(1.0);
		feature.setLikelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod(1.0);
		run ("likelihoodOfOccurrenceQuantitativeLikelihood")
			.withFeature(feature)
			.assertOnlyKey (ATTRIBUTES_EXCLUSIVE);

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence(1.0);
		run ("likelihoodOfOccurrenceQuantitativeLikelihood")
			.withFeature(feature)
			.assertNoMessages();

		feature = new ExposedElements();
		feature.setLikelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod(1.0);
		run ("likelihoodOfOccurrenceQuantitativeLikelihood")
			.withFeature(feature)
			.assertNoMessages();

		feature = new ExposedElements();
		run ("likelihoodOfOccurrenceQuantitativeLikelihood")
			.withFeature(feature)
			.assertNoMessages();
	}

	@Test
	public void testLikelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurenceValidation () throws Exception {

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence")
			.with (null)
			.assertNoMessages();

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence")
			.with (-0.1)
			.assertOnlyKey(ATTRIBUTE_VALUE_NEGATIVE);

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence")
			.with (1.01)
			.assertOnlyKey(ATTRIBUTE_VALUE_TOO_HIGH);

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence")
			.with (0.0)
			.assertNoMessages();

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence")
			.with (1.0)
			.assertNoMessages();
	}

	@Test
	public void testLikelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriodValidation () throws Exception {

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod")
			.with (null)
			.assertNoMessages();

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod")
			.with (-0.1)
			.assertOnlyKey(ATTRIBUTE_VALUE_NEGATIVE);

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod")
			.with (0.0)
			.assertNoMessages();

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod")
			.with (1.0)
			.assertNoMessages();

		run ("likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod")
			.with (1000.0)
			.assertNoMessages();
	}

	private void assertNullOrEmptyStringValidation (String attrName) {
		run (attrName)
			.with (null)
			.assertNoMessages ();

		run (attrName)
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run (attrName)
	        .with ("nl1000")
	        .assertNoMessages();
	}

	private void assertNotNullNotEmptyStringValidation (String attrName) {
		run (attrName)
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run (attrName)
		    .with ("")
		    .assertOnlyKey (ATTRIBUTE_EMPTY);

		run (attrName)
	        .with ("nl1000")
	        .assertNoMessages();
	}


	private void assertNotNullCodeValidation(String attrName, String codeList) {
		run (attrName)
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run (attrName)
			.with (new CodeType(""))
			.assertOnlyKey (ATTRIBUTE_EMPTY);

		run (attrName)
		    .with (new CodeType("bogus"))
		    .assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);

		run (attrName)
		    .withCodeList (codeList, "value")
	        .with (new CodeType("bogus", codeList))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);

		run (attrName)
		    .withCodeList (codeList, "value")
	        .with (new CodeType( "value", codeList))
	        .assertNoMessages();
	}

	private void assertNotNullSurfaceGeometryValidation (String attrName) throws Exception {

		run (attrName)
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run (attrName)
	    	.with (geom.point (1,2))
	    	.assertOnlyKey (GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE);

		run (attrName)
	    	.with (geom.lineString (null))
	    	.assertOnlyKey (GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE);

		run (attrName)
    		.with (geom.multiPoint ())
    		.assertOnlyKey (GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE);

		run (attrName)
			.with (geom.emptyMultiPolygon())
			.assertOnlyKey (GEOMETRY_EMPTY_MULTIGEOMETRY);

		run (attrName)
			.with (geom.polygon(null))
			.assertOnlyKey (GEOMETRY_SRS_NULL);

		run (attrName)
			.with (geom.polygon())
			.assertNoMessages ();

		run (attrName)
			.with (geom.multiPolygon())
			.assertNoMessages ();
	}


}
