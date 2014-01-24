package nl.ipo.cds.validation;

public interface ValidationReporter<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> {
	void reportValidationError (Validator<K, C> validator, C context, K messageKey, Object[] parameters);
}
