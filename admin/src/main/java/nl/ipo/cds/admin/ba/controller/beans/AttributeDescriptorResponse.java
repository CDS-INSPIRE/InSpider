package nl.ipo.cds.admin.ba.controller.beans;

import java.util.Locale;

import nl.ipo.cds.etl.theme.AttributeDescriptor;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class AttributeDescriptorResponse {

	@JsonIgnore
	private final AttributeDescriptor<?> attributeDescriptor;
	
	public AttributeDescriptorResponse (final AttributeDescriptor<?> attributeDescriptor) {
		this.attributeDescriptor = attributeDescriptor;
	}

	public String getName () {
		return attributeDescriptor.getName ();
	}
	
	public String getLabel () {
		return attributeDescriptor.getLabel (Locale.getDefault ());
	}
	
	public String getDescription () {
		return attributeDescriptor.getDescription (Locale.getDefault ());
	}
	
	public String getType () {
		return attributeDescriptor
				.getAttributeType ()
				.toString ();
	}
}
