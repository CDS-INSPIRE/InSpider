package nl.ipo.cds.validation.execute;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Executor<C> {

	private final static MethodHandle executeHandle = Compiler.findMethod (
			Executor.class, 
			"execute", 
			MethodType.methodType (Object.class, Object.class, Object[].class)
		);
	
	public final Class<? extends C> contextClass;
	public final List<Class<?>> inputClasses;
	public final ExecutionPlan<C> plan;
	
	Executor (final Class<? extends C> contextClass, final List<Class<?>> inputClasses, final ExecutionPlan<C> plan) {
		this.contextClass = contextClass;
		this.inputClasses = new ArrayList<> (inputClasses);
		this.plan = plan;
	}
	
	public Object execute (final C context, final Object ... inputs) throws ExecutorException {
		final List<ExecutionStep<C>> steps = plan.getExecutionSteps ();
		
		// Create a "register" array to hold the result of previous computations:
		final Object[] registers = new Object[steps.size ()];
		
		// Execute the plan:
		try {
			for (int i = 0, n = steps.size (); i < n; ++ i) {
				final ExecutionStep<C> step = steps.get (i);
				final ExpressionExecutor<C> executor = step.executor;
				final int[] inputSteps = step.inputs;
				
				final Object[] arguments = new Object[executor.argumentsArrayLength];
				
				// Add the context objects if the executor requests them (first argument is of type Object[]):
				int index = 0;
				if (executor.addContextObjects) {
					arguments[index ++] = inputs;
				}
				
				// Add the context if the executor requests it (first or second argument is of type C):
				if (executor.addContext) {
					arguments[index ++] = context;
				}
				
				// Collect and add inputs after the context objects and the context as arguments to the method, or as a single varargs argument:
				final int argumentCount = inputSteps.length;
				if (executor.useVarargs) {
					final Object[] varargs = (Object[])Array.newInstance (executor.varargsType, argumentCount);
					for (int j = 0; j < argumentCount; ++ j) {
						varargs[j] = registers[inputSteps[j]];
					}
					arguments[index ++] = varargs;
				} else {
					for (int j = 0; j < argumentCount; ++ j) {
						arguments[index ++] = registers[inputSteps[j]];
					}
				}
				
				registers[i] = executor.methodHandle.invokeWithArguments (arguments);
			}
		} catch (Throwable e) {
			throw new ExecutorException (e);
		}
		
		return registers.length == 0 ? null : registers[registers.length - 1];
	}

	public <I> I forInterface (final Class<I> iface) {
		if (iface == null) {
			throw new IllegalArgumentException ("iface cannot be null");
		}
		if (!iface.isInterface ()) {
			throw new IllegalArgumentException (String.format ("%s must be an interface", iface.toString ()));
		}
		
		final Method[] methods = iface.getMethods ();
		if (methods.length != 1) {
			throw new IllegalArgumentException (String.format ("%s must have exactly one method", iface.toString ()));
		}
		
		final Method method = methods[0];
		final ExecutableExpression<C, ?> lastExpression = plan.getExecutionSteps().get (plan.getExecutionSteps ().size () - 1).executor.expression;

		// Return types must match:
		if (!lastExpression.getResultType ().equals (method.getReturnType ())) {
			throw new IllegalArgumentException (String.format (
					"Method %s has unexpected return type %s, while expecting %s", 
					method.toString (), 
					lastExpression.getResultType ().toString (), 
					method.getReturnType ().toString ()
				));
		}
		
		// Interface must have proper input types:
		final Class<?>[] parameterTypes = method.getParameterTypes ();
		if (parameterTypes.length != inputClasses.size () + 1) {
			throw new IllegalArgumentException (String.format (
					"Method %s has an invalid number of arguments %d, expected %d",
					method.toString (),
					parameterTypes.length,
					inputClasses.size () + 1
				));
		}
		if (!parameterTypes[0].isAssignableFrom (contextClass)) {
			throw new IllegalArgumentException (String.format (
					"First parameter of %s must is of unexpected type %s, expecting (a subclass of) %s",
					method.toString (),
					parameterTypes[0].toString (),
					contextClass.toString ()
				));
		}
		final Class<?> actualContextClass = parameterTypes[0];
		for (int i = 0; i < inputClasses.size (); ++ i) {
			if (!parameterTypes[i + 1].isAssignableFrom (inputClasses.get (i))) {
				throw new IllegalArgumentException (String.format (
						"Parameter %d of %s must be assignable from %s",
						i + 1,
						method.toString (),
						inputClasses.get (i).toString ()
					));
			}
		}
		
		// Bind the execute handle to the
		final MethodHandle executeThisHandle = executeHandle.bindTo (this);
		
		// Convert to a specific type:
		final Class<?> returnType = lastExpression.getResultType ();
		final Class<?>[] castParameterTypes = new Class<?>[inputClasses.size () + 1];
		
		castParameterTypes[0] = actualContextClass;
		for (int i = 0; i < inputClasses.size (); ++ i) {
			castParameterTypes[i + 1] = inputClasses.get (i);
		}
		
		final MethodHandle castExecuteHandle = executeThisHandle
				.asVarargsCollector (Object[].class)
				.asType (MethodType.methodType (returnType, castParameterTypes));
		
		// Create an interface wrapper for the resulting method handle:
		return MethodHandleProxies.asInterfaceInstance (iface, castExecuteHandle);
	}
	
	@Override
	public String toString () {
		return "Executor: " + plan.toString ();
	}
}
