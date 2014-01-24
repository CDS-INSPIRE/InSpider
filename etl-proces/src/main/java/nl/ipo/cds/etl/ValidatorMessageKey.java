package nl.ipo.cds.etl;

import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;

public interface ValidatorMessageKey<K extends Enum<K> & ValidatorMessageKey<K, C>, C extends ValidatorContext<K, C>> extends ValidationMessage<K, C> {

	int getMaxMessageLog ();
	boolean isAddToShapeFile ();
	LogLevel getLogLevel ();
	K getMaxMessageKey (); 
}
