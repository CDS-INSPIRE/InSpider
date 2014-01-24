package nl.ipo.cds.admin.ba.controller.beans;

import nl.ipo.cds.etl.process.HarvesterException;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class HarvesterExceptionResponse extends ExceptionResponse<HarvesterException> {

	public String description;
	
	public HarvesterExceptionResponse (final String description, final HarvesterException harvesterException) {
		super (harvesterException);
		
		this.description = description;
	}
	
	public HarvesterExceptionResponse (final HarvesterException harvesterException) {
		super (harvesterException);
	}
	
	public String getUrl () {
		return getException ().getUrl ();
	}
	
	public String getDescription () {
		return description;
	}
}
