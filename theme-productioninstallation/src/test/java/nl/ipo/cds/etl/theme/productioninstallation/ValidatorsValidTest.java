package nl.ipo.cds.etl.theme.productioninstallation;

import java.util.Collections;

import nl.ipo.cds.etl.theme.productioninstallation.ProductionInstallationValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new ProductionInstallationValidator (Collections.emptyMap ());
	}

}
