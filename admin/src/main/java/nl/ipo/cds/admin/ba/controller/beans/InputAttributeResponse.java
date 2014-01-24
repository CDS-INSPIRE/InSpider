package nl.ipo.cds.admin.ba.controller.beans;

import nl.ipo.cds.domain.QName;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class InputAttributeResponse {

	private String type;

	@JsonIgnore	
	private final QName qName;
	
	private final String label;
	
	@JsonIgnore
	private final boolean isFilterOnly;
	
	public InputAttributeResponse (final String type, final QName qName, final String label, final boolean isFilterOnly) {
		this.type = type;
		this.qName = qName;
		this.label = label;
		this.isFilterOnly = isFilterOnly;
	}

	public QNameResponse getName () {
		return new QNameResponse(qName);
	}
	
	public String getType () {
		return type;
	}
	
	public String getLabel () {
		return label;
	}
	
	public boolean isFilterOnly () {
		return isFilterOnly;
	}
}
