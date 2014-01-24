package nl.ipo.cds.admin.ba.controller.beans;

import java.lang.reflect.Type;
import java.util.Locale;

import nl.ipo.cds.attributemapping.operations.PropertyBeanFieldDescription;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class FieldDescriptionResponse {

	@JsonIgnore
	private final PropertyBeanFieldDescription fieldDescription;
	
	@JsonIgnore
	private final Object defaultValueObj;
	
	public FieldDescriptionResponse (final PropertyBeanFieldDescription fieldDescription, final Object defaultValue) {
		this.fieldDescription = fieldDescription;
		this.defaultValueObj = defaultValue;
	}
	
	public String getName () {
		return fieldDescription.getName ();
	}
	
	public String getDescription () {
		return fieldDescription.getDescription (Locale.getDefault ());
	}
	
	public String getType () {
		final Type type = fieldDescription.getType ();

		return type.toString ();
	}
	
	public Object getDefaultValue () {
		return defaultValueObj;
	}
}
