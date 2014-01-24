package nl.ipo.cds.validation.logical;

import java.util.List;

import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;

public class OrExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractLogicalNAryExpr<K, C> {

	public OrExpression (final List<Expression<K, C, Boolean>> inputs) {
		super (inputs);
	}
	
	@Override
	public boolean evaluate (final List<Boolean> inputValues) {
		for (final Boolean value: inputValues) {
			if (value != null && value) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		builder.append ('(');
		
		for (final Expression<K, C, Boolean> input: inputs) {
			if (builder.length () > 1) {
				builder.append (" or ");
			}
			builder.append (input.toString ());
		}
		
		builder.append (')');
		
		return builder.toString ();
	}
}
