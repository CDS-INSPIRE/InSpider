package nl.ipo.cds.etl.filtering;

import static org.junit.Assert.*;
import static nl.ipo.cds.etl.filtering.FilterExpressionFactory.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.FilterExpression;
import nl.ipo.cds.domain.OperatorExpression.OperatorType;
import nl.ipo.cds.domain.QName;
import nl.ipo.cds.domain.ValidateJob;
import nl.ipo.cds.etl.filtering.FilterExpressionValidator.MessageKey;
import nl.ipo.cds.etl.log.EventLogger;

import org.junit.Before;
import org.junit.Test;

public class FilterExpressionValidatorTest {

	private FeatureType featureType;
	private FilterExpressionValidator validator;
	private Logger logger;
	
	@Before
	public void createValidator () {
		featureType = createFeatureType ();
		logger = new Logger ();
		validator = new FilterExpressionValidator (featureType, logger);
	}

	@Test
	public void testNoFilter () {
		assertFalse (validator.isValid (new ValidateJob (), null));
		assertTrue (logger.hasMessage (MessageKey.DATASET_FILTER_MISSING, 1));
	}
	
	@Test
	public void testNoDatasetRootExpression () {
		final DatasetFilter filter = new DatasetFilter ();
		
		assertFalse (validator.isValid (new ValidateJob (), filter));
		assertTrue (logger.hasMessage (MessageKey.DATASET_FILTER_NO_DATASET, 1));
		assertTrue (logger.hasMessage (MessageKey.DATASET_FILTER_NO_ROOT_EXPRESSION, 1));
	}
	
	/*
	DATASET_FILTER_LOGICAL_OPERATOR_WITHOUT_INPUTS,	// Logical operator must have at least one input.
	DATASET_FILTER_LOGICAL_OPERATOR_MISSING_INPUT,	// Logical operator has a missing (null) input.
	DATASET_FILTER_LOGICAL_OPERATOR_WRONG_TYPE,		// Logical operator input is not of type OperatorExpression.
	
	DATASET_FILTER_OPERATOR_NO_ATTRIBUTE,// Comparison operator must have an attribute.
	DATASET_FILTER_OPERATOR_NO_VALUE,	// Comparison operator has no value.
	DATASET_FILTER_OPERATOR_INCOMPATIBLE_TYPES, // Incompatible types in comparison operator.
	DATASET_FILTER_OPERATOR_INPUT_COUNT,			// Operator must have exactly two inputs.
	
	DATASET_FILTER_MISSING_ATTRIBUTE,				// An attribute is missing from the feature type.
	*/

	@Test
	public void testLogicalOperatorWithoutInputs () {
		assertValidationMessage (MessageKey.DATASET_FILTER_LOGICAL_OPERATOR_WITHOUT_INPUTS, 1, and ());
	}
	
	@Test
	public void testLogicalOperatorMissingInput () {
		assertValidationMessage (MessageKey.DATASET_FILTER_LOGICAL_OPERATOR_MISSING_INPUT, 2, and (null, null, like (attribute ("a", AttributeType.STRING), "*a*")));
	}
	
	@Test
	public void testLogicalOperatorWrongType () {
		assertValidationMessage (MessageKey.DATASET_FILTER_LOGICAL_OPERATOR_WRONG_TYPE, 2, operatorExpression (
				OperatorType.AND, 
				attribute ("a", AttributeType.STRING),
				stringValue ("b")
			));
	}
	
	@Test
	public void testOperatorNoAttribute () {
		assertValidationMessage (MessageKey.DATASET_FILTER_OPERATOR_NO_ATTRIBUTE, 1, equal (null, stringValue ("Hello, world!")));
	}
	
	@Test
	public void testOperatorNoValue () {
		assertValidationMessage (MessageKey.DATASET_FILTER_OPERATOR_NO_VALUE, 1, operatorExpression (OperatorType.EQUALS, attribute ("a", AttributeType.STRING), and ()));
	}
	
	@Test
	public void testOperatorIncompatibleTypes () {
		assertValidationMessage (MessageKey.DATASET_FILTER_OPERATOR_INCOMPATIBLE_TYPES, 1, equal (attribute ("a", AttributeType.STRING), integerValue (123)));
	}
	
	@Test
	public void testOperatorInputCount () {
		assertValidationMessage (MessageKey.DATASET_FILTER_OPERATOR_INPUT_COUNT, 1, equal (null, null));
	}
	
	@Test
	public void testOperatorMissingAttribute () {
		assertValidationMessage (MessageKey.DATASET_FILTER_OPERATOR_MISSING_ATTRIBUTE, 1, equal (attribute ("b", AttributeType.STRING), stringValue ("bla")));
		assertNoValidationMessage (equal (attribute ("a/operationName", AttributeType.STRING), stringValue ("bla")));
		
	}
	
	private void assertNoValidationMessage (final FilterExpression filterExpression) {
		final Job job = new ValidateJob ();
		final Dataset dataset = new Dataset ();
		final DatasetFilter filter = new DatasetFilter ();
		
		filter.setDataset (dataset);
		filter.setRootExpression (filterExpression);
		
		assertTrue (validator.isValid (job, filter));
	}
	
	private void assertValidationMessage (final MessageKey messageKey, int count, final FilterExpression filterExpression) {
		final Job job = new ValidateJob ();
		final Dataset dataset = new Dataset ();
		final DatasetFilter filter = new DatasetFilter ();
		
		filter.setDataset (dataset);
		filter.setRootExpression (filterExpression);
		
		assertFalse ("Validation has to fail", validator.isValid (job, filter));
		assertTrue (String.format ("Must have message %s, found: %s", messageKey, logger.getMessageKeys ()), logger.hasMessage (messageKey, count));
	}
	
	private static class Logger implements EventLogger<MessageKey> {
		private final Map<MessageKey, Integer> messageKeys = new HashMap<FilterExpressionValidator.MessageKey, Integer> ();
		
		@Override
		public String logEvent (final Job job, final MessageKey messageKey, final LogLevel logLevel, final String... messageValues) {
			if (messageKeys.containsKey (messageKey)) {
			}
			
			messageKeys.put (messageKey, messageKeys.containsKey (messageKey) ? messageKeys.get (messageKey) + 1 : 1);
			
			return "";
		}

		@Override
		public String logEvent (final Job job, final MessageKey messageKey, final LogLevel logLevel, final double x, final double y, final String gmlId, final String... messageValues) {
			return logEvent (job, messageKey, logLevel, messageValues);
		}

		@Override
		public String logEvent (final Job job, final MessageKey messageKey, final LogLevel logLevel, final Map<String, Object> context, final String... messageValues) {
			return logEvent (job, messageKey, logLevel, messageValues);
		}
		
		public boolean hasMessage (final MessageKey messageKey, final int count) {
			if (count > 0) {
				return messageKeys.containsKey (messageKey) && messageKeys.get (messageKey) == count;
			} else {
				return messageKeys.containsKey (messageKey) && messageKeys.get (messageKey) > 0;
			}
		}
		
		public String getMessageKeys () {
			final StringBuilder builder = new StringBuilder ();
			
			for (final Map.Entry<MessageKey, Integer> entry: messageKeys.entrySet ()) {
				if (builder.length () > 0) {
					builder.append (",");
				}
				builder.append (String.format ("%s(%d)", entry.getKey (), entry.getValue ()));
			}
			
			return builder.toString ();
		}
	}
	
	private FeatureType createFeatureType () {
		return new FeatureType() {
			
			@Override
			public QName getName () {
				return new QName () {
					@Override
					public int compareTo (final QName o) {
						return getLocalPart ().compareTo (o.getLocalPart ()); 
					}
					
					@Override
					public String getNamespace () {
						return "http://www.idgis.nl/test";
					}
					
					@Override
					public String getLocalPart () {
						return "TestFeatureType";
					}
				};
			}
			
			@Override
			public Set<FeatureTypeAttribute> getAttributes () {
				return new HashSet<FeatureTypeAttribute>() {
					private static final long serialVersionUID = 1L;
					{
						add (new FeatureTypeAttribute () {
							
							@Override
							public int compareTo (final FeatureTypeAttribute o) {
								return getName ().compareTo (o.getName ());
							}
							
							@Override
							public AttributeType getType () {
								return AttributeType.STRING;
							}
							
							@Override
							public QName getName () {
								return new QName () {
									
									@Override
									public int compareTo (final QName o) {
										return getLocalPart ().compareTo (o.getLocalPart ());
									}
									
									@Override
									public String getNamespace () {
										return "http://www.idgis.nl/test";
									}
									
									@Override
									public String getLocalPart () {
										return "a";
									}
								};
							}
						});
						
						add (new FeatureTypeAttribute () {
							
							@Override
							public int compareTo (final FeatureTypeAttribute o) {
								return getName ().compareTo (o.getName ());
							}
							
							@Override
							public AttributeType getType () {
								return AttributeType.INTEGER;
							}
							
							@Override
							public QName getName () {
								return new QName () {
									
									@Override
									public int compareTo (final QName o) {
										return getLocalPart ().compareTo (o.getLocalPart ());
									}
									
									@Override
									public String getNamespace () {
										return "http://www.idgis.nl/test";
									}
									
									@Override
									public String getLocalPart () {
										return "b";
									}
								};
							}
						});
					}
					
				};
			}
		};
	}
}
