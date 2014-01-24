package nl.ipo.cds.admin.ba.controller.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.PropertyBeanDescription;
import nl.ipo.cds.attributemapping.operations.PropertyBeanFieldDescription;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.beans.BeanUtils;

@JsonSerialize (include = Inclusion.ALWAYS)
@JsonPropertyOrder (value = {
		"name",
		"description",
		"returnType",
		"inputs"
})
public class OperationTypeResponse {

	@JsonIgnore
	private final OperationType operationType;
	
	@JsonIgnore
	private final PropertyBeanDescription propertyBeanDescription;
	
	public OperationTypeResponse (final OperationType operationType, final PropertyBeanDescription propertyBeanDescription) {
		this.operationType = operationType;
		this.propertyBeanDescription = propertyBeanDescription;
	}
	
	public String getName () {
		return operationType.getName ();
	}
	
	public String getDescription () {
		return operationType.getDescription (Locale.getDefault ());
	}
	
	public String getLabel () {
		return operationType.getLabel (Locale.getDefault ());
	}
	
	public String getFormatLabel () {
		return operationType.getFormatLabel (Locale.getDefault ());
	}
	
	public String getReturnType () {
		final Type type = operationType.getReturnType ();
		
		if (Void.TYPE.equals (type)) {
			return null;
		}
		
		return type.toString ();
	}
	
	public List<OperationInputTypeResponse> getInputs () {
		final List<OperationInputTypeResponse> inputs = new ArrayList<OperationInputTypeResponse> ();

		for (final OperationInputType inputType: operationType.getInputs ()) {
			inputs.add (new OperationInputTypeResponse (inputType));
		}
		
		return inputs;
	}
	
	public boolean getHasFields () {
		return operationType.getPropertyBeanClass () != null;
	}

	public List<FieldDescriptionResponse> getFieldDescriptions () {
		final List<FieldDescriptionResponse> result = new ArrayList<FieldDescriptionResponse> ();
		
		if (propertyBeanDescription == null) {
			return result;
		}
		
		// Create a dummy settings bean instance that exposes the default values:
		try {
			final Class<?> beanClass = propertyBeanDescription.getBeanClass ();
			final Object defaultBean = beanClass.newInstance ();
					
			for (final PropertyBeanFieldDescription fd: propertyBeanDescription.getFieldDescriptions ()) {
				final PropertyDescriptor desc = BeanUtils.getPropertyDescriptor (beanClass, fd.getName ());
				final Object defaultValue = desc.getReadMethod().invoke (defaultBean);
				
				result.add (new FieldDescriptionResponse (fd, defaultValue));
			}
		} catch (InvocationTargetException e) {
			return new ArrayList<FieldDescriptionResponse> ();
		} catch (InstantiationException e) {
			return new ArrayList<FieldDescriptionResponse> ();
		} catch (IllegalAccessException e) {
			return new ArrayList<FieldDescriptionResponse> ();
		}
		
		return result;
	}
	
	@JsonIgnore
	OperationType getOperationType () {
		return operationType;
	}
}
