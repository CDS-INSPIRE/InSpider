package nl.ipo.cds.attributemapping.operations;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.executer.OperationExecuter;

public interface OperationType {
	String getName ();
	String getDescription (Locale locale);
	String getLabel (Locale locale);
	String getFormatLabel (Locale locale);
	
	/**
	 * The return type of this operation. Returns Void.TYPE for outputs.
	 * 
	 * @return The return type of this operation.
	 */
	Type getReturnType ();
	
	/**
	 * Class that contains properties for this operation type, or null if the
	 * operation has no properties.
	 * 
	 * @return The property class, or null.
	 */
	Class<?> getPropertyBeanClass ();
	
	List<OperationInputType> getInputs ();
	OperationExecuter createExecuter (Object operationProperties, MapperContext context);
}
