package nl.ipo.cds.validation.flow;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.HashMap;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.Validator;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.Executor;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public class ForEachExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T> extends AbstractExpression<K, C, Boolean> {

	public final String variableName;
	public final Expression<K, C, T[]> input;
	public final Validator<K, C> validator;
	
	public ForEachExpression (final String variableName, final Expression<K, C, T[]> input, final Validator<K, C> validator) {
		this.variableName = variableName;
		this.input = input;
		this.validator = validator;
	}

	@Override
	public Class<Boolean> getResultType () {
		return Boolean.class;
	}
	
	@Override
	public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
		// Compile the validator:
		final Compiler<C> validatorCompiler = compiler
				.addMap ("loop." + variableName + ".map")
				.addBean ("loop." + variableName + ".bean", input.getResultType ()
			);
		final Executor<C> validatorExecutor = validatorCompiler.compile (validator);
		
		// Create the executor for the for each loop:
		final ForEachExecutor<K, C, T> executor = new ForEachExecutor<K, C, T> () {
			@Override
			public Boolean execute(Object[] objects, C context, T[] input) throws Exception {
				if (input == null || input.length == 0) {
					return true;
				}
				
				final Object[] contextObjects = Arrays.copyOf (objects, objects.length + 2);
				final HashMap<String, Object> map = new HashMap<String, Object> ();
				
				contextObjects[contextObjects.length - 2] = map; 
				
				final T[] list = input;
				boolean allValid = true;
				
				for (final T item: list) {
					map.put (variableName, item);
					contextObjects[contextObjects.length - 1] = item;
					
					final Boolean result = (Boolean)validatorExecutor.execute (context, contextObjects);
					
					if (result == null || !result) {
						allValid = false;
					}
				}
				
				return allValid;
			}
			
		};
		
		return ExpressionExecutor.create (
				this, 
				input, 
				false, 
				false,
				executeHandle.bindTo (executor),
				false
			);
	}
	
	private final static MethodHandle executeHandle = Compiler
			.findMethod (
					ForEachExecutor.class, 
					"execute", 
					MethodType.methodType (Boolean.class, Object[].class, ValidatorContext.class, Object[].class)
				);
	
	public static interface ForEachExecutor<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T> {
		Boolean execute (Object[] objects, C context, T[] input) throws Exception;
	}
}
