package nl.ipo.cds.validation.logical;

import java.util.List;

import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.constants.Constant;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;
import nl.ipo.cds.validation.flow.IfExpression;

public class AndExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractLogicalNAryExpr<K, C> {

	public final boolean shortCircuit;
	
	public AndExpression (final List<Expression<K, C, Boolean>> inputs) {
		this (inputs, false);
	}
	
	public AndExpression (final List<Expression<K, C, Boolean>> inputs, final boolean shortCircuit) {
		super (inputs);
		this.shortCircuit = shortCircuit;
	}
	
	public AndExpression<K, C> shortCircuit () {
		return new AndExpression<K, C> (inputs, true);
	}
	
	@Override
	public boolean evaluate (final List<Boolean> inputValues) {
		for (final Boolean value: inputValues) {
			if (value == null || !value) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		builder.append ('(');
		
		for (final Expression<K, C, Boolean> input: inputs) {
			if (builder.length () > 1) {
				builder.append (" and ");
			}
			builder.append (input.toString ());
		}
		
		builder.append (')');
		
		return builder.toString ();
	}
	
	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
		if (!shortCircuit) {
			return super.getExecutor (compiler);
		}
		
		// Transform the and expression into a sequence of if-else epxressions that implement the early-out:
		return createIfExpression (inputs).getExecutor (compiler);
	}

	private Expression<K, C, Boolean> createIfExpression (final List<Expression<K, C, Boolean>> inputs) {
		if (inputs.size () == 1) {
			return inputs.get (0);
		} else {
			return new IfExpression<K, C, Boolean> (inputs.get (0), createIfExpression (inputs.subList (1, inputs.size ())), new Constant<K, C, Boolean> (false, Boolean.class));
		}
	}
}
