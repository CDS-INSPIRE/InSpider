package nl.ipo.cds.etl.reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ipo.cds.domain.JobLog;

/**
 * The default implementation of LogWriterContext.
 *
 * @author Erik Orbons
 */
public class DefaultLogWriterContext implements LogWriterContext {

	private List<JobLog> logItems;
	private Map<String, Object> parameters;
	
	public DefaultLogWriterContext (List<JobLog> logItems) {
		this (logItems, new HashMap<String, Object> ());
	}
	
	public DefaultLogWriterContext (List<JobLog> logItems, Map<String, Object> parameters) {
		this.logItems = logItems;
		this.parameters = parameters;
	}
	
	@Override
	public List<JobLog> getLogItems() {
		return logItems;
	}

	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public void set (String key, Object value) {
		parameters.put (key, value);
	}
	
	public void remove (String key) {
		parameters.remove (key);
	}
}
