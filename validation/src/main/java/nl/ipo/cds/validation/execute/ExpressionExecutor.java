package nl.ipo.cds.validation.execute;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Method can have one of the following signatures:
 * - (inputType[0], (inputType[1] ...))
 * - C, (inputType[0], (inputType[1] ...))
 *   C is the context object for the executor.
 * - Object[]
 *   The beans that provide input (attributes) for use within the expression are added in the order they
 *   have been declared when compiling the expression.
 * - Object[], C, (inputType[0], (inputType[1] ...))
 * 
 * @author erik
 *
 * @param <C>
 */
public class ExpressionExecutor<C> {

	public final ExecutableExpression<C, ?> expression;
	public final List<ExecutableExpression<C, ?>> inputs;
	public final boolean isConstant;
	public final boolean isDeterministic;
	public final MethodHandle methodHandle;
	
	final boolean addContextObjects;
	final boolean addContext;
	final boolean useVarargs;
	final Class<?> varargsType;
	
	final int argumentsArrayLength;

	public ExpressionExecutor (
			final ExecutableExpression<C, ?> expression, 
			final List<ExecutableExpression<C, ?>> inputs, 
			final boolean isConstant, 
			final boolean isDeterministic,
			final MethodHandle methodHandle,
			final boolean isVarArgs) throws CompilerException {
		
		this.expression = expression;
		this.inputs = Collections.unmodifiableList (new ArrayList<ExecutableExpression<C, ?>> (inputs));
		this.isConstant = isConstant;
		this.isDeterministic = isDeterministic;
		this.methodHandle = methodHandle;

		
		// Precalculate several properties of the evaluation method:
		final MethodType type = methodHandle.type ();
		final Class<?>[] types = type.parameterList().toArray (new Class<?>[0]);
		int n = 0;
		
		if (types.length > 0 && types[n].equals (Object[].class)) {
			addContextObjects = true;
			++ n;
		} else {
			addContextObjects = false;
		}
		
		final Class<?> contextClass = expression.getContextType ();
		if (n < types.length && contextClass.isAssignableFrom (types[n])) {
			addContext = true;
			++ n;
		} else {
			addContext = false;
		}
		
		if (n == types.length - 1 && isVarArgs) {
			// Using varargs, inputs are passed as an array:
			useVarargs = true;
			varargsType = types[types.length - 1].getComponentType ();
		} else {
			useVarargs = false;
			varargsType = null;
			
			// Not using varargs, inputs should be passed as arguments:
			if (n + inputs.size () != types.length) {
				throw new IllegalArgumentException (String.format ("Input count %d does not match argument count of %s", inputs.size (), methodHandle));
			}
			
			for (int i = 0; i < inputs.size (); ++ i) {
				final ExecutableExpression<C, ?> input = inputs.get (i);
				final Class<?> inputType = input.getResultType ();
				
				if (!types[n + i].isAssignableFrom (inputType)) {
					throw new IllegalArgumentException (String.format ("Argument %d of %s is of type %s while the corresponding input is of type %s", n + i, expression.toString (), types[n + i], inputType));
				}
			}
		}
		
		// Precalculate and store the length of the arguments array:
		argumentsArrayLength = 
				(addContextObjects ? 1 : 0)
				+ (addContext ? 1 : 0)
				+ (useVarargs ? 1 : inputs.size ());
	}
	
	public static <C> ExpressionExecutor<C> create (
			final ExecutableExpression<C, ?> expression, 
			final boolean isConstant,
			final boolean isDeterministic,
			final MethodHandle methodHandle,
			final boolean isVarArgs) throws CompilerException {
			
		final List<ExecutableExpression<C, ?>> inputs = new ArrayList<ExecutableExpression<C,?>> (0);
		
		return new ExpressionExecutor<C> (expression, inputs, isConstant, isDeterministic, methodHandle, isVarArgs);
	}
	
	public static <C> ExpressionExecutor<C> create (
		final ExecutableExpression<C, ?> expression, 
		final ExecutableExpression<C, ?> input,
		final boolean isConstant,
		final boolean isDeterministic,
		final MethodHandle methodHandle,
		final boolean isVarArgs) throws CompilerException {
		
		final List<ExecutableExpression<C, ?>> inputs = new ArrayList<ExecutableExpression<C,?>> (1);
		inputs.add (input);
		
		return new ExpressionExecutor<C> (expression, inputs, isConstant, isDeterministic, methodHandle, isVarArgs);
	}
	
	public static <C> ExpressionExecutor<C> create (
			final ExecutableExpression<C, ?> expression, 
			final ExecutableExpression<C, ?> a,
			final ExecutableExpression<C, ?> b,
			final boolean isConstant,
			final boolean isDeterministic,
			final MethodHandle methodHandle,
			final boolean isVarArgs) throws CompilerException {
			
		final List<ExecutableExpression<C, ?>> inputs = new ArrayList<ExecutableExpression<C,?>> (2);
		inputs.add (a);
		inputs.add (b);
		
		return new ExpressionExecutor<C> (expression, inputs, isConstant, isDeterministic, methodHandle, isVarArgs);
	}
	
	@Override
	public String toString () {
		return expression.toString () + (isConstant ? " (constant)" : "") + (isDeterministic ? " (deterministic)" : ""); 
	}
}
