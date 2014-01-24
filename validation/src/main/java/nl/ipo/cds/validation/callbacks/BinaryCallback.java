package nl.ipo.cds.validation.callbacks;

import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;

public interface BinaryCallback<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, R, TypeA, TypeB> {
	R call (TypeA a, TypeB b, C context) throws Exception;
}
