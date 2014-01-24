package nl.ipo.cds.validation;

public abstract class AbstractExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType> implements Expression<K, C, ResultType> {

	@SuppressWarnings("unchecked")
	@Override
	public Class<C> getContextType () {
		final Class<?> c = (Class<?>)ValidatorContext.class;
		return (Class<C>)c;
	}
}
