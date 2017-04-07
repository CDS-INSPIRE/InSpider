package nl.ipo.cds.admin.ba.controller.beans.mapping;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonTypeInfo (
	use = Id.NAME,
	include = As.PROPERTY,
	property = "type"
)
@JsonSubTypes ({
	@Type (value = TransformOperation.class, name = "operation"),
	@Type (value = InputAttribute.class, name = "input"),
	@Type (value = ConditionOperation.class, name = "condition")
})
@JsonSerialize (include = Inclusion.ALWAYS)
public abstract class Operation {

	private String name;

	public String getName () {
		return name;
	}

	public void setName (final String name) {
		this.name = name;
	}
}
