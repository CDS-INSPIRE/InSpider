package nl.ipo.cds.validation.execute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecutionPlan<C> {

	private final List<ExecutorTuple<C>> executors = new ArrayList<ExecutorTuple<C>> ();
	private final List<ExecutionStep<C>> steps = new ArrayList<ExecutionStep<C>> ();
	
	public ExecutionPlan () {
	}
	
	public List<ExecutionStep<C>> getExecutionSteps () {
		return Collections.unmodifiableList (steps);
	}

	public ExecutionStep<C> getExecutionStep (final ExpressionExecutor<C> executor) {
		for (final ExecutionStep<C> step: steps) {
			if (step.executor == executor) {
				return step;
			}
		}
		
		return null;
	}
	
	private int getExecutionStepIndex (final ExecutableExpression<C, ?> expression) {
		final ExpressionExecutor<C> executor = getExecutor (expression);
		if (executor == null) {
			return -1;
		}
		
		return getExecutionStepIndex (executor);
		
		/*
		for (int i = 0; i < steps.size (); ++ i) {
			if (steps.get (i).executor.expression == expression || executorsAreFoldable (steps.get (i).executor, expression)) {
				return i;
			}
		}
		
		return -1;
		*/
	}

	private int getExecutionStepIndex (final ExpressionExecutor<C> executor) {
		for (int i = 0; i < steps.size (); ++ i) {
			if (steps.get (i).executor == executor) {
				return i;
			}
		}
		
		return -1;
	}
	
	public ExecutionStep<C> addExecutionStep (final ExpressionExecutor<C> executor) {
		final ExecutionStep<C> old = getExecutionStep (executor);
		
		if (old != null) {
			return old;
		}
		
		// Locate inputs:
		final int[] inputs = new int[executor.inputs.size ()];
		for (int i = 0; i < inputs.length; ++ i) {
			inputs[i] = getExecutionStepIndex (executor.inputs.get (i));
			if (inputs[i] < 0) {
				throw new IllegalStateException ("Unable to locate execution step for expression: " + executor.inputs.get (i));
			}
		}
		
		final ExecutionStep<C> newStep = new ExecutionStep<C> (executor, inputs); 
		
		steps.add (newStep);
		
		return newStep;
	}
	
	public ExpressionExecutor<C> getExecutor (final ExecutableExpression<C, ?> expression) {
		for (final ExecutorTuple<C> tuple: executors) {
			if (tuple.expression == expression || executorsAreFoldable (tuple.executor, expression)) {
				return tuple.executor;
			}
		}
		
		return null;
	}
	
	public void addExecutor (final ExecutableExpression<C, ?> expression, final ExpressionExecutor<C> executor) {
		if (getExecutor (executor.expression) != null) {
			return;
		}
		
		executors.add (new ExecutorTuple<C> (expression, executor));
	}
	
	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		builder.append ("Execution plan:\n");
		
		int n = 0;
		
		for (final ExecutionStep<C> step: steps) {
			builder.append (String.format (" - %d: ", n));
			builder.append (step.toString ());
			builder.append ('\n');
			++ n;
		}
		
		return builder.toString ();
	}
	
	private final static class ExecutorTuple<C> {
		public final ExecutableExpression<C, ?> expression;
		public final ExpressionExecutor<C> executor;
		
		public ExecutorTuple (final ExecutableExpression<C, ?> expression, final ExpressionExecutor<C> executor) {
			this.expression = expression;
			this.executor = executor;
		}
	}
	
	/**
	 * Returns true if the given executors can be folded: executor B can be replaced by the result of executor A. An expression can be folded when:
	 * - Executor A must be part of the execution plan.
	 * - Both expressions are equal.
	 * - The executors of child expressions must be in the execution plan.
	 * - The executors of child expressions of A and B are foldable.
	 *  
	 * @return
	 */
	private boolean executorsAreFoldable (final ExpressionExecutor<C> a, final ExecutableExpression<C, ?> b) {
		if (a == null || b == null) {
			return false;
		}
		
		// A must be in the execution plan:
		if (getExecutionStepIndex (a) < 0) {
			return false;
		}
		
		// Expressions A and B must be equal:
		if (!a.expression.equals (b)) {
			return false;
		}
		
		// A and B must be constant and/or deterministic. Assume B is constant/deterministic if A is constant/determinstic
		// since both expressions are equal:
		if (!a.isDeterministic) {
			return false;
		}
		
		// Each child of A must be constant and/or deterministic:
		for (final ExecutableExpression<C, ?> input: a.inputs) {
			if (!isConstantOrDeterministic (input)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isConstantOrDeterministic (final ExecutableExpression<C, ?> exp) {
		// Locate the execution step:
		final int index = getExecutionStepIndex (exp);
		if (index < 0) {
			return false;
		}
		final ExpressionExecutor<C> executor = steps.get (index).executor;
		
		if (!executor.isConstant && !executor.isDeterministic) {
			return false;
		}
		
		for (final ExecutableExpression<C, ?> input: executor.inputs) {
			if (!isConstantOrDeterministic (input)) {
				return false;
			}
		}
		
		return true;
	}
}
