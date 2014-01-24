package nl.ipo.cds.etl.theme.riskzone;

import java.util.Collections;

import nl.ipo.cds.etl.theme.riskzone.RiskZoneValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new RiskZoneValidator (Collections.emptyMap ());
	}

}
