package nl.ipo.cds.etl.log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogStringBuilder<T extends Enum<T>> implements EventLogger<T> {
	
	private static final Log technicalLog = LogFactory.getLog(LogStringBuilder.class);
	
	private JobLogger jobLogger;
	private Map<Object, Object> properties;
	
	public void setJobLogger(JobLogger stringLogger) {
		this.jobLogger = stringLogger;
	}
	
	public void setProperties(Map<Object, Object> properties) {
		this.properties = properties;
	}

	@Override
	public String logEvent(Job job, T messageKey, LogLevel logLevel, String... messageValues) {
		
		String message = createOneStringMessage(messageKey, messageValues);
		jobLogger.logString(job, messageKey.toString(), logLevel, message);
		
		return message;
	}

	public static String createStringMessage (final Map<Object, Object> properties, final String messageKey, final String ... messageValues) {
		technicalLog.debug(messageKey.toString());

		String message;
		Map<Object, Object> propertiesValues = new HashMap<Object, Object>(properties);
		for(int i = 0; i < messageValues.length; i++) {
			propertiesValues.put("" + i, messageValues[i]);
			technicalLog.debug(i + ":\t" + messageValues[i]);
		}
		
		if(properties.containsKey(messageKey.toString())) {
			message = (String)properties.get(messageKey.toString());
			
			// substitute properties
			HashSet<String> foundProperties = new HashSet<String>();
			do {
				for(String property : foundProperties) {
					String placeHolder = "\\$\\{" + property + "\\}";
					// handle null properties
					Object propValue = propertiesValues.get(property);
					String propertyValue = (propValue == null) ? "null" : propValue.toString();
					
					propertyValue = propertyValue.replaceAll("\\$", "\\\\\\$");					
					message = message.replaceAll(placeHolder, propertyValue);
				}
				
				foundProperties.clear();
				
				int varIndex = message.indexOf("${");
				while(varIndex != -1 && varIndex < message.length()) {
					int endVarIndex = message.indexOf('}', varIndex + 1);					
					
					String property = message.substring(varIndex + 2, endVarIndex);					
					if(property.indexOf("${") == -1 && propertiesValues.containsKey(property)) {						
						foundProperties.add(property);
					}
					
					varIndex = message.indexOf("${", varIndex + 1) ;
				}
			} while(foundProperties.size() > 0);
		} else {
			StringBuilder sb = new StringBuilder(messageKey.toString());
			for(int i = 0; i < messageValues.length; i++) {
				if(i == 0) {
					sb.append(": ");
				} else {
					sb.append(", ");
				}
				sb.append(messageValues[i]);
			}
			message = sb.toString();
		}
		return message;
	}
	
	/**
	 * Substitute placeholders in the string corresponding to the given messageKey with values in the messageValues-array 
	 * @param messageKey
	 * @param messageValues
	 * @return
	 */
	private String createOneStringMessage(T messageKey, String... messageValues) {
		return createStringMessage (properties, messageKey.toString (), messageValues);
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.etl.log.EventLogger#logEvent(nl.ipo.cds.domain.Job, java.lang.Enum, nl.ipo.cds.domain.LogLevel, double, double, java.lang.String, java.lang.String[])
	 */
	@Override
	public String logEvent(Job job, T messageKey, LogLevel logLevel, double x,
			double y, String gmlId, String... messageValues) {

		String message = createOneStringMessage(messageKey, messageValues);
		Map<String,Object> context = new HashMap<String, Object>();
		context.put("X",x);
		context.put("Y",y);
		context.put("GMLID",gmlId);
		jobLogger.logString(job, messageKey.toString(), logLevel, message, context);
		
		return message;
	}

	@Override
	public String logEvent (final Job job, final T messageKey, final LogLevel logLevel,
			final Map<String, Object> context, final String... messageValues) {
		
		final String message = createOneStringMessage (messageKey, messageValues);
		
		jobLogger.logString (job, messageKey.toString (), logLevel, message, context);
		
		return message;
	}	
}
