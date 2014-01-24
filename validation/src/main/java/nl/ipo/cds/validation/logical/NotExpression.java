package nl.ipo.cds.validation.logical;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.UnaryExpression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public class NotExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractExpression<K, C, Boolean> implements UnaryExpression<K, C, Boolean, Boolean> {

	public final Expression<K, C, Boolean> input;
	
	public NotExpression (final Expression<K, C, Boolean> input) {
		if (input == null) {
			throw new NullPointerException ("input cannot be null");
		}
		
		this.input = input;
	}

	@Override
	public Class<Boolean> getResultType() {
		return Boolean.class;
	}

	@Override
	public Class<Boolean> getInputType() {
		return Boolean.class;
	}

	public Boolean evaluate (final ValidatorContext<K, C> context, final Boolean input) {
		final Boolean result = input;
		
		if (result == null) {
			return true;
		}
		
		return !result;
	}
	
	@Override
	public String toString () {
		return String.format ("not(%s)", input.toString ());
	}

	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					NotExpression.class, 
					"evaluate", 
					MethodType.methodType (Boolean.class, ValidatorContext.class, Boolean.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
		return ExpressionExecutor.create (
				this, 
				input, 
				false, 
				true, 
				evaluateHandle.bindTo (this),
				false
			);
	}
	
	@Override
	public boolean equals (final Object o) {
		if (o == null || !(getClass ().equals (o.getClass ()))) {
			return false;
		}
		
		final NotExpression<?, ?> other = (NotExpression<?, ?>)o;
		
		return input.equals (other.input);
	}
	
	@Override
	public int hashCode () {
		return getClass ().hashCode () ^ input.hashCode (); 
	}
}
