package nl.ipo.cds.admin.ba.controller.beans;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;

@JsonSerialize (include = Inclusion.ALWAYS)
public class PreviewLogLineResponse {

	private final LogLevel logLevel;
	private final String message;
	private final String attribute;
	
	public PreviewLogLineResponse (final LogLevel logLevel, final String message, final String attribute) {
		this.logLevel = logLevel;
		this.message = message;
		this.attribute = attribute;
	}

	public LogLevel getLogLevel () {
		return logLevel;
	}
	
	public String getMessage() {
		return message;
	}

	public String getAttribute() {
		return attribute;
	}
}
