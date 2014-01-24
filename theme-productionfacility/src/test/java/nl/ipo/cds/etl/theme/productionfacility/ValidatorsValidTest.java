package nl.ipo.cds.etl.theme.productionfacility;

import java.util.Collections;

import nl.ipo.cds.etl.theme.productionfacility.ProductionFacilityValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new ProductionFacilityValidator (Collections.emptyMap ());
	}

}
