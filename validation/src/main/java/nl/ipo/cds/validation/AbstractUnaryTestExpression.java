package nl.ipo.cds.validation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

/**
 * Unary tests must be deterministic (for the same input they should yield the same result).
 * 
 * @author erik
 *
 * @param <K>
 * @param <T>
 */
public abstract class AbstractUnaryTestExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T> extends AbstractExpression<K, C, Boolean> implements UnaryExpression<K, C, Boolean, T> {
	public final String name;
	public final Expression<K, C, T> input;

	public AbstractUnaryTestExpression (final Expression<K, C, T> input, final String name) {
		if (input == null) {
			throw new NullPointerException ("input cannot be null");
		}
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		
		this.name = name;
		this.input = input;
	}

	@Override
	public Class<Boolean> getResultType () {
		return Boolean.class;
	}

	public Boolean evaluate (final C context, final T input) {
		return test (input, context);
	}
	
	@Override
	public Class<T> getInputType () {
		return input.getResultType ();
	}
	
	@Override
	public String toString () {
		return String.format ("%s(%s)", name, input.toString ());
	}
	
	public abstract boolean test (T value, C context);
	
	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					AbstractUnaryTestExpression.class, 
					"evaluate", 
					MethodType.methodType (Boolean.class, ValidatorContext.class, Object.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
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
		if (o == null || !o.getClass ().equals (getClass ())) {
			return false;
		}

		final AbstractUnaryTestExpression<?, ?, ?> other = (AbstractUnaryTestExpression<?, ?, ?>)o;

		return name.equals (other.name) && input.equals (other.input);
	}
	
	@Override
	public int hashCode () {
		return name.hashCode () ^ input.hashCode ();
	}
}
