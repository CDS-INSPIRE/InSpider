package nl.ipo.cds.attributemapping.executer;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.NullReference;
import nl.ipo.cds.attributemapping.operations.InputOperationType;
import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.OutputOperationType;
import nl.ipo.cds.attributemapping.operations.TransformOperationType;

import org.junit.Test;

public class TestExecuter {

	private final static NullInputOperationType nullInputOperationType = new NullInputOperationType ();
	private final static StringInputOperationType stringInputOperationType = new StringInputOperationType ();
	private final static IntegerInputOperationType integerInputOperationType = new IntegerInputOperationType ();
	private final static StringOutputOperationType stringOutputOperationType = new StringOutputOperationType ();
	private final static ConcatenateOperationType concatenateOperationType = new ConcatenateOperationType ();
	private final static VariableConcatenateOperationType variableConcatenateOperationType = new VariableConcatenateOperationType ();
	private final static ConcatenateIntegerStringsOperationType concatenateIntegerStringsOperationType = new ConcatenateIntegerStringsOperationType ();
	
	private final static Operation concatenateAB = 
		output (
			concatenate (
				input ("a"),
				input ("b")
			)
		);
	private final static Operation concatenateABCD =
		output (
			concatenate (
				concatenate (
					input ("a"),
					input ("b")
				),
				concatenate (
					input ("c"),
					input ("d")
				)
			)
		);
	private final static Operation concatenateABC = 
		output (
			concatenate (
				input ("a"),
				input ("b"),
				input ("c")
			)
		);
	private final static Operation concatenateABCDEFGHI =
		output (
			concatenate (
				concatenate (
					input ("a"),
					input ("b"),
					input ("c")
				),
				concatenate (
					input ("d"),
					input ("e"),
					input ("f")
				),
				concatenate (
					input ("g"),
					input ("h"),
					input ("i")
				)
			)
		);
	
	@Test
	public void testCreateExecuter () throws MappingValidationException, OperationExecutionException {
		try (final Executer executor = new Executer (concatenateAB, new MapperContext ())) {
		}
	}
	
	@Test
	public void testExecute () throws Exception {
		assertEquals ("ab", execute (concatenateAB));
		assertEquals ("abcd", execute (concatenateABCD));
	}

	@Test
	public void testExecuteVarargs () throws Exception {
		assertEquals ("abc", execute (concatenateABC));
		assertEquals ("abcdefghi", execute (concatenateABCDEFGHI));
	}
	
	@Test (expected = MappingValidationException.class)
	public void testInvalidType () throws Exception {
		execute (
			concatenate (
				input ("a"),
				input (1)
			)
		);
	}
	
	@Test (expected = MappingValidationException.class)
	public void testInvalidType2 () throws Exception {
		execute (
			concatenate (
				input (1),
				input ("a")
			)
		);
	}
	
	@Test (expected = MappingValidationException.class)
	public void testInvalidType3 () throws Exception {
		execute (
			concatenate (
				input (1),
				input (1)
			)
		);
	}
	
	@Test (expected = MappingValidationException.class)
	public void testInvalidTypeVarargs () throws Exception {
		execute (
			concatenate (
				input ("a"),
				input ("b"),
				input ("c"),
				input (1),
				input ("d")
			)
		);
	}
	
	@Test (expected = MappingValidationException.class)
	public void testInvalidArgumentCount () throws Exception {
		execute (
			op (
				concatenateOperationType,
				input ("a"),
				input ("b"),
				input ("c")
			)
		);
	}
	
	@Test (expected = MappingValidationException.class)
	public void testInvalidArgumentCount2 () throws Exception {
		execute (
			op (
				concatenateOperationType,
				input ("a")
			)
		);
	}

	@Test
	public void testConcatenateIntegerStrings () throws Exception {
		final String value = execute (
			output (
				op (
					concatenateIntegerStringsOperationType,
					input (42),
					input ("a"),
					input ("b"),
					input ("c"),
					input ("d")
				)
			)
		);
		
		assertEquals ("42abcd", value);
	}
	
	@Test (expected = MappingValidationException.class)
	public void testConcatenateIntegerStringsInvalidType () throws Exception {
		execute (
			output (
				op (
					concatenateIntegerStringsOperationType,
					input (42),
					input ("a"),
					input ("b"),
					input ("c"),
					input (1)
				)
			)
		);
	}
	
	@Test (expected = MappingValidationException.class)
	public void testConcatenateIntegerStringsInvalidCount () throws Exception {
		execute (
			output (
				op (
					concatenateIntegerStringsOperationType
				)
			)
		);
	}
	
	@Test
	public void testMapNullValue () throws Exception {
		execute (
			output (
				nullValue ()
			)
		);
	}
	
	@Test
	public void testBeforeAfter () throws Exception {
		final AtomicBoolean beforeInvoked = new AtomicBoolean (false);
		final AtomicBoolean afterInvoked = new AtomicBoolean (false);
		
		final InputOperationType ot = new InputOperationType () {
			@Override
			public Type getReturnType () {
				return String.class;
			}
			
			@Override
			public Class<?> getPropertyBeanClass () {
				return null;
			}
			
			@Override
			public String getName () {
				return "TestOperation";
			}
			
			@Override
			public String getLabel (final Locale locale) {
				return getName ();
			}
			
			@Override
			public List<OperationInputType> getInputs() {
				return Collections.emptyList ();
			}
			
			@Override
			public String getFormatLabel (final Locale locale) {
				return getName ();
			}
			
			@Override
			public String getDescription (final Locale locale) {
				return getName ();
			}
			
			@Override
			public OperationExecuter createExecuter (final Object operationProperties, final MapperContext context) {
				return new OperationExecuter () {
					
					@Override
					public Object execute(MappingSource source, MappingDestination destination, List<Object> inputs) throws OperationExecutionException {
						return "Hello, World!";
					}
					
					@Override
					public void before() throws OperationExecutionException {
						beforeInvoked.set (true);
					}
					
					@Override
					public void after() throws OperationExecutionException {
						afterInvoked.set (true);
					}
				};
			}
		};
		
		execute (
			output (
				op (ot)
			)
		);
		
		assertTrue (beforeInvoked.get ());
		assertTrue (afterInvoked.get ());
	}
	
	// =========================================================================
	// Convenience methods for creating and executing operations:
	// =========================================================================
	private static String execute (final Operation rootOperation) throws OperationExecutionException, MappingValidationException {
		try (final Executer executer = new Executer (rootOperation, new MapperContext ())) {
			final StringValue result = new StringValue (null);
			
			System.out.println ("Executing:\n" + executer);
			
			executer.execute (new MappingSource() {
				@Override
				public boolean hasAttribute(String name) {
					return false;
				}
				
				@Override
				public Object getAttributeValue(String name) {
					return null;
				}
			}, new MappingDestination () {
				@Override
				public void setValue(Object value) {
					result.setValue ((String)value);
				}
			});
			
			return result.getValue ();
		}
	}
	
	private static Operation input (final String value) {
		return new Operation() {
			@Override
			public OperationType getOperationType() {
				return stringInputOperationType;
			}
			
			@Override
			public Object getOperationProperties() {
				return new StringValue (value);
			}
			
			@Override
			public List<OperationInput> getInputs() {
				return new ArrayList<OperationInput> ();
			}
		};
	}

	private static Operation input (final int value) {
		return new Operation() {
			@Override
			public OperationType getOperationType() {
				return integerInputOperationType;
			}
			
			@Override
			public Object getOperationProperties() {
				return new IntegerValue (value);
			}
			
			@Override
			public List<OperationInput> getInputs() {
				return new ArrayList<OperationInput> ();
			}
		};
	}
	
	private static Operation output (final Operation a) {
		return op (stringOutputOperationType, a);
	}
	
	private static Operation nullValue () {
		return op (nullInputOperationType);
	}
	
	private static Operation concatenate (final Operation a, final Operation b) {
		return op (concatenateOperationType, a, b);
	}
	
	private static Operation concatenate (final Operation ... operations) {
		return op (variableConcatenateOperationType, operations);
	}
	
	private static Operation op (final OperationType operationType, final Operation ... operations) {
		return new Operation () {
			@Override
			public OperationType getOperationType () {
				return operationType;
			}
			
			@Override
			public Object getOperationProperties () {
				return null;
			}
			
			@Override
			public List<OperationInput> getInputs() {
				final List<OperationInput> inputs = new ArrayList<OperationInput> ();
				
				for (final Operation op: operations) {
					inputs.add (new OperationInput() {
						@Override
						public Operation getOperation() {
							return op;
						}
					});
				}
				
				return inputs;
			}
		};
	}
	
	// =========================================================================
	// Operation type classes:
	// =========================================================================
	private static class StringValue {
		public String value;
		
		public StringValue (final String value) {
			this.value = value;
		}
		
		public String getValue () {
			return value;
		}
		
		public void setValue (final String value) {
			this.value = value;
		}
		
		@Override
		public String toString () {
			return value;
		}
	}
	
	private static class IntegerValue {
		public int value;
		
		public IntegerValue (final int value) {
			this.value = value;
		}
		
		public int getValue () {
			return value;
		}
		
		@Override
		public String toString () {
			return String.valueOf (value);
		}
	}
	
	private abstract static class AbstractOperationType implements OperationType {
		@Override
		public String getName() {
			return getClass ().getName ();
		}

		@Override
		public String getDescription(Locale locale) {
			return getName ();
		}
		
		@Override
		public String getLabel(Locale locale) {
			return getName ();
		}
		
		@Override
		public String getFormatLabel (Locale locale) {
			return getName ();
		}

		@Override
		public abstract Class<?> getReturnType();

		@Override
		public abstract Class<?> getPropertyBeanClass();

		@Override
		public abstract List<OperationInputType> getInputs();

		@Override
		public abstract OperationExecuter createExecuter(Object operationProperties,
				MapperContext context);
	}
	
	private static class NullInputOperationType extends AbstractOperationType implements InputOperationType {
		@Override
		public Class<?> getReturnType () {
			return NullReference.class;
		}
		
		@Override
		public List<OperationInputType> getInputs () {
			return new ArrayList<OperationInputType> ();
		}
		
		@Override
		public OperationExecuter createExecuter (final Object operationProperties, final MapperContext context) {
			return new OperationExecuter() {
				@Override
				public Object execute(MappingSource source, MappingDestination destination, List<Object> inputs) throws OperationExecutionException {
					return NullReference.VALUE;
				}

				@Override
				public void before() throws OperationExecutionException {
				}

				@Override
				public void after() throws OperationExecutionException {
				}
			};
		}

		@Override
		public Class<?> getPropertyBeanClass() {
			return null;
		}
	}
	
	private static class StringInputOperationType extends AbstractOperationType implements InputOperationType {
		@Override
		public Class<?> getReturnType() {
			return String.class;
		}

		@Override
		public Class<?> getPropertyBeanClass () {
			return StringValue.class;
		}

		@Override
		public List<OperationInputType> getInputs () {
			return new ArrayList<OperationInputType> ();
		}

		@Override
		public OperationExecuter createExecuter (final Object operationProperties, final MapperContext context) {
			return new OperationExecuter() {
				@Override
				public Object execute(MappingSource source, MappingDestination destination, List<Object> inputs) throws OperationExecutionException {
					return ((StringValue)operationProperties).getValue ();
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
	
	private static class IntegerInputOperationType extends AbstractOperationType implements InputOperationType {
		@Override
		public Class<?> getReturnType () {
			return Integer.TYPE;
		}

		@Override
		public Class<?> getPropertyBeanClass() {
			return IntegerValue.class;
		}

		@Override
		public List<OperationInputType> getInputs() {
			return new ArrayList<OperationInputType> ();
		}

		@Override
		public OperationExecuter createExecuter(final Object operationProperties,
				final MapperContext context) {
			return new OperationExecuter() {
				
				@Override
				public Object execute(MappingSource source, MappingDestination destination,
						List<Object> inputs) throws OperationExecutionException {
					return ((IntegerValue)operationProperties).getValue ();
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
	
	private static class StringOutputOperationType extends AbstractOperationType implements OutputOperationType {

		@Override
		public Class<?> getReturnType () {
			// TODO Auto-generated method stub
			return Void.TYPE;
		}

		@Override
		public Class<?> getPropertyBeanClass () {
			return null;
		}

		@Override
		public List<OperationInputType> getInputs () {
			final List<OperationInputType> inputs = new ArrayList<OperationInputType> ();
			inputs.add (new OperationInputType() {
				@Override
				public boolean isVariableInputCount() {
					return false;
				}
				
				@Override
				public String getName() {
					return "a";
				}
				
				@Override
				public Class<?> getInputType() {
					return String.class;
				}
				
				@Override
				public String getDescription(Locale locale) {
					return "a";
				}
			});
			return inputs;
		}

		@Override
		public OperationExecuter createExecuter(Object operationProperties,
				MapperContext context) {
			return new OperationExecuter() {
				@Override
				public Object execute(MappingSource source, MappingDestination destination,
						List<Object> inputs) throws OperationExecutionException {
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
	
	private static class ConcatenateOperationType extends AbstractOperationType implements TransformOperationType {

		@Override
		public Class<?> getReturnType() {
			return String.class;
		}

		@Override
		public Class<?> getPropertyBeanClass() {
			return null;
		}

		@Override
		public List<OperationInputType> getInputs() {
			final List<OperationInputType> inputs = new ArrayList<OperationInputType> ();
			
			inputs.add (new OperationInputType() {
				@Override
				public boolean isVariableInputCount() {
					return false;
				}
				
				@Override
				public String getName() {
					return "a";
				}
				
				@Override
				public Class<?> getInputType() {
					return String.class;
				}
				
				@Override
				public String getDescription(Locale locale) {
					return "a";
				}
			});
			inputs.add (new OperationInputType () {
				@Override
				public String getName() {
					return "b";
				}

				@Override
				public Class<?> getInputType() {
					return String.class;
				}

				@Override
				public String getDescription(Locale locale) {
					return "a";
				}

				@Override
				public boolean isVariableInputCount() {
					return false;
				}
			});
			
			return inputs;
		}

		@Override
		public OperationExecuter createExecuter(Object operationProperties,
				MapperContext context) {
			return new OperationExecuter() {
				
				@Override
				public Object execute(MappingSource source, MappingDestination destination,
						List<Object> inputs) throws OperationExecutionException {
					
					return ((String)inputs.get (0)) + ((String)inputs.get (1));
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
	
	private static class VariableConcatenateOperationType extends AbstractOperationType implements TransformOperationType {
		@Override
		public Class<?> getReturnType() {
			return String.class;
		}

		@Override
		public Class<?> getPropertyBeanClass() {
			return null;
		}

		@Override
		public List<OperationInputType> getInputs() {
			final List<OperationInputType> inputs = new ArrayList<OperationInputType> ();
			inputs.add (new OperationInputType() {
				@Override
				public boolean isVariableInputCount() {
					return true;
				}
				
				@Override
				public String getName() {
					return "values";
				}
				
				@Override
				public Class<?> getInputType() {
					return String.class;
				}
				
				@Override
				public String getDescription(Locale locale) {
					return "values";
				}
			});
			return inputs;
		}

		@Override
		public OperationExecuter createExecuter(Object operationProperties,
				MapperContext context) {
			return new OperationExecuter() {
				@Override
				public Object execute(MappingSource source, MappingDestination destination,
						List<Object> inputs) throws OperationExecutionException {
					final StringBuilder builder = new StringBuilder ();
					
					for (final Object o: inputs) {
						builder.append (o);
					}
					
					return builder.toString ();
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
	
	private static class ConcatenateIntegerStringsOperationType extends AbstractOperationType implements TransformOperationType {
		@Override
		public Class<?> getReturnType() {
			return String.class;
		}

		@Override
		public Class<?> getPropertyBeanClass() {
			return null;
		}

		@Override
		public List<OperationInputType> getInputs() {
			final List<OperationInputType> inputs = new ArrayList<OperationInputType> ();
			inputs.add (new OperationInputType () {
				@Override
				public String getName () {
					return "a";
				}

				@Override
				public Type getInputType () {
					return Integer.TYPE;
				}

				@Override
				public String getDescription (final Locale locale) {
					return "a";
				}

				@Override
				public boolean isVariableInputCount () {
					return false;
				}
			});
			inputs.add (new OperationInputType () {
				@Override
				public boolean isVariableInputCount() {
					return true;
				}
				
				@Override
				public String getName() {
					return "b";
				}
				
				@Override
				public Class<?> getInputType() {
					return String.class;
				}
				
				@Override
				public String getDescription(Locale locale) {
					return "values";
				}
			});
			return inputs;
		}

		@Override
		public OperationExecuter createExecuter(Object operationProperties,
				MapperContext context) {
			return new OperationExecuter () {
				@Override
				public Object execute(MappingSource source, MappingDestination destination, List<Object> inputs) throws OperationExecutionException {
					final StringBuilder builder = new StringBuilder ();
					
					for (final Object o: inputs) {
						builder.append (o.toString ());
					}
					
					return builder.toString ();
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
}
