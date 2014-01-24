package nl.ipo.cds.etl.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;

public class ListStringLogger implements JobLogger {

	private List<String> log;

	@Override
	public void logString(Job job, String key, LogLevel logLevel, String message) {
		log.add(message);
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.etl.log.StringLogger#logString(nl.ipo.cds.domain.Job, java.lang.String, nl.ipo.cds.domain.LogLevel, java.lang.String, double, double, long)
	 */
	@Override
	public void logString(Job job, String key, LogLevel logLevel,
				String message, Map<String, Object> context) {
		log.add(message);
	}

	public List<String> getLog(){
		return this.log;
	}
	
	public void reset(){
		this.log = new ArrayList<String>(); 
	}

}
