package nl.ipo.cds.etl.theme.habitat;

import java.util.Collections;

import nl.ipo.cds.etl.theme.habitat.HabitatValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new HabitatValidator (Collections.emptyMap ());
	}

}
