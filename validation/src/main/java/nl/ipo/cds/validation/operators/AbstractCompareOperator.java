package nl.ipo.cds.validation.operators;

import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;

public abstract class AbstractCompareOperator<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T extends Comparable<T>> extends AbstractOperator<K, C, Boolean, T> {
	public AbstractCompareOperator (final Expression<K, C, T> a, final Expression<K, C, T> b) {
		super (a, b);
	}
	
	@Override
	public Class<Boolean> getResultType () {
		return Boolean.class;
	}
	
	@Override
	public boolean equals (final Object o) {
		if (o == null || !getClass ().equals (o.getClass ())) {
			return false;
		}
		
		final AbstractCompareOperator<?, ?, ?> other = (AbstractCompareOperator<?, ?, ?>)o;
		
		return a.equals (other.a) && b.equals (other.b);
	}
	
	@Override
	public int hashCode () {
		return getClass ().hashCode () ^ a.hashCode () ^ b.hashCode ();
	}
}
