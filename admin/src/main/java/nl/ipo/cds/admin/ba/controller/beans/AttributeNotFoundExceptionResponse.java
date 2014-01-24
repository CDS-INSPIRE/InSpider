package nl.ipo.cds.admin.ba.controller.beans;

import nl.ipo.cds.admin.ba.controller.AttributeNotFoundException;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class AttributeNotFoundExceptionResponse extends ExceptionResponse<AttributeNotFoundException> {

	public AttributeNotFoundExceptionResponse (final AttributeNotFoundException exception) {
		super (exception);
	}

	public String getThemeName () {
		return getException ().getThemeName ();
	}
	
	public String getAttributeName () {
		return getException ().getAttributeName ();
	}
}
