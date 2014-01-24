package nl.ipo.cds.etl.reporting;

import java.util.List;
import java.util.Map;

import nl.ipo.cds.domain.JobLog;

/**
 * The writer context serves as the input to the JobFaseLog writer. An implementation of this interface
 * is provided in the form of DefaultLogWriterContext.
 * 
 * The context contains a list of log items that are to be written by the template as well as a generic
 * map of extra parameters templates may refer to.
 * 
 * @author Erik Orbons
 */
public interface LogWriterContext {

	/**
	 * Returns an ordered list of log items.
	 *  
	 * @return A list of log items in the order in which they are to be written to the template.
	 */
	public List<JobLog> getLogItems ();
	
	/**
	 * Returns a map of additional template parameters, such as for example the report title. You
	 * can refer to these parameters from within the template.
	 * 
	 * @return A map of additional template parameters.
	 */
	public Map<String, Object> getParameters ();
}
