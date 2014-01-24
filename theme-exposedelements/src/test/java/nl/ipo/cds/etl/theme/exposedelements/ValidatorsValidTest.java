package nl.ipo.cds.etl.theme.exposedelements;

import java.util.Collections;

import nl.ipo.cds.etl.theme.exposedelements.ExposedElementsValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new ExposedElementsValidator (Collections.emptyMap ());
	}

}
