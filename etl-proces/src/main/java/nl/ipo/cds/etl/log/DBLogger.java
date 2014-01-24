package nl.ipo.cds.etl.log;

import java.io.IOException;
import java.util.Map;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobDao;
import nl.idgis.commons.jobexecutor.JobLogger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class DBLogger implements JobLogger {
	
	private static final Log technicalLog = LogFactory.getLog(DBLogger.class);
	
	private final JobDao jobDao;
	
	public DBLogger (final JobDao jobDao) {
		this.jobDao = jobDao;
	}
	
	@Override
	public void logString(Job job, String key, LogLevel logLevel, String message) {
		jobDao.putLogItem (job, message, key, logLevel, null);
		technicalLog.debug (String.format ("%s (%s)", message, key));
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.etl.log.StringLogger#logString(nl.ipo.cds.domain.Job, java.lang.String, nl.ipo.cds.domain.LogLevel, java.lang.String, double, double, String)
	 */
	@Override
	public void logString(Job job, String key, LogLevel logLevel,
			String message,  Map<String,Object> context) {
		final ObjectMapper mapper = new ObjectMapper ();
		String contextStr;
		try {
			contextStr = mapper.writeValueAsString(context);
		} catch (JsonGenerationException e) {
			contextStr = e.getMessage();
			e.printStackTrace();
		} catch (JsonMappingException e) {
			contextStr = e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			contextStr = e.getMessage();
			e.printStackTrace();
		}
		jobDao.putLogItem (job, message, key, logLevel, contextStr);
//		jobDao.putLogItem (job, message, key, logLevel, String.format ("x=%f;y=%f;gmlId=%s", x, y, gmlId));
		technicalLog.debug (String.format ("Msg=%s (Key=%s): Ctx=%s", message, key, contextStr));
	}
	
}
