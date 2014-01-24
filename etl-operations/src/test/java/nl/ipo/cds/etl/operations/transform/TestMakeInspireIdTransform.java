package nl.ipo.cds.etl.operations.transform;

import static org.junit.Assert.assertEquals;
import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.etl.operations.AbstractTestOperation;

import org.junit.Test;

public class TestMakeInspireIdTransform extends AbstractTestOperation {

	@Test
	public void testMakeInspireIdTransform () throws Exception {
		final Operation op = stringOut (
				makeInspireId (
					stringConstant ("NL"),
					stringConstant ("1234"),
					stringConstant ("abcd"),
					stringConstant ("42")
				)
			);
		
		assertEquals ("NL.1234.abcd.42", execute (op, String.class));
	}
}