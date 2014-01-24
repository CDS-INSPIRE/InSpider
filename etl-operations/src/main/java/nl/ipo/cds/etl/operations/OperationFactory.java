package nl.ipo.cds.etl.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationOperationType;
import nl.ipo.cds.etl.operations.input.StringConstantInput;
import nl.ipo.cds.etl.operations.input.StringInput;
import nl.ipo.cds.etl.operations.output.StringArrayOutput;
import nl.ipo.cds.etl.operations.output.StringOutput;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform;
import nl.ipo.cds.etl.operations.transform.MakeInspireIdTransform;
import nl.ipo.cds.etl.operations.transform.MakeStringArrayTransform;
import nl.ipo.cds.etl.operations.transform.SplitStringTransform;
import nl.ipo.cds.etl.operations.transform.ToStringTransform;

public class OperationFactory {

	@Inject
	private OperationDiscoverer discoverer;
	
	private Collection<OperationType> operationTypes;
	
	// Operation types:
	private @Inject StringConstantInput stringConstantInput;
	private @Inject ConditionalTransform conditionalTransform;
	private @Inject SplitStringTransform splitStringTransform;
	private @Inject StringOutput stringOutput;
	private @Inject StringArrayOutput stringArrayOutput;
	private @Inject StringInput stringInput;
	private @Inject ToStringTransform convertToString;
	private @Inject MakeInspireIdTransform makeInspireIdTransform;
	private @Inject MakeStringArrayTransform makeStringArrayTransform;
	
	@PostConstruct
	public void discoverOperations () {
		this.operationTypes = discoverer.getOperationTypes ();
	}
	
	public Operation stringInput (final String attributeName) {
		final StringInput.Settings settings = new StringInput.Settings ();
		
		settings.setAttributeName (attributeName);
		
		return op (stringInput, settings);
	}
	
	public Operation stringConstant (final String value) {
		final StringConstantInput.Settings settings = new StringConstantInput.Settings ();
		
		settings.setValue (value);
		
		return op (stringConstantInput, settings);
	}
	
	public Operation conditional (final Operation elseBranch, final ConditionalInput ... inputs) {
		final ConditionalTransform.Settings settings = new ConditionalTransform.Settings ();
		final Operation[] inputOperations = new Operation[inputs.length + 1];
		final List<ConditionalTransform.Condition> conditions = new ArrayList<ConditionalTransform.Condition > (inputs.length);
		
		for (int i = 0; i < inputs.length; ++ i) {
			inputOperations[i] = inputs[i].input;
			conditions.add (inputs[i].condition);
		}
		
		settings.setConditions (conditions);
		inputOperations[inputs.length] = elseBranch;
		
		return op (conditionalTransform, settings, inputOperations);
	}
	
	public ConditionalInput conditionalInput (final Operation input, final String attribute, final ConditionalTransform.Operation operation, final String ... values) {
		return new ConditionalInput (input, condition (attribute, operation, values));
	}
	
	public ConditionalTransform.Condition condition (final String attribute, final ConditionalTransform.Operation operation, final String ... values) {
		final ConditionalTransform.Condition condition = new ConditionalTransform.Condition ();
		
		condition.setAttribute (attribute);
		condition.setOperation (operation);
		condition.setValues (values);
		
		return condition;
	}
	
	public Operation split (final Operation input, final String boundary) {
		return split (input, boundary, true, false);
	}
	
	public Operation split (final Operation input, final String boundary, final boolean trimWhitespace) {
		return split (input, boundary, trimWhitespace, false);
	}
	
	public Operation split (final Operation input, final String boundary, final boolean trimWhitespace, final boolean ignoreEmpty) {
		final SplitStringTransform.Settings settings = new SplitStringTransform.Settings ();
		
		settings.setBoundary (boundary);
		settings.setTrimWhitespace (trimWhitespace);
		settings.setIgnoreEmptyItems (ignoreEmpty);
		
		return op (splitStringTransform, settings, input);
	}
	
	public Operation stringOut (final Operation input) {
		return op (stringOutput, null, input);
	}
	
	public Operation stringArrayOut (final Operation input) {
		return op (stringArrayOutput, null, input);
	}
	
	public Operation convertToString (final Operation input) {
		return op (convertToString, null, input);
	}
	
	public Operation makeInspireId (final Operation countryCode, final Operation bronhouderCode, final Operation uuid, final Operation datasetCode) {
		return op (makeInspireIdTransform, null, countryCode, bronhouderCode, uuid, datasetCode);
	}
	
	public Operation makeStringArray (final Operation ... inputs) {
		return op (makeStringArrayTransform, null, inputs);
	}
	
	public Operation op (final Object bean, final Object operationProperties, final Operation ... inputs) {
		final OperationType operationType = findOperationTypeForBean (bean);
		
		if (operationType == null) {
			throw new IllegalArgumentException ("Invalid bean");
		}
		
		return new Operation() {
			@Override
			public OperationType getOperationType() {
				return operationType;
			}
			
			@Override
			public Object getOperationProperties() {
				return operationProperties;
			}
			
			@Override
			public List<OperationInput> getInputs() {
				final List<OperationInput> result = new ArrayList<OperationInput> ();
				
				for (final Operation operation: inputs) {
					result.add (new OperationInput() {
						@Override
						public Operation getOperation() {
							return operation;
						}
					});
				}
				
				return result;
			}
		};
	}

	private OperationType findOperationTypeForBean (final Object bean) {
		for (final OperationType ot: operationTypes) {
			if (!(ot instanceof AnnotationOperationType)) {
				continue;
			}
			
			if (((AnnotationOperationType)ot).getBean () == bean) {
				return ot;
			}
		}
		
		return null;
	}
	
	public static class ConditionalInput {
		final Operation input;
		final ConditionalTransform.Condition condition;
		
		public ConditionalInput (final Operation input, final ConditionalTransform.Condition condition) {
			this.input = input;
			this.condition = condition;
		}
	}
}
