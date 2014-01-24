package nl.ipo.cds.etl.theme;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.deegree.commons.tom.ows.CodeType;

import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.executer.OperationExecuter;
import nl.ipo.cds.attributemapping.executer.OperationExecutionException;
import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OutputOperationType;
import nl.ipo.cds.etl.theme.annotation.CodeSpace;

public class AttributeDescriptor<ObjectType> implements OutputOperationType {

	private final ObjectDescriptor<ObjectType> objectDescriptor;
	private final PropertyDescriptor propertyDescriptor;
	private final OperationInputType inputType;
	private final Type attributeType;
	private final String codeSpace;
	
	AttributeDescriptor (final ObjectDescriptor<ObjectType> objectDescriptor, final PropertyDescriptor propertyDescriptor) {
		this.objectDescriptor = objectDescriptor;
		this.propertyDescriptor = propertyDescriptor;
		
		attributeType = propertyDescriptor
			.getWriteMethod ()
			.getGenericParameterTypes ()[0];
		final String inputName = "input";
		
		inputType = new OperationInputType() {
			@Override
			public boolean isVariableInputCount() {
				return false;
			}
			
			@Override
			public String getName() {
				return inputName;
			}
			
			@Override
			public Type getInputType () {
				return Object.class;
			}
			
			@Override
			public String getDescription (final Locale locale) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		// Determine the code space:
		if (attributeType.equals (CodeType.class)) {
			final CodeSpace codeSpaceAnnotation = propertyDescriptor.getReadMethod ().getAnnotation (CodeSpace.class) != null
					? propertyDescriptor.getReadMethod ().getAnnotation (CodeSpace.class)
					: propertyDescriptor.getWriteMethod ().getAnnotation (CodeSpace.class);
					
			codeSpace = codeSpaceAnnotation != null ? codeSpaceAnnotation.value () : null;
		} else {
			codeSpace = null;
		}
	}
	
	public String getCodeSpace () {
		return codeSpace;
	}
	
	public Type getAttributeType () {
		return attributeType;
	}
	
	public ObjectDescriptor<ObjectType> getObjectDescriptor () {
		return objectDescriptor;
	}
	
	public PropertyDescriptor getPropertyDescriptor () {
		return propertyDescriptor;
	}

	public String getMessageKey () {
		return String.format ("%s.%s", getObjectDescriptor ().getObjectClass ().getCanonicalName (), getName ());
	}

	@Override
	public String getName () {
		return propertyDescriptor.getName ();
	}

	@Override
	public String getLabel (final Locale locale) {
		return getObjectDescriptor ()
				.getMessageSource ()
				.getMessage (String.format ("%s.label", getMessageKey ()), null, getName (), locale);
	}
	
	@Override
	public String getFormatLabel (final Locale locale) {
		return getLabel (locale);
	}
	
	@Override
	public String getDescription (final Locale locale) {
		return getObjectDescriptor ()
				.getMessageSource ()
				.getMessage (String.format ("%s.description", getMessageKey ()), null, getLabel (locale), locale);
	}

	@Override
	public Type getReturnType() {
		return Void.TYPE;
	}

	@Override
	public Class<?> getPropertyBeanClass() {
		return null;
	}

	@Override
	public List<OperationInputType> getInputs () {
		return Arrays.asList (new OperationInputType[] { inputType });
	}

	@Override
	public OperationExecuter createExecuter (final Object operationProperties, final MapperContext context) {
		return new OperationExecuter () {
			@Override
			public Object execute (final MappingSource source, final MappingDestination destination, final List<Object> inputs) throws OperationExecutionException {
				destination.setValue (inputs.get (0));
				return null;
			}

			@Override
			public void before() throws OperationExecutionException {
			}

			@Override
			public void after() throws OperationExecutionException {
			}
		};
	}
}
