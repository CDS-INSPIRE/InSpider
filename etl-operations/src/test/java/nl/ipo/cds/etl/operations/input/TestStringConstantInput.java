package nl.ipo.cds.etl.operations.input;

import static org.junit.Assert.*;
import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.etl.operations.AbstractTestOperation;

import org.junit.Test;

public class TestStringConstantInput extends AbstractTestOperation {

	@Test
	public void testStringConstantInput () throws Exception {
		final Operation op = stringOut (stringConstant ("Hello, world!"));
		
		assertEquals ("Hello, world!", execute (op, String.class));
	}

	@Test
	public void testStringConstantEmpty () throws Exception {
		final Operation op = stringOut (stringConstant (""));
		
		assertEquals ("", execute (op, String.class));
	}

	@Test (expected = NullPointerException.class)
	public void testStringConstantNull () throws Exception {
		final Operation op = stringOut (stringConstant (null));
		
		execute (op, String.class);
	}
}
