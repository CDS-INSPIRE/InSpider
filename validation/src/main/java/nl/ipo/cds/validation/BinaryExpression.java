package nl.ipo.cds.validation;

public interface BinaryExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType, TypeA, TypeB> extends Expression<K, C, ResultType> {
	Class<TypeA> getTypeA ();
	Class<TypeB> getTypeB ();
}
