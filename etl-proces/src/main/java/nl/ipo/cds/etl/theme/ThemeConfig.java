package nl.ipo.cds.etl.theme;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import nl.ipo.cds.admin.ba.controller.beans.mapping.Mapping;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.DatasetHandlersFactory;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.Validator;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;
import nl.ipo.cds.etl.theme.schema.SchemaHarvester;
import nl.ipo.cds.etl.theme.schema.WfsSchemaHarvester;

import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

public abstract class ThemeConfig<T extends PersistableFeature> implements DatasetHandlersFactory<T> {

	private final Class<T> featureTypeClass;
	private final Map<Class<?>, ObjectDescriptor<?>> objectDescriptors;
	private final String themeName;
	
	public ThemeConfig (final String themeName, final Class<T> featureTypeClass) {
		this.themeName = themeName;
		this.featureTypeClass = featureTypeClass;
		this.objectDescriptors = new HashMap<Class<?>, ObjectDescriptor<?>> ();
	}
	
	@PostConstruct
	public void initialize () {
		final MessageSource messageSource = createMessageSource (featureTypeClass);
		
		this.objectDescriptors.put (featureTypeClass, introspect (featureTypeClass, messageSource));
	}

	@Override
	public boolean isJobSupported (final EtlJob job) {
		return job.getDatasetType () != null
				&& job.getDatasetType ().getThema () != null
				&& getThemeName ().equals (job.getDatasetType ().getThema ().getNaam ());
	}
	
	public String getThemeName () {
		return themeName;
	}
	
	public Class<T> getFeatureTypeClass () {
		return featureTypeClass;
	}

	public Set<ObjectDescriptor<?>> getObjectDescriptors () {
		return Collections.unmodifiableSet (new HashSet<ObjectDescriptor<?>> (objectDescriptors.values ()));
	}
	
	public Set<AttributeDescriptor<?>> getAttributeDescriptors () {
		final Set<AttributeDescriptor<?>> attributeDescriptors = new HashSet<AttributeDescriptor<?>> ();
		
		for (final Map.Entry<Class<?>, ObjectDescriptor<?>> entry: objectDescriptors.entrySet ()) {
			final ObjectDescriptor<?> objectDescriptor = entry.getValue ();

			attributeDescriptors.addAll (objectDescriptor.getAttributeDescriptors ());
		}		
		
		return Collections.unmodifiableSet (attributeDescriptors);
	}
	
	public abstract Validator<T> getValidator () throws ThemeConfigException;
	
	public SchemaHarvester getSchemaHarvester() {
		return new WfsSchemaHarvester();
	}	
	
	private MessageSource createMessageSource (final Class<T> cls) {
		final List<String> baseNames = new ArrayList<String> ();
		
		baseNames.add (String.format ("%s.messages", cls.getCanonicalName ()));
		baseNames.add (String.format ("%s.messages", cls.getPackage ().getName ()));
		
		final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource ();
		
		messageSource.setBasenames (baseNames.toArray (new String[baseNames.size ()]));
		
		return messageSource;
	}
	
	private ObjectDescriptor<T> introspect (final Class<T> cls, final MessageSource messageSource) {
		return new ObjectDescriptor<T> (cls, messageSource, getCandidateProperties (cls));
	}
	
	private PropertyDescriptor[] getCandidateProperties (final Class<?> cls) {
		final List<PropertyDescriptor> candidates = new ArrayList<PropertyDescriptor> ();
		
		for (final PropertyDescriptor propertyDescriptor: BeanUtils.getPropertyDescriptors (cls)) {
			final Method readerMethod = propertyDescriptor.getReadMethod ();
			final Method writerMethod = propertyDescriptor.getWriteMethod ();
			
			if (readerMethod == null || writerMethod == null) {
				continue;
			}
			
			for (final Method method: (new Method[] { readerMethod, writerMethod })) {
				if (method.getAnnotation (MappableAttribute.class) != null) {
					candidates.add (propertyDescriptor);
					break;
				}
			}
		}
		
		return candidates.toArray (new PropertyDescriptor[candidates.size ()]);
	}

	/**
	 * Return initial mapping for a specific attribute; default is null, but can be overridden by subclass.
	 * 
	 * @param name
	 * @return
	 */
	public Mapping getDefaultMappingForAttributeType(AttributeDescriptor<?> attributeDescriptor) {
		return null;
	}

	/**
	 * Whether or not the features in this theme are taggable.
	 */
	public boolean isTaggable() {
		return false;
	}

}
