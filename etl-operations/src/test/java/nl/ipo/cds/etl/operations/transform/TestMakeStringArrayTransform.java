package nl.ipo.cds.etl.operations.transform;

import static org.junit.Assert.*;

import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.etl.operations.AbstractTestOperation;

import org.junit.Test;

public class TestMakeStringArrayTransform extends AbstractTestOperation {

	@Test
	public void testMakeString () throws Exception {
		final Operation op = stringArrayOut (
				makeStringArray (
					stringConstant ("A"),
					stringConstant ("B"),
					stringConstant ("C"),
					stringConstant ("D")
				)
			);
		
		final String[] values = execute (op, String[].class);
		
		assertEquals (4, values.length);
		assertEquals ("A", values[0]);
		assertEquals ("B", values[1]);
		assertEquals ("C", values[2]);
		assertEquals ("D", values[3]);
	}
}
