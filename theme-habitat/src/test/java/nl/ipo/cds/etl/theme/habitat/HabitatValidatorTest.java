package nl.ipo.cds.etl.theme.habitat;

import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_VALUE_NEGATIVE;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_SRS_NULL;

import java.util.Collections;

import nl.ipo.cds.etl.test.GeometryConstants;
import nl.ipo.cds.etl.test.ValidationRunner;

import org.deegree.commons.tom.ows.CodeType;
import org.junit.Before;
import org.junit.Test;

public class HabitatValidatorTest {

	private HabitatValidator validator;
	private ValidationRunner<Habitat, Message, Context> runner;
	private GeometryConstants geom;

	@Before
	public void createValidator() throws Exception {
		validator = new HabitatValidator(Collections.emptyMap());
		runner = new ValidationRunner<>(validator, Habitat.class);
		geom = new GeometryConstants("EPSG:28992");
	}

	private ValidationRunner<Habitat, Message, Context>.Runner run(final String validationName) {
		return runner.validation(validationName);
	}

	@Test
	public void getInspireIdDatasetCodeValidator () throws Throwable {

		run ("inspireIdDatasetCode")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("inspireIdDatasetCode")
			.with (new CodeType(""))
			.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);
		
		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat", "hbttpkt")
		    .with (new CodeType("hbttpkt"))
		    .assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);
		
		run ("inspireIdDatasetCode")
	        .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat", "hbttpkt")
	        .with (new CodeType("hbttpkt", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat/"))
		    .assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);		

		run ("inspireIdDatasetCode")
	        .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat", "hbttpkt")
            .with (new CodeType("value1", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat"))
            .assertOnlyKey (ATTRIBUTE_CODE_INVALID);				
		
		run ("inspireIdDatasetCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat", "hbttpkt")
	        .with (new CodeType("hbttpkt", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat"))
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
	public void testHabitatReferenceHabitatTypeIdCodeValidator () throws Exception {

		run ("habitatReferenceHabitatTypeIdCode")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("habitatReferenceHabitatTypeIdCode")
			.with (new CodeType(""))
			.assertOnlyKey (ATTRIBUTE_EMPTY);
			
		run ("habitatReferenceHabitatTypeIdCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat", "Habitattypenkaart")
	        .with (new CodeType("value1", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat"))
	        .assertNoMessages();
		
		run ("habitatReferenceHabitatTypeIdCode")
		    .withCodeList ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat", "Habitattypenkaart")
	        .with (new CodeType("Habitattypenkaart", "http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat"))
	        .assertNoMessages();		
	}

	@Test	
	public void testHabitatReferenceHabitatTypeSchemeCode () throws Exception {

		run ("habitatReferenceHabitatTypeSchemeCode")
			.with (null)
			.assertOnlyKey (ATTRIBUTE_NULL);

		run ("habitatReferenceHabitatTypeSchemeCode")
            .withCodeList ("http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue", "eunis")		
			.with (new CodeType(""))
			.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);
		
		run ("habitatReferenceHabitatTypeSchemeCode")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue", "eunis")
		    .with (new CodeType("bogus"))
		    .assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);
		
		run ("habitatReferenceHabitatTypeSchemeCode")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue", "eunis")
	        .with (new CodeType("value1", "http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue"))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);
		
		run ("habitatReferenceHabitatTypeSchemeCode")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue", "eunis")
	        .with (new CodeType("eunis", "http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue"))
	        .assertNoMessages();
	}

	@Test	
	public void testLocalHabitatNameLocalSchemeValidator () throws Exception {
		run ("localHabitatNameLocalScheme")
			.with (null)
			.assertNoMessages();
		
		run ("localHabitatNameLocalScheme")
			.with ("")
			.assertOnlyKey (ATTRIBUTE_EMPTY);
		
		run ("localHabitatNameLocalScheme")
			.with ("value")
			.assertNoMessages();
	
		Habitat feature = new Habitat();
		feature.setLocalHabitatNameLocalNameCode(new CodeType ("value","http://www.namespace.com"));
		run ("localHabitatNameLocalScheme")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
		
		feature = new Habitat();
		feature.setLocalHabitatNameLocalName("value");		
		run ("localHabitatNameLocalScheme")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
		
		feature = new Habitat();
		feature.setLocalHabitatNameQualifierLocalName(new CodeType ("value","http://www.namespace.com"));
		run ("localHabitatNameLocalScheme")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);		
	}

	@Test	
	public void testLocalHabitatNameLocalNameCodeValidator () throws Exception {
		run ("localHabitatNameLocalNameCode")
			.with (null)
			.assertNoMessages();
		
		run ("localHabitatNameLocalNameCode")
			.with (new CodeType ("","http://www.namespace.com"))
			.assertOnlyKey (ATTRIBUTE_EMPTY);
	
		run ("localHabitatNameLocalNameCode")
			.with (new CodeType ("value","http://www.namespace.com"))
			.assertNoMessages();

		Habitat feature = new Habitat();
		feature.setLocalHabitatNameLocalScheme("value");
		run ("localHabitatNameLocalNameCode")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
		
		feature = new Habitat();
		feature.setLocalHabitatNameLocalName("value");		
		run ("localHabitatNameLocalNameCode")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
		
		feature = new Habitat();
		feature.setLocalHabitatNameQualifierLocalName(new CodeType ("value","http://www.namespace.com"));
		run ("localHabitatNameLocalNameCode")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
	}
	
	@Test	
	public void testLocalHabitatNameLocalNameValidator () throws Exception {
		run ("localHabitatNameLocalName")
			.with (null)
			.assertNoMessages();
		
		run ("localHabitatNameLocalName")
			.with ("")
			.assertOnlyKey (ATTRIBUTE_EMPTY);
		
		run ("localHabitatNameLocalName")
			.with ("value")
			.assertNoMessages();
	
		Habitat feature = new Habitat();
		feature.setLocalHabitatNameLocalNameCode(new CodeType ("value","http://www.namespace.com"));
		run ("localHabitatNameLocalName")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
		
		feature = new Habitat();
		feature.setLocalHabitatNameLocalNameCode(new CodeType ("value","http://www.namespace.com"));		
		run ("localHabitatNameLocalName")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
		
		feature = new Habitat();
		feature.setLocalHabitatNameQualifierLocalName(new CodeType ("value","http://www.namespace.com"));
		run ("localHabitatNameLocalName")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);		
	}
	
	@Test	
	public void testLocalHabitatNameQualifierLocalName () throws Exception {
		run ("localHabitatNameQualifierLocalName")
			.with (null)
			.assertNoMessages();

		run ("localHabitatNameQualifierLocalName")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue", "congruent")
			.with (new CodeType ("congruent","http://www.namespace.com"))
			.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);
	
		run ("localHabitatNameQualifierLocalName")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue", "congruent")
	    	.with (new CodeType("bogus"))
	    	.assertOnlyKey (ATTRIBUTE_CODE_CODESPACE_INVALID);
	
		run ("localHabitatNameQualifierLocalName")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue", "congruent")
	        .with (new CodeType("value1", "http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue"))
	        .assertOnlyKey (ATTRIBUTE_CODE_INVALID);
		
		run ("localHabitatNameQualifierLocalName")
		    .withCodeList ("http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue", "congruent")
	        .with (new CodeType("congruent", "http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue"))
	        .assertNoMessages();
	
		Habitat feature = new Habitat();
		feature.setLocalHabitatNameLocalScheme("value");
		run ("localHabitatNameQualifierLocalName")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
		
		feature = new Habitat();
		feature.setLocalHabitatNameLocalName("value");		
		run ("localHabitatNameQualifierLocalName")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);
		
		feature = new Habitat();
		feature.setLocalHabitatNameLocalNameCode(new CodeType ("value","http://www.namespace.com"));		
		run ("localHabitatNameLocalName")
			.withFeature(feature )
			.assertOnlyKey (ATTRIBUTE_GROUP_INCONSISTENT);	
	}
	
	@Test	
	public void testHabitatAreaCoveredValidator () throws Exception {

		run ("habitatAreaCovered")
			.with (null)
			.assertNoMessages();
		
		run ("habitatAreaCovered")
			.with (-0.1)
			.assertOnlyKey (ATTRIBUTE_VALUE_NEGATIVE);
		
//		run ("habitatAreaCovered")
//			.with (0.0)
//			.assertNoMessages();		
	}

}
