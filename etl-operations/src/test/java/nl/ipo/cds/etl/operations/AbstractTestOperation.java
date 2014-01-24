package nl.ipo.cds.etl.operations;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.executer.Executer;
import nl.ipo.cds.attributemapping.executer.MappingValidationException;
import nl.ipo.cds.attributemapping.executer.OperationExecutionException;
import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationOperationDiscoverer;
import nl.ipo.cds.etl.operations.OperationFactory.ConditionalInput;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (classes = AbstractTestOperation.Config.class)
public abstract class AbstractTestOperation {

	@Inject
	protected OperationFactory factory;
	
	@Configuration
	@ComponentScan (basePackageClasses = nl.ipo.cds.etl.operations.Package.class)
	public static class Config {
		@Bean
		public AnnotationOperationDiscoverer annotationOperationDiscoverer () {
			return new AnnotationOperationDiscoverer ();
		}
		
		@Bean
		public OperationFactory operationFactory () {
			return new OperationFactory ();
		}
	}
	
	public Operation stringConstant (final String value) {
		return factory.stringConstant (value);
	}
	
	public Operation stringInput (final String attributeName) {
		return factory.stringInput (attributeName);
	}
	
	public Operation conditional (final Operation elseBranch, final ConditionalInput ... inputs) {
		return factory.conditional (elseBranch, inputs);
	}
	
	public ConditionalInput conditionalInput (final Operation input, final String attribute, final ConditionalTransform.Operation operation, final String ... values) {
		return factory.conditionalInput (input, attribute, operation, values);
	}
	
	public ConditionalTransform.Condition condition (final String attribute, final ConditionalTransform.Operation operation, final String ... values) {
		return factory.condition (attribute, operation, values);
	}
	
	public Operation split (final Operation input, final String boundary) {
		return factory.split (input, boundary);
	}
	
	public Operation split (final Operation input, final String boundary, final boolean trimWhitespace) {
		return factory.split (input, boundary, trimWhitespace);
	}
	
	public Operation split (final Operation input, final String boundary, final boolean trimWhitespace, final boolean ignoreEmpty) {
		return factory.split (input, boundary, trimWhitespace, ignoreEmpty);
	}
	
	public Operation stringOut (final Operation input) {
		return factory.stringOut (input);
	}
	
	public Operation stringArrayOut (final Operation input) {
		return factory.stringArrayOut (input);
	}
	
	public Operation convertToString (final Operation input) {
		return factory.convertToString (input);
	}
	
	public Operation makeInspireId (final Operation countryCode, final Operation bronhouderCode, final Operation uuid, final Operation datasetCode) {
		return factory.makeInspireId (countryCode, bronhouderCode, uuid, datasetCode);
	}
	
	public Operation makeStringArray (final Operation ... operations) {
		return factory.makeStringArray (operations);
	}
	
	
	public <T> T execute (final Operation op, final Class<T> resultClass) throws MappingValidationException, OperationExecutionException {
		return execute (op, resultClass, (Map<String, Object>)null);
	}
	
	public <T> T execute (final Operation op, final Class<T> resultClass, final Object ... params) throws MappingValidationException, OperationExecutionException {
		final Map<String, Object> values = new HashMap<String, Object> ();
		
		for (int i = 0; i < params.length; i += 2) {
			values.put ((String)params[i], params[i + 1]);
		}
		
		return execute (op, resultClass, values);
	}
	
	public <T> T execute (final Operation op, final Class<T> resultClass, final Map<String, Object> attributes) throws MappingValidationException, OperationExecutionException {
		try (final Executer executer = new Executer (op, new MapperContext ())) {
			final ValueContainer<T> container = new ValueContainer<T> ();
			
			executer.execute (new MappingSource () {
				@Override
				public boolean hasAttribute (final String name) {
					return attributes != null && attributes.containsKey (name);
				}
				
				@Override
				public Object getAttributeValue (final String name) {
					return attributes == null ? null : attributes.get (name);
				}
			}, new MappingDestination () {
				@Override
				public void setValue (final Object value) {
					container.setValue (resultClass.cast (value));
				}
			});
			
			return container.value;
		}
	}
	
	private static class ValueContainer<T> {
		public T value;

		public void setValue (final T value) {
			this.value = value;
		}
	}
}
