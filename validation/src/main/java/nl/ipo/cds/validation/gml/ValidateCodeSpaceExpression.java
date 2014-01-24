package nl.ipo.cds.validation.gml;

import nl.ipo.cds.validation.AbstractUnaryTestExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.gml.codelists.CodeList;
import nl.ipo.cds.validation.gml.codelists.CodeListException;

public class ValidateCodeSpaceExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractUnaryTestExpression<K, C, String> {

	public ValidateCodeSpaceExpression (final Expression<K, C, String> input) {
		super(input, "ValidateCodeSpace");
	}

	@Override
	public boolean test (final String value, final C context) {
		final CodeList list;
		
		try {
			list = context.getCodeListFactory ().getCodeList (value);
		} catch (CodeListException e) {
			return false;
		}
		
		return list != null;
	}
}
