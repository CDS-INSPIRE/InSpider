package nl.ipo.cds.validation.flow;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.Validator;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.Executor;
import nl.ipo.cds.validation.execute.ExecutorException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public class SplitExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractExpression<K, C, Boolean> implements Expression<K, C, Boolean> {

	final Expression<K, C, String> input;
	final Expression<K, C, String> splitter;
	final Validator<K, C> validator;
	
	public SplitExpression (final Expression<K, C, String> input, final Expression<K, C, String> splitter, final Validator<K, C> validator) {
		if (input == null) {
			throw new NullPointerException ("input cannot be null");
		}
		if (splitter == null) {
			throw new NullPointerException ("splitter cannot be null");
		}
		if (validator == null) {
			throw new NullPointerException ("validator cannot be null");
		}
		
		this.input = input;
		this.splitter = splitter;
		this.validator = validator;
	}

	@Override
	public Class<Boolean> getResultType () {
		return Boolean.class;
	}

	@Override
	public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
		// Compile the validator:
		final Executor<C> validatorExecutor = compiler
			.addBean ("splitResult", SplitBean.class)
			.compile (validator);
		
		// Create an executor for the split operation:
		final SplitExecutor<K, C> executor = new SplitExecutor<K, C> () {
			@Override
			public Boolean execute (final Object[] objects, final C context, final String input, final String splitter) throws ExecutorException {
				final String inputValue = input;
				final String splitterValue = splitter;
				final String[] list;
				
				// Create a list of strings:
				if (inputValue == null) {
					list = new String[0];
				} else if (splitterValue == null) {
					list = new String[] { inputValue };
				} else {
					list = inputValue.split (splitterValue);
				}

				final Object[] contextObjects = Arrays.copyOf (objects, objects.length + 1);
				contextObjects[contextObjects.length - 1] = new SplitBean<K> (list);
				
				return (Boolean)validatorExecutor.execute (context, contextObjects);
			}
		};
		
		return ExpressionExecutor.create (
				this, 
				input, 
				splitter, 
				false, 
				false, 
				executeHandle.bindTo (executor),
				false
			);
	}
	
	public static class SplitBean<K extends Enum<K>> {
		private final String[] values;
		
		public SplitBean (final String[] values) {
			this.values = values;
		}

		public Integer getLength () {
			return values.length;
		}
		
		public String[] getValues () {
			return values;
		}
		
		public String get (int i) {
			return i >= 0 && i < values.length ? values[i] : null;
		}
	}
	
	private final static MethodHandle executeHandle = Compiler
			.findMethod (
					SplitExecutor.class, 
					"execute", 
					MethodType.methodType (Boolean.class, Object[].class, ValidatorContext.class, String.class, String.class)
				);
	
	public static interface SplitExecutor<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> {
		Boolean execute (Object[] objects, C context, String input, String splitter) throws ExecutorException;
	}
}
