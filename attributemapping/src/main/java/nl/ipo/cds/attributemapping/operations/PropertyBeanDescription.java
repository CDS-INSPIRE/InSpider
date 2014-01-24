package nl.ipo.cds.attributemapping.operations;

import java.util.Collection;

public interface PropertyBeanDescription {

	Class<?> getBeanClass ();
	Collection<PropertyBeanFieldDescription> getFieldDescriptions ();
}
