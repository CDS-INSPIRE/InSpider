package nl.ipo.cds.attributemapping.operations;

import java.lang.reflect.Type;
import java.util.Locale;

public interface OperationInputType {
	String getName ();
	Type getInputType ();
	String getDescription (Locale locale);
	boolean isVariableInputCount ();
}
