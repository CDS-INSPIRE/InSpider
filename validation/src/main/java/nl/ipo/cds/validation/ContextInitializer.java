package nl.ipo.cds.validation;

public interface ContextInitializer<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> {
	void initialize (final ValidatorContext<K, C> context) throws Exception;
}
