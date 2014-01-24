package nl.ipo.cds.etl.theme.hazardarea;

import java.util.Collections;

import nl.ipo.cds.etl.theme.hazardarea.HazardAreaValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new HazardAreaValidator (Collections.emptyMap ());
	}

}
