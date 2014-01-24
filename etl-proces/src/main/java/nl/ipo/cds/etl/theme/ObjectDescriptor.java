package nl.ipo.cds.etl.theme;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.MessageSource;

public class ObjectDescriptor<T> {

	private final Class<T> objectClass;
	private final Set<AttributeDescriptor<T>> attributeDescriptors;
	private final MessageSource messageSource;
	
	ObjectDescriptor (final Class<T> objectClass, final MessageSource messageSource, final PropertyDescriptor ... propertyDescriptors) {
		this.objectClass = objectClass;
		this.attributeDescriptors = new HashSet<AttributeDescriptor<T>> ();
		this.messageSource = messageSource;
		
		for (final PropertyDescriptor propertyDescriptor: propertyDescriptors) {
			this.attributeDescriptors.add (new AttributeDescriptor<T> (this, propertyDescriptor));
		}
	}
	
	public Class<T> getObjectClass () {
		return objectClass;
	}
	
	public MessageSource getMessageSource () {
		return messageSource;
	}
	
	public Set<AttributeDescriptor<T>> getAttributeDescriptors () {
		return Collections.unmodifiableSet (attributeDescriptors);
	}
}
