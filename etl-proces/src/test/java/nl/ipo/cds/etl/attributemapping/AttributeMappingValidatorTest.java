package nl.ipo.cds.etl.attributemapping;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.dao.attributemapping.InputOperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationInputDTO;
import nl.ipo.cds.dao.attributemapping.TransformOperationDTO;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.QName;
import nl.ipo.cds.etl.attributemapping.AttributeMappingValidator.MessageKey;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.etl.operations.input.StringConstantInput;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform.Operation;
import nl.ipo.cds.etl.operations.transform.MakeInspireIdTransform;
import nl.ipo.cds.etl.operations.transform.SplitStringTransform;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Category(IntegrationTests.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AttributeMappingValidatorTest.Config.class)
public class AttributeMappingValidatorTest extends AbstractAttributeMapperTest {

	private static final String themeName = "Protected sites";
	
	@Inject
	private ThemeDiscoverer themeDiscoverer;
	
	@Inject
	private OperationDiscoverer operationDiscoverer;
	
	private Map<String, AttributeDescriptor<?>> attributeDescriptors;
	
	protected Map<MessageKey, List<String[]>> lines;
	
	@Configuration
	@ComponentScan (basePackageClasses = { 
		nl.ipo.cds.etl.theme.protectedSite.config.Package.class, 
		nl.ipo.cds.etl.theme.Package.class
	})
	public static class Config {
	}
	
	@Before
	public void createLines () {
		 lines = new HashMap<MessageKey, List<String[]>> ();		
	}
	
	@Before
	public void findAttributeDescriptors () {
		final ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration (themeName);
		
		if (themeConfig == null) {
			throw new IllegalArgumentException (String.format ("Theme %s not found", themeName));
		}

		attributeDescriptors = new HashMap<String, AttributeDescriptor<?>> ();
		
		for (final AttributeDescriptor<?> ad: themeConfig.getAttributeDescriptors ()) {
			attributeDescriptors.put (ad.getName (), ad);
		}
	}
	
	@Test
	public void testNullOperation () {
		assertFalse (validate (attributeDescriptors.get("geometry"), null));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_MISSING);
	}
	
	@Test
	public void testRootIsNotTransform () {
		assertFalse (validate (attributeDescriptors.get ("geometry"), new OperationDTO () {
			@Override
			public Object getOperationProperties() {
				return null;
			}

			@Override
			public List<OperationInput> getInputs() {
				return null;
			}

			@Override
			public OperationType getOperationType() {
				return null;
			}
		}));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR);
	}
	
	@Test
	public void testRootIsNotConditionalTransform () {
		assertFalse (validate (
				attributeDescriptors.get ("geometry"), 
				new TransformOperationDTO (
						getOperationType (SplitStringTransform.class), 
						new ArrayList<OperationInputDTO> (), 
						null
				)
		));
		
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR);
	}
	
	@Test
	public void testRootConditionCountMismatch () {
		final ConditionalTransform.Settings settings = new ConditionalTransform.Settings ();
		final List<OperationInputDTO> inputs = new ArrayList<OperationInputDTO> ();
		final ConditionalTransform.Condition condition = new ConditionalTransform.Condition ();
		final List<ConditionalTransform.Condition> conditions = new ArrayList<ConditionalTransform.Condition> ();
		
		conditions.add (condition);
		
		settings.setConditions (conditions);
		
		assertFalse (validate (
				attributeDescriptors.get ("geometry"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						inputs,
						settings
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR);
	}

	@Test
	public void testConditionInvalidAttribute () {
		final ConditionalTransform.Settings settings = new ConditionalTransform.Settings ();
		final ConditionalTransform.Condition condition = new ConditionalTransform.Condition ();

		condition.setAttribute ("invalidAttribute");
		condition.setOperation (Operation.IS_NULL);

		settings.setConditions (Arrays.asList (condition));
		
		// ATTRIBUTE_MAPPING_CONDITION_INVALID_ATTRIBUTE
		assertFalse (validate (
				attributeDescriptors.get ("geometry"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (constantTextInput (), constantTextInput ()),
						settings
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_CONDITION_INVALID_ATTRIBUTE);
		assertNoTechnicalErrors ();
	}
	
	@Test
	public void testConditionNoValue () {
		final ConditionalTransform.Settings settings = new ConditionalTransform.Settings ();
		final ConditionalTransform.Condition condition = new ConditionalTransform.Condition ();

		condition.setAttribute ("geometry");
		condition.setOperation (Operation.IN);

		settings.setConditions (Arrays.asList (condition));
		
		// ATTRIBUTE_MAPPING_CONDITION_INVALID_ATTRIBUTE
		assertFalse (validate (
				attributeDescriptors.get ("geometry"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (constantTextInput (), constantTextInput ()),
						settings
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_CONDITION_NO_VALUE);
		assertNoTechnicalErrors ();
	}
	
	@Test
	public void testConditionTypesIncompatible () {
		final ConditionalTransform.Settings settings = new ConditionalTransform.Settings ();
		final ConditionalTransform.Condition condition = new ConditionalTransform.Condition ();

		condition.setAttribute ("geometry");
		condition.setOperation (Operation.IS_NULL);

		settings.setConditions (Arrays.asList (condition));

		// Attempt to assign string to geometry:
		assertFalse (validate (
				attributeDescriptors.get ("geometry"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (constantTextInput (), constantTextInput ()),
						settings
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TYPES_INCOMPATIBLE, 2);
		assertNoTechnicalErrors ();
	}
	
	@Test
	public void testTypesIncompatible () {
		// ATTRIBUTE_MAPPING_TYPES_INCOMPATIBLE
		final ConditionalTransform.Settings settings = new ConditionalTransform.Settings ();

		// Constant text -> Split string -> Make inspire ID
		// This assigns an array to a string and should fail.
		final TransformOperationDTO constantText = new TransformOperationDTO (
				getOperationType (StringConstantInput.class), 
				Arrays.<OperationInputDTO>asList (), 
				new StringConstantInput.Settings ()
			);
		final TransformOperationDTO splitString = new TransformOperationDTO (
				getOperationType (SplitStringTransform.class),
				Arrays.asList (new OperationInputDTO[] { new OperationInputDTO (constantText) }),
				new SplitStringTransform.Settings ()
			);
		final TransformOperationDTO makeInspireID = new TransformOperationDTO (
				getOperationType (MakeInspireIdTransform.class),
				Arrays.asList (new OperationInputDTO[] { new OperationInputDTO (splitString), constantTextInput (), constantTextInput (), constantTextInput () }),
				null
			);

		// Attempt to assign string to geometry:
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (new OperationInputDTO (makeInspireID)),
						settings
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TYPES_INCOMPATIBLE);
		assertNoTechnicalErrors ();
	}
	
	@Test
	public void testWrongOperationType () {
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (new OperationInputDTO (new OperationDTO () {
							@Override
							public Object getOperationProperties() {
								return null;
							}

							@Override
							public List<OperationInput> getInputs() {
								return null;
							}

							@Override
							public OperationType getOperationType() {
								return null;
							}
						})),
						new ConditionalTransform.Settings ()
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR);
	}
	
	@Test
	public void testInputNotFound () {
		// ATTRIBUTE_MAPPING_INPUT_NOT_FOUND
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (input ("nonExistingInput", AttributeType.STRING)),
						new ConditionalTransform.Settings ()
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_INPUT_NOT_FOUND);
		assertNoTechnicalErrors ();
	}
	
	@Test
	public void testInputNoLongerAvailable () {
		
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (input ("testAttribute", AttributeType.STRING, "testAttributeOld")),
						new ConditionalTransform.Settings ()
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_INPUT_NO_LONGER_AVAILABLE);
		assertNoTechnicalErrors ();
	}
	
	@Test
	public void testInputTypeChanged () {
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (input ("testAttribute", AttributeType.INTEGER)),
						new ConditionalTransform.Settings ()
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_INPUT_TYPE_CHANGED);
		assertNoTechnicalErrors ();
	}
	
	@Test
	public void testInputCountInvalid () {
		// ATTRIBUTE_MAPPING_TYPES_INCOMPATIBLE
		final ConditionalTransform.Settings settings = new ConditionalTransform.Settings ();

		// Constant text -> Split string -> Make inspire ID
		// This assigns an array to a string and should fail.
		final TransformOperationDTO makeInspireID = new TransformOperationDTO (
				getOperationType (MakeInspireIdTransform.class),
				Arrays.asList (new OperationInputDTO[] { constantTextInput (), constantTextInput (), constantTextInput () }),
				null
			);

		// Attempt to assign string to geometry:
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.asList (new OperationInputDTO (makeInspireID)),
						settings
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_INPUT_COUNT_INVALID);
		assertNoTechnicalErrors ();
	}
	
	@Test
	public void testOperationWithoutOperationType () {
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.<OperationInputDTO>asList (new OperationInputDTO (new TransformOperationDTO (null, Arrays.<OperationInputDTO>asList (), null))),
						new ConditionalTransform.Settings ()
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR);
	}
	
	@Test
	public void testOperationWithoutPropertiesObject () {
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.<OperationInputDTO>asList (new OperationInputDTO (new TransformOperationDTO (getOperationType (StringConstantInput.class), Arrays.<OperationInputDTO>asList (), null))),
						new ConditionalTransform.Settings ()
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR);
	}
	
	@Test
	public void testOperationWithPropertiesObjectOfWrongType () {
		assertFalse (validate (
				attributeDescriptors.get ("inspireID"),
				new TransformOperationDTO (
						getOperationType (ConditionalTransform.class),
						Arrays.<OperationInputDTO>asList (new OperationInputDTO (new TransformOperationDTO (getOperationType (StringConstantInput.class), Arrays.<OperationInputDTO>asList (), new ConditionalTransform.Settings ()))),
						new ConditionalTransform.Settings ()
				)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR);
	}
	
	@Test
	public void testOperationWithNullInputs () {
		assertFalse (validate (
			attributeDescriptors.get ("inspireID"),
			new TransformOperationDTO (
				getOperationType (ConditionalTransform.class),
				Arrays.<OperationInputDTO>asList(new OperationInputDTO (
					new TransformOperationDTO (
						getOperationType (MakeInspireIdTransform.class),
						Arrays.<OperationInputDTO>asList (
							new OperationInputDTO (new TransformOperationDTO (
								getOperationType (StringConstantInput.class),
								Arrays.<OperationInputDTO>asList (),
								new StringConstantInput.Settings ()
							)),
							new OperationInputDTO (null),
							new OperationInputDTO (null),
							new OperationInputDTO (new TransformOperationDTO (
								getOperationType (StringConstantInput.class),
								Arrays.<OperationInputDTO>asList (),
								new StringConstantInput.Settings ()
							))
						),
						null
					)
				)),
				new ConditionalTransform.Settings ()
			)
		));
		assertHasMessage (MessageKey.ATTRIBUTE_MAPPING_INPUT_COUNT_INVALID);
	}

	private OperationInputDTO input (final String attributeName, final AttributeType attributeType) {
		return input (attributeName, attributeType, attributeName);
	}
	
	private OperationInputDTO input (final String attributeName, final AttributeType attributeType, final String newName) {
		final FeatureType featureType = getFeatureType ();
		FeatureTypeAttribute attribute = null;
		
		for (final FeatureTypeAttribute attr: featureType.getAttributes ()) {
			if (attr.getName ().getLocalPart().equals (attributeName)) {
				attribute = attr;
				break;
			}
		}
		
		return new OperationInputDTO (new InputOperationDTO (attribute, newName, attributeType));
	}
	
	private TransformOperationDTO constantText () {
		return new TransformOperationDTO(getOperationType (StringConstantInput.class), Arrays.<OperationInputDTO>asList (), new StringConstantInput.Settings ());
	}
	
	private OperationInputDTO constantTextInput () {
		return new OperationInputDTO (constantText ());
	}
	
	private void assertNoTechnicalErrors () {
		if (lines.containsKey (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR)) {
			final StringBuilder b = new StringBuilder ();
			for (final String[] item: lines.get (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR)) {
				if (b.length () > 0) {
					b.append (",");
				}
				b.append (item[0]);
			}
			fail (b.toString ());
		}
	}
	
	private void assertHasMessage (final MessageKey messageKey) {
		assertHasMessage (messageKey, 1);
	}
	
	private void assertHasMessage (final MessageKey messageKey, final int count) {
		final List<String[]> messages = lines.get (messageKey);

		if ((messages == null ? 0 : messages.size ()) != count) {
			fail (String.format ("Expected: %d instances of %s (found %s)", count, messageKey.toString (), getMessageKeys ()));
		}
	}
	
	private String getMessageKeys () {
		final StringBuilder builder = new StringBuilder ();
		
		for (final MessageKey key: lines.keySet ()) {
			if (builder.length () > 0) {
				builder.append (",");
			}
			builder.append (String.format("%s:%d", key, lines.get (key).size ()));
			if (lines.get(key).get (0).length > 0) {
				builder.append (String.format("(%s)", lines.get(key).get(0)[0]));
			}
			
		}
		
		return builder.toString ();
	}
	
	private boolean validate (final AttributeDescriptor<?> attributeDescriptor, final OperationDTO rootOperation) {
		final EventLogger<AttributeMappingValidator.MessageKey> logger = new EventLogger<AttributeMappingValidator.MessageKey> () {
			@Override
			public String logEvent(final Job job, final MessageKey messageKey, final LogLevel logLevel, final String... messageValues) {
				final List<String[]> list;
				
				if (!lines.containsKey (messageKey)) {
					list = new ArrayList<String[]> ();
					lines.put (messageKey, list);
				} else {
					list = lines.get (messageKey);
				}
				
				list.add (messageValues);
				
				return messageKey.toString ();
			}
	
			@Override
			public String logEvent(Job job, MessageKey messageKey, LogLevel logLevel, double x, double y, String gmlId, String... messageValues) {
				return logEvent (job, messageKey, logLevel, messageValues);
			}

			@Override
			public String logEvent(Job job, MessageKey messageKey,
					LogLevel logLevel, Map<String, Object> context,
					String... messageValues) {
				return logEvent (job, messageKey, logLevel, messageValues);
			}
		};
	
		final AttributeMappingValidator validator = new AttributeMappingValidator (
				attributeDescriptor, 
				getFeatureType (),
				logger
			);
		
		return validator.isValid (null, rootOperation);
	}
	
	private FeatureType getFeatureType () {
		final FeatureType featureType = new FeatureType() {
			@Override
			public QName getName() {
				return new QName() {
					@Override
					public int compareTo(QName o) {
						return getLocalPart ().compareTo (o.getLocalPart ());
					}
					
					@Override
					public String getNamespace() {
						return "http://www.idgis.nl";
					}
					
					@Override
					public String getLocalPart() {
						return "TestFeatureType";
					}
				};
			}
			
			@Override
			public Set<FeatureTypeAttribute> getAttributes() {
				final Set<FeatureTypeAttribute> attributes = new HashSet<FeatureTypeAttribute> ();
				
				attributes.add (new FeatureTypeAttribute() {
					@Override
					public int compareTo (FeatureTypeAttribute o) {
						return getName ().compareTo (o.getName ());
					}
					
					@Override
					public AttributeType getType() {
						return AttributeType.STRING;
					}
					
					@Override
					public QName getName() {
						return new QName () {
							@Override
							public int compareTo(QName o) {
								return getLocalPart ().compareTo (o.getLocalPart ());
							}

							@Override
							public String getNamespace() {
								return "http://www.idgis.nl";
							}

							@Override
							public String getLocalPart() {
								return "testAttribute";
							}
						};
					}
				});
				
				return attributes;
			}
		};
		
		return featureType;
	}
}
