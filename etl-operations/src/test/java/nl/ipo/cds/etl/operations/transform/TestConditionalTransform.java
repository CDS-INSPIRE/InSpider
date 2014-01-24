package nl.ipo.cds.etl.operations.transform;

import static org.junit.Assert.assertEquals;
import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.etl.operations.AbstractTestOperation;

import org.junit.Test;

import static nl.ipo.cds.etl.operations.transform.ConditionalTransform.Operation.*;

public class TestConditionalTransform extends AbstractTestOperation {

	@Test
	public void testConditional () throws Exception {
		final Operation op = stringOut (
				convertToString (
					conditional (
						stringConstant ("branch1"),
						conditionalInput (
							stringConstant ("branch0"),
							"inValue",
							IN,
							"a"
						)
					)
				)
			);
		
		assertEquals ("branch0", execute (op, String.class, "inValue", "a"));
	}

	@Test
	public void testConditionalElse () throws Exception {
		final Operation op = stringOut (
				convertToString (
					conditional (
						stringConstant ("branch1"),
						conditionalInput (
							stringConstant ("branch0"),
							"inValue",
							IN,
							"a"
						)
					)
				)
			);
		
		assertEquals ("branch1", execute (op, String.class, "inValue", "b"));
	}
	
	@Test
	public void testConditionalNoCondition () throws Exception {
		final Operation op = stringOut (
				convertToString (
					conditional (
						stringConstant ("branch1")
					)
				)
			);
		
		assertEquals ("branch1", execute (op, String.class, "inValue", "b"));
		
	}
	
	@Test
	public void testConditionalNotIn () throws Exception {
		final Operation op = stringOut (
				convertToString (
					conditional (
						stringConstant ("branch1"),
						conditionalInput (
							stringConstant ("branch0"),
							"inValue",
							NOT_IN,
							"b"
						)
					)
				)
			);
		
		assertEquals ("branch0", execute (op, String.class, "inValue", "a"));
	}
	
	@Test
	public void testConditionalIsEmpty () throws Exception {
		final Operation op = stringOut (
				convertToString (
					conditional (
						stringConstant ("branch1"),
						conditionalInput (
							stringConstant ("branch0"),
							"inValue",
							IS_EMPTY
						)
					)
				)
			);
		
		assertEquals ("branch0", execute (op, String.class, "inValue", ""));
		assertEquals ("branch0", execute (op, String.class, "inValue", null));
	}
	
	@Test
	public void testConditionalIsNotEmpty () throws Exception {
		final Operation op = stringOut (
				convertToString (
					conditional (
						stringConstant ("branch1"),
						conditionalInput (
							stringConstant ("branch0"),
							"inValue",
							IS_NOT_EMPTY
						)
					)
				)
			);
		
		assertEquals ("branch0", execute (op, String.class, "inValue", "a"));
		assertEquals ("branch0", execute (op, String.class, "inValue", 1));
		assertEquals ("branch1", execute (op, String.class, "inValue", ""));
		assertEquals ("branch1", execute (op, String.class, "inValue", null));
	}
	
	@Test
	public void testConditionalIsNull () throws Exception {
		final Operation op = stringOut (
				convertToString (
					conditional (
						stringConstant ("branch1"),
						conditionalInput (
							stringConstant ("branch0"),
							"inValue",
							IS_NULL
						)
					)
				)
			);
		
		assertEquals ("branch1", execute (op, String.class, "inValue", ""));
		assertEquals ("branch0", execute (op, String.class, "inValue", null));
	}
	
	@Test
	public void testConditionalIsNotNull () throws Exception {
		final Operation op = stringOut (
				convertToString (
					conditional (
						stringConstant ("branch1"),
						conditionalInput (
							stringConstant ("branch0"),
							"inValue",
							IS_NULL
						)
					)
				)
			);
		
		assertEquals ("branch1", execute (op, String.class, "inValue", ""));
		assertEquals ("branch0", execute (op, String.class, "inValue", null));
		assertEquals ("branch1", execute (op, String.class, "inValue", "a"));
		assertEquals ("branch1", execute (op, String.class, "inValue", 1));
	}
	
	@Test
	public void testConditionalGeometryType () throws Exception {
	}
}
