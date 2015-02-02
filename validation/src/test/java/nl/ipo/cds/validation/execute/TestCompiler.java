package nl.ipo.cds.validation.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.ipo.cds.validation.DefaultValidatorContext;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.Validation;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidationReporter;
import nl.ipo.cds.validation.Validator;
import nl.ipo.cds.validation.gml.codelists.CodeList;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;
import nl.ipo.cds.validation.gml.codelists.StaticCodeListFactory;

import org.junit.Before;
import org.junit.Test;

public class TestCompiler extends Validation<TestCompiler.MessageKeys, TestCompiler.Context> {

	private final int N = 10000;
	
	private Compiler<Context> compiler;
	
	@Before
	public void createCompiler () {
		compiler = new Compiler<Context> (Context.class)
				.addBean ("bean", Bean.class);
	}
	
	@Test
	public void testCompile () throws Exception {
		System.out.println (compiler.compile (constant ("Hello, world!")));
		System.out.println (compiler.compile (gt (constant (1.0), constant (2.0))));
		System.out.println (compiler.compile (stringAttr ("b")));
	}
	
	@Test
	public void testMergeConstants () throws Exception {
		final Executor<Context> executor = compiler.compile (eq (constant (1), constant (1)));
		
		// The plan should only have two steps: the constant expressions are merged.
		assertEquals (2, executor.plan.getExecutionSteps ().size ());
	}
	
	@Test
	public void testMergeAttributes () throws Exception {
		final Executor<Context> executor = compiler.compile (eq (stringAttr ("b"), stringAttr ("b")));
		
		// The plan should only have three steps: the attribute references are merged.
		// 1) Get unboxed attribute value.
		// 2) Box attribute value (in Optional).
		// 3) Equals.
		System.out.println (executor);
		assertEquals (2, executor.plan.getExecutionSteps ().size ());
	}
	
	@Test
	public void testExecute () throws Exception {
		assertEquals ("Hello, world!", execute (constant ("Hello, world!")));
		assertEquals (true, execute (gt (constant (2.0), constant (1.0))));
		assertEquals (true, execute (validate (gt (constant (2.0), constant (1.0)))));
		assertEquals (false, execute (validate (gt (constant (1.0), constant (2.0)))));
	}
	
	@Test
	public void testAccessAttribute () throws Exception {
		assertEquals ("Hello, World!", execute (stringAttr ("b")));
		assertEquals (42, execute (intAttr ("a")));
		assertEquals (1.0, (double)((Float)execute (floatAttr ("c"))), 0.01);
		assertEquals(BigInteger.valueOf(123),execute(bigIntegerAttr("f")));
	}
	
	@Test
	public void testAccessContextAttribute () throws Exception {
		assertEquals ("Hello, World!", execute (stringAttr ("contextProperty")));
	}
	
	@Test
	public void testIf () throws Exception {
		assertEquals (true, execute (ifExp (constant (true), constant (true), constant (false))));
		assertEquals (false, execute (ifExp (constant (false), constant (true), constant (false))));
		
		assertEquals (true, execute (ifExp (booleanAttr ("d"), booleanAttr ("d"), constant (false))));
	}
	
	@Test
	public void testAnd () throws Exception {
		assertEquals (true, execute (and (constant (true), constant (true), constant (true))));
		assertEquals (false, execute (and (constant (true), constant (false), constant (true))));
		assertEquals (false, execute (and (constant (false), constant (false), constant (false))));
	}
	
	@Test
	public void testAndShortCircuit () throws Exception {
		assertEquals (true, execute (and (constant (true), constant (true), constant (true)).shortCircuit ()));
		assertEquals (false, execute (and (constant (true), constant (false), constant (true)).shortCircuit ()));
		assertEquals (false, execute (and (constant (false), constant (false), constant (false)).shortCircuit ()));
		
		// Test short-circuited and operators nested in another operator. These should
		// work even though the and is internally replaced by a sequence of if-else operations:
		assertEquals (true, execute (
				validate (
					and (
						validate (
							constant (true)
						), 
						validate (
							constant (true)
						), 
						validate (
							constant (true)
						)
					).shortCircuit ()
				)
			));
	}
	
	@Test
	public void testOr () throws Exception {
		assertEquals (true, execute (or (constant (true), constant (false), constant (true))));
		assertEquals (true, execute (or (constant (true), constant (true), constant (true))));
		assertEquals (false, execute (or (constant (false), constant (false), constant (false))));
	}
	
	@Test
	public void testForEach () throws Exception {
		assertEquals (true, execute (forEach ("i", attr ("e", Integer[].class), validate (gt (intAttr ("i"), constant (0))))));
	}
	
	@Test
	public void testSplit () throws Exception {
		assertEquals (true, execute (validate (eq (constant (2), constant (2)))));
		assertEquals (true, execute (split (constant ("a|b"), constant ("\\|"), validate (eq (intAttr ("length"), constant (2))))));
		assertEquals (true, execute (split (constant ("a|b"), constant ("\\|"), validate (eq (stringAttr ("0"), constant ("a"))))));
		assertEquals (true, execute (split (constant ("a|b"), constant ("\\|"), validate (eq (stringAttr ("1"), constant ("b"))))));
		assertEquals (true, execute (split (constant ("a|b"), constant ("\\|"), validate (eq (join (attr ("values", String[].class), constant ("|")), constant ("a|b"))))));
	}
	
	@Test
	public void testFoldCommonUnarySubexpressions () throws Exception {
		final Executor<Context> executor = compiler.compile (and (stringAttr ("b").isNull (), stringAttr ("b").isNull ()));
		
		// The plan should have 4 expressions. The second isNull expression should be folded with the first.
		System.out.println (executor);
		assertEquals (3, executor.plan.getExecutionSteps ().size ());
	}
	
	@Test
	public void testFoldCommonBinarySubexpressions () throws Exception {
		final Set<String> values = new HashSet<String> ();
		
		final Executor<Context> executor = compiler.compile (and (in (stringAttr ("b"), constant (values)), in (stringAttr ("b"), constant (values))));
		
		// The plan should have 5 expressions. The second In expression should be folded with the first.
		System.out.println (executor);
		assertEquals (4, executor.plan.getExecutionSteps ().size ());
	}
	
	@Test
	public void testFoldCompareOperators () throws Exception {
		final Executor<Context> executor = compiler.compile (and (gt (intAttr ("a"), intAttr ("a")), gt (intAttr ("a"), intAttr ("a"))));
		
		System.out.println (executor);
		assertEquals (3, executor.plan.getExecutionSteps ().size ());
	}
	
	@Test
	public void testFoldNot () throws Exception {
		final Executor<Context> executor = compiler.compile (and (not (booleanAttr ("d")), not (booleanAttr ("d"))));
		
		System.out.println (executor);
		assertEquals (3, executor.plan.getExecutionSteps ().size ());
	}
	
	@Test
	public void testManyObjects () throws Exception {
		final Validator<MessageKeys, Context> validator = validate (
				and (
					gt (intAttr ("a"), constant (100)),
					not (eq (stringAttr ("b"), constant ("Hello, World!"))),
					not (not (booleanAttr ("d"))),
					forEach ("i", attr ("e", Integer[].class), validate (
						gt (intAttr ("i"), constant (1000))
					)),
					split (stringAttr ("b"), constant (","), validate (
						not (eq (stringAttr ("0"), constant ("Hello")))
					)),
					or (
						not (stringAttr ("b").isNull ()),
						not (intAttr ("a").isNull ()),
						and (
							not (floatAttr ("c").isNull ()),
							isUrl (stringAttr ("b")),
							isUUID (stringAttr ("b"))
						)
					)
				)
			);
		final Executor<Context> executor = compiler.compile (validator);

		{
			final double startTime = System.currentTimeMillis ();
			
			for (int i = 0; i < N; ++ i) {
				final Bean bean = createBean (i);
				
				final Context context = new Context (new StaticCodeListFactory (Collections.<String, CodeList>emptyMap ()), null);
				executor.execute (context, bean);
			}
			
			final double endTime = System.currentTimeMillis ();
			
			System.out.println (String.format ("%d iterations of interpreted validator in %f seconds", N, (endTime - startTime) / 1000.0));
		}
	}
	
	private Bean createBean (int i) {
		final Bean bean = new Bean ();
		
		bean.setA (i);
		bean.setB ("Hello, World! " + i);
		bean.setC (i * 2.0f);
		bean.setD (i % 2 == 0);
		bean.setE (new Integer[] { 1, 2, 3, 4, i });
		bean.setF(BigInteger.valueOf(i));
		
		return bean;
	}
	
	@Test
	public void testExecuteAsInterface () throws Exception {
		final Bean bean = new Bean ();
		bean.setD (true);
		
		final Context context = new Context (new StaticCodeListFactory (Collections.<String, CodeList>emptyMap ()), null);
		final ExecuteInterface executor = compiler.compile (booleanAttr ("d"), ExecuteInterface.class);
		
		assertTrue (executor.execute (context, bean));
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testExecuteAsInterfaceInvalidType () throws Exception {
		final Bean bean = new Bean ();
		bean.setD (false);
		
		final Context context = new Context (new StaticCodeListFactory (Collections.<String, CodeList>emptyMap ()), null);
		final ExecuteInterface executor = compiler.compile (intAttr ("a"), ExecuteInterface.class);
		
		executor.execute (context, bean);
	}
	
	@Test
	public void testValidateCodeSpace () throws Exception {
		final Bean bean = new Bean ();
		
		final Map<String, CodeList> codeLists = new HashMap<> ();
		final Set<String> codes = new HashSet<> ();
		codes.add ("a");
		codes.add ("b");
		codes.add ("c");
		final CodeList codeList = new StaticCodeListFactory.StaticCodeList ("http://www.idgis.nl/codes", codes);
		codeLists.put ("http://www.idgis.nl/codes", codeList);
		
		final Context context = new Context (new StaticCodeListFactory (codeLists), null);
		final ExecuteInterface executor = compiler.compile (codeSpace (constant ("http://www.idgis.nl/codes")), ExecuteInterface.class);
		
		assertTrue (executor.execute (context, bean));
	}
	
	@Test
	public void testValidateCodeSpaceInvalid () throws Exception {
		final Bean bean = new Bean ();
		
		final Map<String, CodeList> codeLists = new HashMap<> ();
		final Set<String> codes = new HashSet<> ();
		codes.add ("a");
		codes.add ("b");
		codes.add ("c");
		final CodeList codeList = new StaticCodeListFactory.StaticCodeList ("http://www.idgis.nl/codes", codes);
		codeLists.put ("http://www.idgis.nl/codes", codeList);
		
		final Context context = new Context (new StaticCodeListFactory (codeLists), null);
		final ExecuteInterface executor = compiler.compile (codeSpace (constant ("http://www.idgis.nl/codes2")), ExecuteInterface.class);
		
		assertFalse (executor.execute (context, bean));
	}
	
	public Object execute (final Expression<MessageKeys, Context, ?> expression) throws Exception {
		final Bean bean = new Bean ();
		
		bean.setA (42);
		bean.setB ("Hello, World!");
		bean.setC (1.0f);
		bean.setD (true);
		bean.setE (new Integer[] { 1, 2, 3, 4 });
		bean.setF(BigInteger.valueOf(123));
		
		final Context context = new Context (new StaticCodeListFactory (Collections.<String, CodeList>emptyMap ()), null);
		context.setContextProperty ("Hello, World!");
		final Executor<Context> executor = compiler.compile (expression);
		
		return executor.execute (context, bean);
	}

	public static enum MessageKeys implements ValidationMessage<MessageKeys, Context> {
		A,
		B,
		C,
		D,
		E;

		@Override
		public boolean isBlocking () {
			return true;
		}

		@Override
		public List<Expression<MessageKeys, Context, ?>> getMessageParameters () {
			return Collections.emptyList ();
		}
	}
	
	public static interface ExecuteInterface {
		Boolean execute (Context context, Bean bean);
	}
	
	public static class Context extends DefaultValidatorContext<MessageKeys, Context> {
		public Context (final CodeListFactory codeListFactory, final ValidationReporter<MessageKeys, Context> reporter) {
			super (codeListFactory, reporter);
		}

		private String contextProperty;

		public String getContextProperty () {
			return contextProperty;
		}

		public void setContextProperty (final String contextProperty) {
			this.contextProperty = contextProperty;
		}
	}
	
	public static class Bean {
		private int a;
		private String b;
		private float c;
		private boolean d;
		private Integer[] e;

		public BigInteger getF() {
			return f;
		}

		public void setF(BigInteger f) {
			this.f = f;
		}

		private BigInteger f;
		
		public int getA() {
			return a;
		}
		
		public void setA(int a) {
			this.a = a;
		}
		
		public String getB() {
			return b;
		}
		
		public void setB(String b) {
			this.b = b;
		}
		
		public float getC() {
			return c;
		}
		
		public void setC(float c) {
			this.c = c;
		}

		public boolean getD() {
			return d;
		}

		public void setD(boolean d) {
			this.d = d;
		}

		public Integer[] getE() {
			return e;
		}

		public void setE(Integer[] e) {
			this.e = e;
		}
	}
}
