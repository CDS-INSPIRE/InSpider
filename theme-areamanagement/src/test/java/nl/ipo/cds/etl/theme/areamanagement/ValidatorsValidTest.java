package nl.ipo.cds.etl.theme.areamanagement;

import java.util.Collections;

import nl.ipo.cds.etl.theme.areamanagement.AreaManagementValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new AreaManagementValidator (Collections.emptyMap ());
	}

}
