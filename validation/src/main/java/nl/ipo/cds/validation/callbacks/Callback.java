package nl.ipo.cds.validation.callbacks;

import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;

public interface Callback<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T> {

	T call (final C context) throws Exception;
}
