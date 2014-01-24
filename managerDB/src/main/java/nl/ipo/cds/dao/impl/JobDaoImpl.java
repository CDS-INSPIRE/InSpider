/**
 * 
 */
package nl.ipo.cds.dao.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobCreator;
import nl.idgis.commons.jobexecutor.JobDao;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.JobLog;
import nl.ipo.cds.utils.DateTimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rob
 *
 */
public class JobDaoImpl implements JobDao, JobCreator {
	private final ManagerDao managerDao;
	private EntityManager entityManager;
	private static final Log logger = LogFactory.getLog(JobDaoImpl.class); // developer log
	
	public JobDaoImpl (final ManagerDao managerDao) {
		this.managerDao = managerDao;
	}

	public ManagerDao getManagerDao () {
		return managerDao;
	}

	@PersistenceContext(unitName = "cds")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public EntityManager getEntityManager () {
		return entityManager;
	}

	@Override
	@Transactional (propagation=Propagation.REQUIRED)
	public void create (Job job) {
		// Set the create time:
		job.setCreateTime(DateTimeUtils.now());
		
		logger.debug("\t### before create: job.id: " + job.getId());
		managerDao.create (toAbstractJob (job));
		logger.debug("\t### after create:  job.id: " + job.getId());
	}

	@Override
	public Job getJob (final Long pk) {
		return managerDao.getJob (pk);
	}

	public AbstractJob getJob (Job job) {
		return managerDao.getJob (job.getId());
	}

	@Override
	@Transactional (propagation = Propagation.REQUIRED) 
	public void update (Job job) {
		managerDao.update (toAbstractJob (job));
	}

	@Override
	@Transactional (propagation = Propagation.REQUIRED)
	public void delete (Job job) {
		managerDao.delete (toAbstractJob (job));
	}

	@Override
	@Transactional (propagation = Propagation.REQUIRES_NEW)
//	@Transactional (propagation = Propagation.REQUIRED) // hangs when called during psql copy statement
	public void putLogItem (Job job, final String msg, final String key, final LogLevel level, final String context) {		
		JobLog logItem = createLogItem(job, msg, key, level, context);
		managerDao.create (logItem);
	}

	public JobLog createLogItem (Job job, final String msg, final String key, final LogLevel level, final String context) {
		final AbstractJob cdsJob = toAbstractJob (getJob(job));
		
		final JobLog logItem = new JobLog ();
		
		logItem.setJob (cdsJob);
		logItem.setMessage (msg);
		logItem.setKey (key);
		logItem.setLogLevel (level);
		logItem.setTime (DateTimeUtils.now ());
		logItem.setContext (context);
		
		if (context == null || context.isEmpty()) {
			logItem.setGmlId(null);
			logItem.setX(null);
			logItem.setY(null);
		} else {
			// split the context string in x, y, gmlId values
			try {
				final ObjectMapper mapper = new ObjectMapper();
				final StringReader reader = new StringReader(context);
				@SuppressWarnings("unchecked")
				final Map<String, Object> contextMap = mapper.readValue(reader,
						HashMap.class);
				Object gmlId = contextMap.get("GMLID");
				if (gmlId==null){
					logItem.setGmlId(null);
				}else{
					logItem.setGmlId(gmlId.toString());
				}
				logItem.setX((Double) contextMap.get("X"));
				logItem.setY((Double) contextMap.get("Y"));
			} catch (JsonParseException e1) {
				e1.printStackTrace();
			} catch (JsonMappingException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return logItem;
	}
	
	private AbstractJob toAbstractJob (final Job job) {
		if (job instanceof AbstractJob) {
			return (AbstractJob)job;
		}
		
		throw new IllegalArgumentException (String.format ("Job "+job.getClass().getCanonicalName()+"must be an instance of %s", AbstractJob.class.getCanonicalName ()));
	}

	@Override
	public void putJob (final Job job) {
		create (job);
	}
}
