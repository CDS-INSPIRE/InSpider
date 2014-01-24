package nl.ipo.cds.validation.constants;

import java.lang.invoke.MethodHandles;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public class Constant<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T> extends AbstractExpression<K, C, T> {
	public final T value;
	public final Class<T> type;
	
	public Constant (final T value, final Class<T> type) {
		this.value = value;
		this.type = type;
	}

	@Override
	public Class<T> getResultType () {
		return type;
	}

	public T evaluate (final ValidatorContext<K, C> context) {
		return value;
	}

	@Override
	public String toString () {
		return value.toString ();
	}

	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
		return ExpressionExecutor.create (
				this, 
				true, 
				true, 
				MethodHandles.constant (type, value), 
				false
			);
	}
	
	/**
	 * Override equals to merge equal constants in the executor.
	 */
	@Override
	public boolean equals (final Object o) {
		if (o == null) {
			return false;
		}
		
		if (!(o instanceof Constant)) {
			return false;
		}
		
		return value.equals (((Constant<?, ?, ?>)o).value); 
	}
	
	@Override
	public int hashCode () {
		return value.hashCode ();
	}
}
