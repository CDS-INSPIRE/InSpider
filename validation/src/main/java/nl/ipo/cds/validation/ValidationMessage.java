package nl.ipo.cds.validation;

import java.util.List;

public interface ValidationMessage<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> {
	boolean isBlocking ();
	List<Expression<K, C, ?>> getMessageParameters ();
}
