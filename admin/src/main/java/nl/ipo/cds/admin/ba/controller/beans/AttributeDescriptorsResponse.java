package nl.ipo.cds.admin.ba.controller.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nl.ipo.cds.etl.theme.AttributeDescriptor;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class AttributeDescriptorsResponse {

	@JsonIgnore
	private final Set<AttributeDescriptor<?>> attributeDescriptors;
	
	public AttributeDescriptorsResponse (final Set<AttributeDescriptor<?>> attributeDescriptors) {
		this.attributeDescriptors = attributeDescriptors;
	}

	public List<AttributeDescriptorResponse> getAttributes () {
		final List<AttributeDescriptorResponse> result = new ArrayList<AttributeDescriptorResponse> ();
		
		for (final AttributeDescriptor<?> d: attributeDescriptors) {
			result.add (new AttributeDescriptorResponse (d));
		}
		
		return Collections.unmodifiableList (result);
	}
}
