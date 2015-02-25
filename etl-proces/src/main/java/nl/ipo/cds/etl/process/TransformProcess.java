package nl.ipo.cds.etl.process;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.Process;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TransformJob;
import nl.ipo.cds.etl.Transformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class TransformProcess implements Process<TransformJob>, ApplicationContextAware {
	
	private static final Log log = LogFactory.getLog(TransformProcess.class);

	private ApplicationContext applicationContext;

	private final ManagerDao managerDao;

	public TransformProcess(ManagerDao managerDao) {
		this.managerDao = managerDao;
	}

	@Override
	public boolean process (TransformJob job, final JobLogger logger) throws Exception {
		Map<String, Transformer> transformers = applicationContext.getBeansOfType(Transformer.class);
		log.debug("# of transformers found: " + transformers.size());
		List<String> themeNames = getThemeNamesThatNeedTransformation();
		for(String transformerName : transformers.keySet()) {
			Transformer transformer = transformers.get(transformerName);
			log.debug("starting transformer: " + transformerName);
			transformer.transform(themeNames);
		}
		log.debug("transformation finished");		
		return false;
	}

	private List<String> getThemeNamesThatNeedTransformation () {
		final Set<String> themeNames = new LinkedHashSet<String> ();
		List<Thema> themas = managerDao.getImportedThemasWithoutSubsequentTransform();
		for (Thema thema : themas) {
			log.debug("needs transformation (imported): " + thema); 
			themeNames.add(thema.getNaam());
		}
		themas = managerDao.getRemovedThemasWithoutSubsequentTransform();
		for (Thema thema : themas) {			
			log.debug("needs transformation (removed): " + thema); 
			themeNames.add(thema.getNaam());
		}
		return new ArrayList<String>(themeNames);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public Class<? extends Job> getJobType () {
		return TransformJob.class;
	}
}
