package nl.ipo.cds.etl.theme.watercourselink;

import java.util.Collections;

import nl.ipo.cds.etl.theme.watercourselink.WatercourseLinkValidator;

import org.junit.Test;

public class ValidatorsValidTest {

	@Test
	public void testValidatorValid () throws Exception {
		new WatercourseLinkValidator (Collections.emptyMap ());
	}

}
