package nl.ipo.cds.etl.theme.hydronode;

import java.util.Collections;

import nl.ipo.cds.etl.theme.hydronode.HydroNodeValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new HydroNodeValidator (Collections.emptyMap ());
	}

}
