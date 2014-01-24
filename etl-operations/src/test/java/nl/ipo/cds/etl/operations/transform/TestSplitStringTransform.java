package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.etl.operations.AbstractTestOperation;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestSplitStringTransform extends AbstractTestOperation {

	@Test
	public void testSplit () throws Exception {
		final Operation op = stringArrayOut (split (stringConstant ("Hello, world!"), ","));
		final String[] result = execute (op, String[].class);
		
		assertEquals (2, result.length);
		assertEquals ("Hello", result[0]);
		assertEquals ("world!", result[1]);
	}
	
	@Test
	public void testSplitNone () throws Exception {
		final Operation op = stringArrayOut (split (stringConstant ("Hello, world!"), ";"));
		final String[] result = execute (op, String[].class);
		
		assertEquals (1, result.length);
		assertEquals ("Hello, world!", result[0]);
	}
	
	@Test
	public void testSplitMany () throws Exception {
		final Operation op = stringArrayOut (split (stringConstant ("0, 1, 2   , 3\t , 4   , 5 , 6 \n\r, 7  , 8 , 9  "), ","));
		final String[] result = execute (op, String[].class);
		
		assertEquals (10, result.length);
		for (int i = 0; i < 10; ++ i) {
			assertEquals (String.valueOf (i), result[i]);
		}
	}
	
	@Test
	public void testSplitLongBoundary () throws Exception {
		final Operation op = stringArrayOut (split (stringConstant ("0boundary1"), "boundary"));
		final String[] result = execute (op, String[].class);
		
		assertEquals (2, result.length);
		assertEquals ("0", result[0]);
		assertEquals ("1", result[1]);
	}
}
