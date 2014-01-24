package nl.ipo.cds.validation;

public interface NAryExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType, InputType> extends Expression<K, C, ResultType> {
	public Class<InputType> getInputType ();
}
