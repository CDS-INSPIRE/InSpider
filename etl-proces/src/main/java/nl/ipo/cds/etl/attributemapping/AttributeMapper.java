package nl.ipo.cds.etl.attributemapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.executer.Executer;
import nl.ipo.cds.attributemapping.executer.MappingValidationException;
import nl.ipo.cds.attributemapping.executer.OperationExecutionException;
import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.attributemapping.AttributeMappingValidator.MessageKey;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.log.LogStringBuilder;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ObjectDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.tom.ows.CodeType;

public class AttributeMapper<T extends PersistableFeature> {

	private static final Log technicalLog = LogFactory.getLog (AttributeMapper.class);
	
	private final Job job;
	private final ThemeConfig<T> themeConfig;
	private final FeatureType featureType;
	private final Map<AttributeDescriptor<?>, OperationDTO> attributeMappings;
	private final LogStringBuilder<AttributeMappingValidator.MessageKey> logger;
	private final Mapping mapping;
	
	public AttributeMapper (final Job job,
			final ThemeConfig<T> themeConfig, 
			final FeatureType featureType, 
			final Map<AttributeDescriptor<?>, OperationDTO> attributeMappings,
			final JobLogger jobLogger,
			final Properties loggerProperties) {
		
		if (job == null) {
			throw new NullPointerException ("job is null");
		}
		if (themeConfig == null) {
			throw new NullPointerException ("themeConfig is null");
		}
		if (featureType == null) {
			throw new NullPointerException ("featureType is null");
		}
		if (attributeMappings == null) {
			throw new NullPointerException ("attributeMappings is null");
		}
		if (jobLogger == null) {
			throw new NullPointerException ("stringLogger is null");
		}
		if (loggerProperties == null) {
			throw new NullPointerException ("loggerProperties is null");
		}
		
		this.job = job;
		this.themeConfig = themeConfig;
		this.featureType = featureType;
		this.attributeMappings = new HashMap<AttributeDescriptor<?>, OperationDTO> (attributeMappings);
		
		// Create a logger:
		logger = new LogStringBuilder<AttributeMappingValidator.MessageKey> ();
		logger.setJobLogger (jobLogger);
		logger.setProperties (loggerProperties);
		
		// Build attribute mapping pipelines for each attribute:
		this.mapping = buildAttributeMappings ();
	}
	
	public boolean isValid () {
		return mapping != null;
	}
	
	public ThemeConfig<T> getThemeConfig () {
		return themeConfig;
	}
	
	public FeatureType getFeatureType () {
		return featureType;
	}
	
	public Map<AttributeDescriptor<?>, OperationDTO> getAttributeMappings () {
		return Collections.unmodifiableMap (attributeMappings);
	}
	
	public void processFeatures (final FeatureCollection featureCollection, final FeatureOutputStream<T> outputStream) {
		// Validate:
		if (featureCollection == null) {
			throw new NullPointerException ("featureCollection is null");
		}
		if (outputStream == null) {
			throw new NullPointerException ("outputStream is null");
		}
		if (!isValid ()) {
			throw new IllegalStateException ("The attribute mapping is not valid");
		}

		final MappingAttributeInfo[] attributes = mapping.attributes;
		final Object[] instances = new Object[mapping.objectClasses.length];
		final Executer[] executers = new Executer[attributes.length]; 
		final MappingDestination[] destinations = new MappingDestination[mapping.attributes.length];
		
		// Create mapping destinations:
		for (int i = 0; i < attributes.length; ++ i) {
			destinations[i] = createMappingDestination (attributes[i], instances);
			executers[i] = attributes[i].executer;
		}
		
		// Initialize an instance of each object:
		initializeInstances (instances);
		
		// Initialize values for keys:
		// TODO: Implement key checks.
		
		// Loop over features in the feature collection:
		for (final GenericFeature feature: featureCollection) {
			final MappingSource mappingSource = createMappingSource (feature);
			
			// Apply mapping to each known attribute:
			for (int i = 0; i < executers.length; ++ i) {
				try {
					executers[i].execute (mappingSource, destinations[i]);
				} catch (OperationExecutionException e) {
					technicalLog.error ("Error while executing mapping operation", e);
					logger.logEvent (
							job, 
							MessageKey.ATTRIBUTE_MAPPING_RUNTIME_ERROR, 
							LogLevel.ERROR, 
							attributes[i].attributeDescriptor.getDescription (Locale.getDefault ()),
							e.getLocalizedMessage ()
						);
				}
			}
			
			// Compare key values:
			// TODO: Implement key checks.
			
			// Store instances:
			// TODO: Simplified, only works for themes that use a single simple feature type during import (like Protected Sites).
			for (int i = 0; i < instances.length; ++ i) {
				if (instances[i] instanceof PersistableFeature) {
					((PersistableFeature)instances[i]).setId (feature.getId ());
				}
				outputStream.writeFeature (convert (instances[i]));
				instances[i] = createInstance (mapping.objectClasses[i]);
			}
		}
		
		// TODO: Finish writing instances.
	}
	
	@SuppressWarnings("unchecked")
	private T convert (final Object feature) {
		final PersistableFeature persistable = (PersistableFeature)feature; 
		return (T)persistable;
	}
	
	private MappingSource createMappingSource (final GenericFeature feature) {
		return new MappingSource() {
			@Override
			public boolean hasAttribute (final String name) {
				return feature.hasProperty (name);
			}
			
			@Override
			public Object getAttributeValue (final String name) {
				return feature.get (name);
			}
		};
	}
	
	private Object createInstance (final Class<?> cls) {
		try {
			return cls.newInstance ();
		} catch (InstantiationException e) {
			reportException (e);
			throw new RuntimeException (e);
		} catch (IllegalAccessException e) {
			reportException (e);
			throw new RuntimeException (e);
		}
	}
	
	private void initializeInstances (final Object[] instances) {
		for (int i = 0; i < instances.length; ++ i) {
			try {
				instances[i] = mapping.objectClasses[i].newInstance ();
			} catch (InstantiationException e) {
				reportException (e);
				throw new RuntimeException (e);
			} catch (IllegalAccessException e) {
				reportException (e);
				throw new RuntimeException (e);
			}
		}
	}
	
	private MappingDestination createMappingDestination (final MappingAttributeInfo attribute, final Object[] instances) {
		final Method setterMethod = attribute.setterMethod;
		final int index = attribute.objectIndex;
		
		final MappingDestination destination = new MappingDestination() {
			@Override
			public void setValue (final Object value) {
				try {
					setterMethod.invoke (instances[index], value);
				} catch (IllegalArgumentException e) {
					reportException (e);
					throw new RuntimeException (e);
				} catch (IllegalAccessException e) {
					reportException (e);
					throw new RuntimeException (e);
				} catch (InvocationTargetException e) {
					reportException (e);
					throw new RuntimeException (e);
				}
			}
		};

		// Convert code types:
		if (attribute.attributeDescriptor.getAttributeType ().equals (CodeType.class) && attribute.attributeDescriptor.getCodeSpace () != null) {
			final String codeSpace = attribute.attributeDescriptor.getCodeSpace ();
			
			return new MappingDestination () {
				@Override
				public void setValue (final Object value) {
					final CodeType codeType = (CodeType)value;
					
					if (codeType != null && codeType.getCodeSpace () == null) {
						final CodeType newCodeType = codeType != null ? new CodeType (codeType.getCode (), codeSpace) : null;
	
						destination.setValue (newCodeType);
					} else {
						destination.setValue (codeType);
					}
				}
			};
		}
		
		return destination;
	}
	
	private void reportException (final Exception e) {
		final String message = e.getLocalizedMessage ();
		logger.logEvent (job, MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, LogLevel.ERROR, message != null ? message : e.getClass ().getCanonicalName ());
	}
	
	private Mapping buildAttributeMappings () {
		final Set<AttributeDescriptor<?>> attributeDescriptors = themeConfig.getAttributeDescriptors ();
		final List<Class<?>> beanClasses = new ArrayList<Class<?>> ();
		final List<MappingAttributeInfo> attributes = new ArrayList<AttributeMapper.MappingAttributeInfo> ();
		
		for (final Map.Entry<AttributeDescriptor<?>, OperationDTO> entry: attributeMappings.entrySet ()) {
			final ObjectDescriptor<?> objectDescriptor = entry.getKey ().getObjectDescriptor ();

			// Locate the bean class:
			final Class<?> objectClass = objectDescriptor.getObjectClass ();
			int objectClassIndex = beanClasses.indexOf (objectClass);
			if (objectClassIndex < 0) {
				objectClassIndex = beanClasses.size ();
				beanClasses.add (objectClass);
			}
			
			final MappingAttributeInfo attributeInfo = buildAttributeMapping (entry.getKey (), objectClassIndex, entry.getValue (), attributeDescriptors);
			if (attributeInfo == null) {
				continue;
			}
			
			attributes.add (attributeInfo);
		}
		
		if (attributes.size () != attributeMappings.size ()) {
			return null;
		}
		
		return new Mapping (beanClasses.toArray (new Class<?>[beanClasses.size ()]), attributes.toArray (new MappingAttributeInfo[attributes.size ()]));
	}
	
	private MappingAttributeInfo buildAttributeMapping (final AttributeDescriptor<?> attributeDescriptor, final int objectIndex, final OperationDTO operation, final Set<AttributeDescriptor<?>> attributeDescriptors) {
		if (!attributeDescriptors.contains (attributeDescriptor)) {
			throw new IllegalStateException (String.format ("Attribute descriptor for %s not in theme %s", attributeDescriptor.getName (), themeConfig.getThemeName ()));
		}

		// Validate the attribute mapping:
		final AttributeMappingValidator validator = new AttributeMappingValidator (attributeDescriptor, featureType, logger);
		if (!validator.isValid (job, operation)) {
			return null;
		}
		
		// Create the root operation:
		final List<OperationInput> inputs = new ArrayList<OperationInput> ();
		inputs.add (new OperationInput() {
			@Override
			public Operation getOperation() {
				return operation;
			}
		});
		final Operation rootOperation = new Operation() {
			@Override
			public OperationType getOperationType() {
				return attributeDescriptor;
			}
			
			@Override
			public Object getOperationProperties() {
				return null;
			}
			
			@Override
			public List<OperationInput> getInputs() {
				return inputs;
			}
		};
		
		// Create an executer for the attribute mapping:
		final Executer executer;
		try {
			executer = new Executer (rootOperation, new MapperContext ());
		} catch (MappingValidationException e) {
			// Log a technical error if the executer fails to initialize:
			logger.logEvent (job, MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, LogLevel.ERROR, e.getLocalizedMessage ());
			return null;
		} catch (OperationExecutionException e) {
			logger.logEvent (job, MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, LogLevel.ERROR, e.getLocalizedMessage ());
			return null;
		}
		
		return new MappingAttributeInfo (
				attributeDescriptor,
				objectIndex,
				attributeDescriptor.getPropertyDescriptor ().getWriteMethod (),
				executer
			);
	}
	
	private static class Mapping {
		public final Class<?>[] objectClasses;
		public final MappingAttributeInfo[] attributes;
		
		public Mapping (final Class<?>[] objectClasses, final MappingAttributeInfo[] attributes) {
			this.objectClasses = Arrays.copyOf (objectClasses, objectClasses.length);
			this.attributes = Arrays.copyOf (attributes, attributes.length);
		}
	}
	
	private static class MappingAttributeInfo {
		public final AttributeDescriptor<?> attributeDescriptor;
		public final int objectIndex;
		public final Method setterMethod;
		public final Executer executer;
		
		public MappingAttributeInfo (
				final AttributeDescriptor<?> attributeDescriptor,
				final int objectIndex, 
				final Method setterMethod, 
				final Executer executer) {

			this.attributeDescriptor = attributeDescriptor;
			this.setterMethod = setterMethod;
			this.objectIndex = objectIndex;
			this.executer = executer;
		}
	}
}
