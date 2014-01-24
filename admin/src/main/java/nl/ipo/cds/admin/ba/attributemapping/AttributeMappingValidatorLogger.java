package nl.ipo.cds.admin.ba.attributemapping;

import java.util.Map;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.etl.attributemapping.AttributeMappingValidator;
import nl.ipo.cds.etl.attributemapping.AttributeMappingValidator.MessageKey;
import nl.ipo.cds.etl.log.EventLogger;

public class AttributeMappingValidatorLogger implements EventLogger<AttributeMappingValidator.MessageKey> {
	@Override
	public String logEvent (final Job job, final MessageKey messageKey, final LogLevel logLevel, final String... messageValues) {
		return "";
	}

	@Override
	public String logEvent (final Job job, final MessageKey messageKey, final LogLevel logLevel, final double x, 
			final double y, final String gmlId, final String... messageValues) {
		return logEvent (job, messageKey, logLevel, messageValues);
	}

	@Override
	public String logEvent(final Job job, MessageKey messageKey, final LogLevel logLevel, final Map<String, Object> context, final String... messageValues) {
		return logEvent (job, messageKey, logLevel, messageValues);
	}
}