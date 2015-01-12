package nl.ipo.cds.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.Executor;
import nl.ipo.cds.validation.execute.ExecutorException;
import nl.ipo.cds.validation.format.HtmlFormatter;
import nl.ipo.cds.validation.gml.codelists.CodeList;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;
import nl.ipo.cds.validation.gml.codelists.StaticCodeListFactory;

import org.junit.Test;

public class TestValidator extends Validation<TestValidator.MessageKeys, TestValidator.Context> {

	@Test
	public void testEvaluate () throws Exception {
		assertExpression (constant (true), null, true);
		assertExpression (constant (false), null, false);
	}
	
	@Test
	public void testIf () throws Exception {
		assertExpression (ifExp (constant (true), constant (true), constant (false)), null, true);
		assertExpression (ifExp (constant (false), constant (true), constant (false)), null, false);
	}
	
	@Test
	public void testBeanAttribute () throws Exception {
		final Bean bean = new Bean ();
		
		bean.setBooleanValue (true);
		assertExpression (booleanAttr ("booleanValue"), bean, true);
		
		bean.setBooleanValue (false);
		assertExpression (booleanAttr ("booleanValue"), bean, false);
		
		bean.setStringValue ("Hello, World!");
		assertExpression (eq (stringAttr ("stringValue"), constant ("Hello, World!")), bean, true);
		
		bean.setDoubleValue (1.0);
		assertExpression (gte (doubleAttr ("doubleValue"), constant (0.0)), bean, true);
		
		bean.setIntValue (100);
		assertExpression (lte (intAttr ("intValue"), constant (300)), bean, true);

		bean.setIntValue (100);
		assertExpression (lte (intAttr ("intValue"), constant (300)), bean, true);
	}
	
	@Test
	public void testToString () {
		final Expression<MessageKeys, Context, Boolean> validationExpression =
			and (
				validate (
					and (
						gt (doubleAttr ("a"), constant (2.0)),
						lt (doubleAttr ("b"), constant (3.0))
					)
				),
				validate (
					and (
						gt (doubleAttr ("c"), constant (4.0)),
						lt (doubleAttr ("d"), constant (5.0))
					)
				),
				validate (not (geometry ("geometry").hasCurveDiscontinuity ())),
				validate (not (geometry ("geometry").hasExteriorRingCW ())),
				validate (not (geometry ("geometry").hasInteriorRingIntersectingExterior ()))
			);
		
		System.out.println (validationExpression.toString ());
	}
	
	@Test
	public void testHtmlFormat () {
		final Set<String> protectionClassification = new HashSet<String> (Arrays.asList(new String[] {
				"natureConservation",
				"archaeological",
				"cultural",
				"ecological",
				"landscape",
				"environment",
				"geological"
		}));
		final String protectionClassificationConcat = "natureConservation,archaeological,cultural,ecological,landscape,environment,geological";
		
		final Validator<MessageKeys, Context> validator = validate (
			and (
				validate (not (attr ("siteProtectionClassification", String[].class).isNull ())).message (MessageKeys.A),
				forEach (
					"i",
					attr ("siteProtectionClassification", String[].class),
					validate (in (stringAttr ("i"), constant (protectionClassification))).message (MessageKeys.B, stringAttr ("i"), constant (protectionClassificationConcat))
				)
			).shortCircuit ()
		);		
		
		System.out.println (validator.toString ());
		System.out.println (new HtmlFormatter<MessageKeys, Context> (validator).format ());
	}

	private static void assertExpression (final Expression<MessageKeys, Context, Boolean> expression, final Bean input, final boolean expectedResult) throws CompilerException, ExecutorException {
        final Validator<MessageKeys, Context> validator = new Validator<MessageKeys, Context> (expression, MessageKeys.A, new ArrayList<Expression<MessageKeys, Context, ?>> ());
        final Context context = new Context (new StaticCodeListFactory (Collections.<String, CodeList>emptyMap ()), null);
        
        @SuppressWarnings("unchecked")
		final Class<Context> cls = (Class<Context>)((Class<?>)ValidatorContext.class);
        final Compiler<Context> compiler = new Compiler<> (cls).addBean ("input", Bean.class);
        final Executor<Context> executor = compiler.compile (validator);

		if (expectedResult) {
			assertTrue ((Boolean)executor.execute (context, input));
		} else {
			assertFalse ((Boolean)executor.execute (context, input));
		}
	}
	
	public static enum MessageKeys implements ValidationMessage<MessageKeys, Context>{
		A,
		B,
		C;

		@Override
		public boolean isBlocking () {
			return true;
		}

		@Override
		public List<Expression<MessageKeys, Context, ?>> getMessageParameters () {
			return Collections.emptyList ();
		}
	}
	
	public static class Bean {
		private String stringValue;
		private int intValue;
		private double doubleValue;
		private boolean booleanValue;
		private BigInteger bigIntegerValue;
		
		public String getStringValue() {
			return stringValue;
		}
		
		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}
		
		public int getIntValue() {
			return intValue;
		}
		
		public void setIntValue(int intValue) {
			this.intValue = intValue;
		}
		
		public double getDoubleValue() {
			return doubleValue;
		}
		
		public void setDoubleValue(double doubleValue) {
			this.doubleValue = doubleValue;
		}
		
		public boolean getBooleanValue() {
			return booleanValue;
		}
		
		public void setBooleanValue(boolean booleanValue) {
			this.booleanValue = booleanValue;
		}

		public BigInteger getBigIntegerValue() {
			return bigIntegerValue;
		}

		public void setBigIntegerValue(BigInteger bigIntegerValue) {
			this.bigIntegerValue = bigIntegerValue;
		}
	}
	
	public static class Context extends DefaultValidatorContext<MessageKeys, Context> {
		public Context (final CodeListFactory codeListFactory, final ValidationReporter<MessageKeys, Context> reporter) {
			super (codeListFactory, reporter);
		}
	}
}
