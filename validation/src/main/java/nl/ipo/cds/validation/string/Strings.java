package nl.ipo.cds.validation.string;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import nl.ipo.cds.validation.AbstractBinaryTestExpression;
import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.AbstractUnaryTestExpression;
import nl.ipo.cds.validation.BinaryExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.UnaryExpression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

import org.apache.commons.lang.StringUtils;

public class Strings {

	public static class IsBlank<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractUnaryTestExpression<K, C, String> {
		public IsBlank (final Expression<K, C, String> input) {
			super(input, "Strings.IsBlank");
		}

		@Override
		public boolean test (String value, C context) {
			return value == null || StringUtils.isBlank (value);
		}
	}
	
	public static class IsUrl<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractUnaryTestExpression<K, C, String> {
		public IsUrl (final Expression<K, C, String> input) {
			super(input, "Strings.IsUrl");
		}

		@Override
		public boolean test (String value, C context) {
			try {
				new URL (value);
				return true;
			} catch (MalformedURLException e) {
				return false;
			}
		}
	}
	
	public static class IsUUID<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractUnaryTestExpression<K, C, String> {
		public IsUUID (final Expression<K, C, String> input) {
			super (input, "Strings.IsUUID");
		}
		
		@Override
		public boolean test (final String value, final C context) {
			try {
				UUID.fromString (value);
			} catch(IllegalArgumentException e) {
				return false;
			}
			return true;
		}
	}

	public static class Matches<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractBinaryTestExpression<K, C, String, String> {
		public Matches (final Expression<K, C, String> input, final Expression<K, C, String> pattern) {
			super (input, pattern, "Strings.Matches");
		}

		@Override
		public boolean test (final String input, String pattern, final C context) {
			if (input == null || pattern == null) {
				return false;
			}
			
			return input.matches (pattern);
		}
	}
	
	public static class Length<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractExpression<K, C, Integer> implements UnaryExpression<K, C, Integer, String> {
		final Expression<K, C, String> input;

		public Length (final Expression<K, C, String> input) {
			this.input = input;
		}
		
		@Override
		public Class<Integer> getResultType () {
			return Integer.class;
		}

		@Override
		public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
			try {
				return ExpressionExecutor.create (
						this, 
						input, 
						false, 
						true, 
						MethodHandles.lookup ().findVirtual (String.class, "length", MethodType.methodType (Integer.TYPE)), 
						false
					);
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw new CompilerException (e);
			}
		}

		@Override
		public Class<String> getInputType () {
			return input.getResultType ();
		}
	}
	
	public static class Join<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractExpression<K, C, String> implements BinaryExpression<K, C, String, String[], String> {
		final Expression<K, C, String[]> input;
		final Expression<K, C, String> separator;
		
		public Join (final Expression<K, C, String[]> input, final Expression<K, C, String> separator) {
			if (input == null) {
				throw new NullPointerException ("input cannot be null");
			}
			if (separator == null) {
				throw new NullPointerException ("separator cannot be null");
			}
			
			this.input = input;
			this.separator = separator;
		}
		
		@Override
		public Class<String> getResultType () {
			return String.class;
		}

		public String evaluate (final ValidatorContext<K, C> context, final String[] input, final String rawSeparator) {
			final String[] list = input;
			final String separator = rawSeparator == null ? "" : rawSeparator;
			
			if (list == null) {
				return "";
			}
			
			final StringBuilder builder = new StringBuilder ();
			for (final String s: list) {
				if (builder.length () > 0) {
					builder.append (separator);
				}
				builder.append (s);
			}
			
			return builder.toString ();
		}

		@Override
		public Class<String[]> getTypeA () {
			return String[].class;
		}

		@Override
		public Class<String> getTypeB () {
			return String.class;
		}

		private final static MethodHandle evaluateHandle = Compiler
				.findMethod (
						Join.class, 
						"evaluate", 
						MethodType.methodType (String.class, ValidatorContext.class, String[].class, String.class)
					);
		
		@Override
		public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
			return ExpressionExecutor.create (
				this, 
				input,
                separator,
				false, 
				true, 
				evaluateHandle.bindTo (this),
				false
			);
		}
	}
}
