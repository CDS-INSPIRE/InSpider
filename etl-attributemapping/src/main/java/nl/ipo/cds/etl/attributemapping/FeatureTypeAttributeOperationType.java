package nl.ipo.cds.etl.attributemapping;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.executer.OperationExecuter;
import nl.ipo.cds.attributemapping.executer.OperationExecutionException;
import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.domain.FeatureTypeAttribute;

public class FeatureTypeAttributeOperationType implements OperationType {

	private final FeatureTypeAttribute featureTypeAttribute;
	
	public FeatureTypeAttributeOperationType (final FeatureTypeAttribute featureTypeAttribute) {
		this.featureTypeAttribute = featureTypeAttribute;
	}
	
	@Override
	public String getName () {
		return featureTypeAttribute == null ? null : featureTypeAttribute.getName ().getLocalPart ();
	}

	@Override
	public String getDescription (final Locale locale) {
		return getName ();
	}

	@Override
	public String getLabel (final Locale locale) {
		return getName ();
	}
	
	@Override
	public String getFormatLabel (final Locale locale) {
		return getName ();
	}

	@Override
	public Type getReturnType () {
		return (featureTypeAttribute == null || featureTypeAttribute.getType () == null) ? null : featureTypeAttribute.getType ().getJavaType ();
	}

	@Override
	public Class<?> getPropertyBeanClass () {
		return null;
	}

	@Override
	public List<OperationInputType> getInputs () {
		return new ArrayList<OperationInputType> ();
	}

	@Override
	public OperationExecuter createExecuter (final Object operationProperties, final MapperContext context) {
		final String name = getName ();
		
		return new OperationExecuter() {
			
			@Override
			public Object execute (final MappingSource source, final MappingDestination destination, final List<Object> inputs) throws OperationExecutionException {
				return source.getAttributeValue (name);
			}

			@Override
			public void before () throws OperationExecutionException {
			}

			@Override
			public void after () throws OperationExecutionException {
			}
		};
	}

}
