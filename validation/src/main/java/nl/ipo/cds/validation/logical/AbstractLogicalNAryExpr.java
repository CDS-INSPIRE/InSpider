package nl.ipo.cds.validation.logical;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.NAryExpression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExecutableExpression;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public abstract class AbstractLogicalNAryExpr<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractExpression<K, C, Boolean> implements NAryExpression<K, C, Boolean, Boolean> {

	public final List<Expression<K, C, Boolean>> inputs;
	
	public AbstractLogicalNAryExpr (final List<Expression<K, C, Boolean>> expressions) {
		if (expressions.size () < 1) {
			throw new IllegalArgumentException ("must have at least one input");
		}
		
		this.inputs = new ArrayList<Expression<K, C, Boolean>> (expressions);
	}
	
	@Override
	public Class<Boolean> getResultType () {
		return Boolean.class;
	}
	
	@Override
	public Class<Boolean> getInputType () {
		return Boolean.class;
	}
	
	public Boolean evaluate (final ValidatorContext<K, C> context, final Boolean ... inputs) {
		// Evaluate all inputs:
		final ArrayList<Boolean> results = new ArrayList<Boolean> (inputs.length);
		
		for (final Boolean input: inputs) {
			final Boolean result = input;
			results.add (result);
		}
		
		// Let the logical operator do the rest of the work:
		return evaluate (results);
	}
	
	public abstract boolean evaluate (final List<Boolean> inputValues);
	
	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					AbstractLogicalNAryExpr.class, 
					"evaluate", 
					MethodType.methodType (Boolean.class, ValidatorContext.class, Boolean[].class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
		final List<ExecutableExpression<C, ?>> inputs = new ArrayList<ExecutableExpression<C, ?>> ();
		for (final Expression<K, C, Boolean> input: this.inputs) {
			inputs.add (input);
		}
		
		return new ExpressionExecutor<C> (
				this, 
				inputs, 
				false, 
				true, 
				evaluateHandle.bindTo (this),
				true
			);
	}
}
