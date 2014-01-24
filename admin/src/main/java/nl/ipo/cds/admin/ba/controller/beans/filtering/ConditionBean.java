package nl.ipo.cds.admin.ba.controller.beans.filtering;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class ConditionBean {

	private String field;
	private String operation;
	private String value;
	private boolean caseSensitive;
	
	public String getField () {
		return field;
	}
	
	public void setField (final String field) {
		this.field = field;
	}
	
	public String getOperation () {
		return operation;
	}
	
	public void setOperation (final String operation) {
		this.operation = operation;
	}
	
	public String getValue () {
		return value;
	}
	
	public void setValue (final String value) {
		this.value = value;
	}
	
	public boolean isCaseSensitive () {
		return caseSensitive;
	}
	
	public void setCaseSensitive (final boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
}
