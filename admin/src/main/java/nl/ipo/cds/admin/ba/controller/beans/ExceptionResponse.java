package nl.ipo.cds.admin.ba.controller.beans;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class ExceptionResponse<T extends Exception> {

	@JsonIgnore
	private final T exception;
	
	public ExceptionResponse (final T exception) {
		this.exception = exception;
	}
	
	@JsonIgnore
	protected T getException () {
		return exception;
	}
	
	public boolean getError () {
		return true;
	}
	
	public String getMessage () {
		return exception.getLocalizedMessage ();
	}
	
	public String getCause () {
		if (exception.getCause () != null) {
			return exception.getCause ().getLocalizedMessage ();
		}
		
		return null;
	}
}
