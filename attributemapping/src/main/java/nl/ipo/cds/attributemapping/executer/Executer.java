package nl.ipo.cds.attributemapping.executer;

import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.ipo.cds.attributemapping.AttributeMapperUtils;
import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;

/**
 * Executer for attribute mapping operations. Creates an execution plan to efficiently invoke the same
 * tree of mapping operations multiple times.
 * 
 * An executer is specific to a given operation tree and mapper context. Furthermore it is not threadsafe,
 * nor is the execute method reentrant.
 */
public class Executer implements AutoCloseable {

	private final Operation rootOperation;
	private final Object[] registers;
	private final ExecutionStep[] executionPlan;
	private final MapperContext context;
	
	public Executer (final Operation rootOperation, final MapperContext context) throws MappingValidationException, OperationExecutionException {
		this.rootOperation = rootOperation;
		this.context = context;
		
		final List<Operation> operations = flattenOperationTree (rootOperation);
		
		validateOperations (operations);
		
		this.registers = new Object[operations.size ()];
		this.executionPlan = buildExecutionPlan (operations);
		
		// Invoke the before methods:
		for (final ExecutionStep step: executionPlan) {
			step.executer.before ();
		}
	}

	public Operation getRootOperation () {
		return rootOperation;
	}
	
	public MapperContext getMapperContext () {
		return context;
	}
	
	public void execute (final MappingSource source, final MappingDestination destination) throws OperationExecutionException {
		for (final ExecutionStep step: executionPlan) {
			registers[step.outputRegister] = step.executer.execute (source, destination, step.inputRegisters);
		}
	}
	
	@Override
	public void close () throws OperationExecutionException {
		// Invoke the "after" methods:
		for (final ExecutionStep step: executionPlan) {
			step.executer.after ();
		}
	}

	private void validateOperations (final List<Operation> operations) throws MappingValidationException {
		for (final Operation operation: operations) {
			validateOperation (operation);
		}
	}
	
	private void validateOperation (final Operation operation) throws MappingValidationException {
		final OperationType operationType = operation.getOperationType ();
		final List<OperationInputType> inputTypes = operationType.getInputs ();
		final List<OperationInput> inputs = operation.getInputs ();
		
		if (inputTypes.size () > 0 && inputTypes.get (inputTypes.size () - 1).isVariableInputCount ()) {
			// Variable arguments:
			if (inputs.size () < inputTypes.size () - 1) {
				throw new MappingValidationException (String.format ("Invalid number of inputs for operation %s, expected at least %d (variable arguments)", operationType.getName (), inputTypes.size () - 1));
			}
		} else {
			if (inputs.size () != inputTypes.size ()) {
				throw new MappingValidationException (String.format ("Invalid number of inputs for operation %s, expected at %d", operationType.getName (), inputTypes.size ()));
			}
		}
		
		for (int i = 0; i < inputs.size (); ++ i) {
			final OperationInput input = inputs.get (i);
			final OperationInputType inputType = inputTypes.get (Math.min (inputTypes.size () - 1, i));
			
			validateInput (operation, input, inputType);
		}
	}
	
	private void validateInput (final Operation operation, final OperationInput input, final OperationInputType inputType) throws MappingValidationException {
		final Type effectiveType = input.getOperation ().getOperationType ().getReturnType ();
		final Type expectedType = inputType.getInputType ();
		
		if (!AttributeMapperUtils.areTypesAssignable (effectiveType, expectedType)) {
			throw new MappingValidationException (String.format (
					"Input %s of operation %s has an incompatible type %s, expected %s (output of operation %s)",
					inputType.getName (),
					operation.getOperationType ().getName (),
					effectiveType,
					expectedType,
					input.getOperation ().getOperationType ().getName ()
				));
		}
	}
	
	/**
	 * Creates an execution plan for the given list of operations. The execution plan
	 * has an "execution step" for each operation. An execution step in turn has a reference
	 * to an executor specific to the operation type and a reference to the registers that
	 * serve as inputs.
	 * 
	 * @param operations
	 * @return An array of execution steps that execute the attribute mapping.
	 * @throws MappingValidationException 
	 */
	private ExecutionStep[] buildExecutionPlan (final List<Operation> operations) throws MappingValidationException {
		final Map<Operation, Integer> operationIndex = new HashMap<Operation, Integer> ();
		final ExecutionStep[] executionSteps = new ExecutionStep[operations.size ()];
		
		for (int i = 0; i < operations.size (); ++ i) {
			final Operation operation = operations.get (i);
			final List<OperationInput> inputs = operation.getInputs ();
			
			// Store the index of this operation, later operations may use it as an input:
			operationIndex.put (operation, i);

			// List the indices of the input registers:
			final int[] inputRegisters = new int[inputs.size ()];
			for (int j = 0; j < inputs.size (); ++ j) {
				inputRegisters[j] = operationIndex.get (inputs.get (j).getOperation ());
			}
			
			// Create an executer for this operation:
			final OperationExecuter executer = operation.getOperationType ().createExecuter (operation.getOperationProperties (), context);
			
			if (executer == null) {
				throw new MappingValidationException (String.format ("Operation type %s did not return an executer.", operation.getOperationType ().getName ()));
			}
			
			executionSteps[i] = new ExecutionStep (
					operation,
					executer, 
					i, 
					new RegisterList (inputRegisters)
				);
		}
		
		return executionSteps;
	}
	
	/**
	 * Flattens the operation tree into an execution order that processes inputs of each
	 * operation first.
	 * 
	 * @param rootOperation
	 * @return The list of operations, in depth-first order.
	 */
	private List<Operation> flattenOperationTree (final Operation rootOperation) {
		final List<Operation> operations = new ArrayList<Operation> ();
		final LinkedList<Operation> fringe = new LinkedList<Operation> ();
		
		fringe.addLast (rootOperation);
		while (!fringe.isEmpty ()) {
			final Operation operation = fringe.pollFirst ();
			
			operations.add (operation);
			
			for (final OperationInput input: operation.getInputs ()) {
				fringe.addLast (input.getOperation ());
			}
		}
		
		Collections.reverse (operations);
		
		return operations;
	}

	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		for (final ExecutionStep s: executionPlan) {
			if (builder.length () > 0) {
				builder.append ("\n");
			}
			builder.append (s.toString ());
		}
		
		return builder.toString ();
	}
	
	private static class ExecutionStep {
		public final Operation operation;
		public final OperationExecuter executer;
		public final int outputRegister;
		public final RegisterList inputRegisters;
		
		public ExecutionStep (final Operation operation, final OperationExecuter executer, final int outputRegister, final RegisterList inputRegisters) {
			this.operation = operation;
			this.executer = executer;
			this.outputRegister = outputRegister;
			this.inputRegisters = inputRegisters;
		}
		
		@Override
		public String toString () {
			final StringBuilder b = new StringBuilder ();
			for (final int i: inputRegisters.getOffsets ()) {
				if (b.length() > 0) {
					b.append (", ");
				}
				b.append (String.format ("%d", i));
			}
			return String.format ("reg.%d = invoke %s(%s) (props: %s)", outputRegister, operation.getOperationType ().getName (), b.toString (), operation.getOperationProperties ());
		}
	}
	
	private class RegisterList extends AbstractList<Object> {

		private final int[] indices;
		
		public RegisterList (final int[] indices) {
			this.indices = indices;
		}
		
		@Override
		public Object get (final int index) {
			return registers[indices[index]];
		}

		@Override
		public int size () {
			return indices.length;
		}
		
		public int[] getOffsets () {
			return indices;
		}
	}
}
