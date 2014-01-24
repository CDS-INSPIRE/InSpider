package nl.ipo.cds.validation.format;

import java.util.Set;

import nl.ipo.cds.validation.AbstractUnaryTestExpression;
import nl.ipo.cds.validation.AttributeExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.Validator;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.constants.Constant;
import nl.ipo.cds.validation.flow.ForEachExpression;
import nl.ipo.cds.validation.logical.AndExpression;
import nl.ipo.cds.validation.logical.NotExpression;
import nl.ipo.cds.validation.operators.In;

public class HtmlFormatter<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends ValidatorFormatter<K, C> {

	public HtmlFormatter (final Validator<K, C> exp) {
		super (exp);
	}
	
	public String format (final Validator<K, C> validator) {
		if (validator.messageKey == null) {
			return formatExpression (validator.expression);
		}
		
		return formatExpression (validator.expression);
	}
	
	public String format (final AndExpression<K, C> exp) {
		final StringBuilder b = new StringBuilder ();
		
		b.append ("<p><strong>De volgende constraints moeten gelden:</strong></b>\n");
		b.append ("<ul>\n");
		
		for (final Expression<K, C, ?> e: exp.inputs) {
			b.append ("<li>\n");
			b.append (formatExpression (e));
			b.append ('\n');
			b.append ("</li>\n");
		}
		
		b.append ("</ul>\n");
		
		return b.toString ();
	}
	
	public String format (final In<K, C, ?> exp) {
		return String.format ("%s komt voor in %s", formatExpression (exp.a), formatExpression (exp.b));
	}
	
	public String format (final Constant<K, C, ?> exp) {
		if (Set.class.isAssignableFrom (exp.type)) {
			final StringBuilder b = new StringBuilder ();

			b.append ('(');
			
			for (final Object o: (Set<?>)exp.value) {
				if (b.length () > 1) {
					b.append (", ");
				}
				b.append ("<em>\"" + o.toString () + "\"</em>");
			}
			
			b.append (')');
			return b.toString ();
		} else {
			return String.format ("<em>\"%s\"</em>", exp.value.toString ());
		}
	}
	
	public String format (final ForEachExpression<K, C, ?> exp) {
		return String.format ("<p><strong>Voor iedere <em>%s</em> in %s moet gelden:</strong></p>\n<ul>\n<li>\n%s\n</li>\n</ul>\n", 
			exp.variableName, 
			formatExpression (exp.input),
			formatExpression (exp.validator.expression)
		);
	}
	
	public String format (final AttributeExpression<K, C, ?> exp) {
		return "<em>" + (exp.label != null ? exp.label : exp.name) + "</em>";
	}
	
	public String format (final NotExpression<K, C> exp) {
		return String.format ("<strong>Niet toegestaan</strong>: %s", formatExpression (exp.input));
	}
	
	public String format (final AbstractUnaryTestExpression<K, C, ?> exp) {
		if (exp.name.equals ("IsNull")) {
			return String.format ("%s heeft geen waarde (null)", formatExpression (exp.input));
		} else {
			return String.format ("%s %s", formatExpression (exp.input), exp.name);
		}
	}
}
