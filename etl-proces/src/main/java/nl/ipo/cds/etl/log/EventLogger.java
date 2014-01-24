/**
 * 
 */
package nl.ipo.cds.etl.log;

import java.util.Map;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;

/**
 * This the logger that is used to log user related messages.<br>
 * 
 * @author 
 *
 */
public interface EventLogger<T extends Enum<T>> {
	String logEvent(Job job, T messageKey, LogLevel logLevel, String... messageValues);
	String logEvent(Job job, T messageKey, LogLevel logLevel, double x, double y, String gmlId, String... messageValues);
	String logEvent(Job job, T messageKey, LogLevel logLevel, Map<String, Object> context, String... messageValues);
}
