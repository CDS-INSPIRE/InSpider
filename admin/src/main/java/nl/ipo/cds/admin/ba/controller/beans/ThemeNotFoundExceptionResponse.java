package nl.ipo.cds.admin.ba.controller.beans;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import nl.ipo.cds.admin.ba.controller.ThemeNotFoundException;

@JsonSerialize (include = Inclusion.ALWAYS)
public class ThemeNotFoundExceptionResponse extends ExceptionResponse<ThemeNotFoundException> {

	public ThemeNotFoundExceptionResponse (final ThemeNotFoundException ex) {
		super (ex);
	}
	
	public String getThemeName () {
		return getException ().getThemeName ();
	}
}
