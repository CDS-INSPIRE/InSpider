package nl.ipo.cds.admin.ba.controller.beans.mapping;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public final class InputAttribute extends Operation {
	
	private String inputAttributeNamespace;
	private String inputAttributeType;

	public String getInputAttributeNamespace () {
		return inputAttributeNamespace;
	}

	public void setInputAttributeNamespace (final String inputAttributeNamespace) {
		this.inputAttributeNamespace = inputAttributeNamespace;
	}

	public String getInputAttributeType () {
		return inputAttributeType;
	}

	public void setInputAttributeType (final String inputAttributeType) {
		this.inputAttributeType = inputAttributeType;
	}
}
