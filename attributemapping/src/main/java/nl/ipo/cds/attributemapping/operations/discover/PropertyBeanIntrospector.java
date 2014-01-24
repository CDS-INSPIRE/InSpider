package nl.ipo.cds.attributemapping.operations.discover;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import nl.ipo.cds.attributemapping.operations.PropertyBeanDescription;
import nl.ipo.cds.attributemapping.operations.PropertyBeanFieldDescription;

import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

public class PropertyBeanIntrospector {

	private final Set<String> baseNames = new HashSet<String> ();
	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource ();
	private final ConcurrentHashMap<Class<?>, BeanDescription> beanDescriptions = new ConcurrentHashMap<Class<?>, BeanDescription> ();
	
	public PropertyBeanDescription getDescriptorForBeanClass (final Class<?> beanClass) {
		if (beanClass == null) {
			throw new IllegalArgumentException ("beanClass cannot be null");
		}
		
		final BeanDescription beanDescription = beanDescriptions.get (beanClass);
		
		if (beanDescription == null) {
			updateMessageSource (beanClass);
			
			final BeanDescription newBeanDescription = new BeanDescription (beanClass, messageSource);
			final BeanDescription existingBeanDescription = beanDescriptions.putIfAbsent (beanClass, newBeanDescription);
			
			if (existingBeanDescription != null) {
				return existingBeanDescription;
			}
			
			return newBeanDescription;
		}
		
		return beanDescription;
	}
	
	private synchronized void updateMessageSource (final Class<?> beanClass) {
		final String baseName = String.format ("%s.messages", beanClass.getPackage ().getName ());
		if (baseNames.add (baseName) || baseNames.add (beanClass.getCanonicalName ())) {
			messageSource.setBasenames (baseNames.toArray (new String[baseNames.size ()]));
		}
	}
	
	private final static class BeanDescription implements PropertyBeanDescription {
		private final Class<?> beanClass;
		private final MessageSource messageSource;
		private final List<PropertyBeanFieldDescription> fieldDescriptions;
		
		public BeanDescription (final Class<?> beanClass, final MessageSource messageSource) {
			this.beanClass = beanClass;
			this.messageSource = messageSource;
			this.fieldDescriptions = this.introspect ();
		}

		public MessageSource getMessageSource () {
			return messageSource;
		}
		
		@Override
		public Class<?> getBeanClass () {
			return beanClass;
		}

		@Override
		public Collection<PropertyBeanFieldDescription> getFieldDescriptions() {
			return fieldDescriptions;
		}
		
		private List<PropertyBeanFieldDescription> introspect () {
			final List<PropertyBeanFieldDescription> result = new ArrayList<PropertyBeanFieldDescription> ();
			
			for (final PropertyDescriptor pd: BeanUtils.getPropertyDescriptors (beanClass)) {
				if (pd.getReadMethod () == null || pd.getWriteMethod () == null) {
					continue;
				}
				
				final Class<?> cls = pd.getPropertyType ();
				
				if (!String.class.equals (cls) && !Boolean.TYPE.equals (cls) && !Integer.TYPE.equals (cls)) {
					continue;
				}
				
				result.add (new FieldDescription (this, pd));
			}
			
			return result;
		}
	}
	
	private final static class FieldDescription implements PropertyBeanFieldDescription {

		private final BeanDescription beanDescription;
		private final PropertyDescriptor propertyDescriptor;
		
		public FieldDescription (final BeanDescription beanDescription, final PropertyDescriptor propertyDescriptor) {
			this.beanDescription = beanDescription;
			this.propertyDescriptor = propertyDescriptor;
		}
		
		@Override
		public String getName () {
			return propertyDescriptor.getName ();
		}

		@Override
		public String getDescription (final Locale locale) {
			
			return beanDescription.getMessageSource ().getMessage (
					getMessageKey (), null, getName (), locale);
		}

		@Override
		public Type getType () {
			return propertyDescriptor.getReadMethod ().getGenericReturnType ();
		}
		
		private String getMessageKey () {
			return String.format (
					"%s.%s.description", 
					beanDescription.getBeanClass ().getCanonicalName (), 
					propertyDescriptor.getName ()
				);
		}
		
	}
}
