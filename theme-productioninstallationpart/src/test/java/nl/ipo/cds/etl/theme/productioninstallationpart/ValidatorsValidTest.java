package nl.ipo.cds.etl.theme.productioninstallationpart;

import java.util.Collections;

import nl.ipo.cds.etl.theme.productioninstallationpart.ProductionInstallationPartValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new ProductionInstallationPartValidator (Collections.emptyMap ());
	}

}
