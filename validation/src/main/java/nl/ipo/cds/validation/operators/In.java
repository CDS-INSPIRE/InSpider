package nl.ipo.cds.validation.operators;

import java.util.Set;

import nl.ipo.cds.validation.AbstractBinaryTestExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;

public class In<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T> extends AbstractBinaryTestExpression<K, C, T, Set<T>>{

	public In (final Expression<K, C, T> a, final Expression<K, C, Set<T>> b) {
		super(a, b, "In");
	}

	@Override
	public boolean test (final T a, final Set<T> b, final C context) {
		if (b == null) {
			return false;
		}
		
		return b.contains (a);
	}
}
