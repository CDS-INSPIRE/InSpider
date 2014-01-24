package nl.ipo.cds.validation;

import nl.ipo.cds.validation.execute.ExecutableExpression;


public interface Expression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType> extends ExecutableExpression<C, ResultType> {
}
