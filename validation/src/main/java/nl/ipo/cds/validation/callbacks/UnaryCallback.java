package nl.ipo.cds.validation.callbacks;

import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;

public interface UnaryCallback<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, R, I> {
	R call (I input, C context) throws Exception;
}
