package nl.ipo.cds.validation;

public interface UnaryExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType, InputType> extends Expression<K, C, ResultType> {
	Class<InputType> getInputType ();
}
