package nl.ipo.cds.attributemapping.operations;

import java.lang.reflect.Type;
import java.util.Locale;

public interface PropertyBeanFieldDescription {

	String getName ();
	String getDescription (final Locale locale);
	Type getType ();
}
