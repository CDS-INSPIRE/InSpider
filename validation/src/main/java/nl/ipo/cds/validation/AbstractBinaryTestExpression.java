package nl.ipo.cds.validation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

/**
 * Binary tests must be deterministic.
 * 
 * @author erik
 *
 * @param <K>
 * @param <TypeA>
 * @param <TypeB>
 */
public abstract class AbstractBinaryTestExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, TypeA, TypeB> extends AbstractExpression<K, C, Boolean> implements BinaryExpression<K, C, Boolean, TypeA, TypeB> {

	public final String name;
	public final Expression<K, C, TypeA> a;
	public final Expression<K, C, TypeB> b;
	
	public AbstractBinaryTestExpression (final Expression<K, C, TypeA> a, final Expression<K, C, TypeB> b, final String name) {
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		if (a == null) {
			throw new NullPointerException ("a cannot be null");
		}
		if (b == null) {
			throw new NullPointerException ("b cannot be null");
		}
		
		this.name = name;
		this.a = a;
		this.b = b;
	}

	@Override
	public Class<Boolean> getResultType () {
		return Boolean.class;
	}

	public Boolean evaluate (final C context, final TypeA a, final TypeB b) {
		return test (a, b, context);
	}

	@Override
	public Class<TypeA> getTypeA () {
		return a.getResultType ();
	}

	@Override
	public Class<TypeB> getTypeB() {
		return b.getResultType ();
	}
	
	public abstract boolean test (TypeA a, TypeB b, C context);
	
	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					AbstractBinaryTestExpression.class, 
					"evaluate", 
					MethodType.methodType (Boolean.class, ValidatorContext.class, Object.class, Object.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
		return ExpressionExecutor.create (
				this, 
				a, 
				b,
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
		
		final AbstractBinaryTestExpression<?, ?, ?, ?> other = (AbstractBinaryTestExpression<?, ?, ?, ?>)o;
		
		return name.equals (other.name) && a.equals (other.a) && b.equals (other.b);
	}
	
	@Override
	public int hashCode () {
		return name.hashCode () ^ a.hashCode () ^ b.hashCode ();
	}
	
	@Override
	public String toString () {
		return String.format ("(%s %s %s)", a, name, b);
	}
}
