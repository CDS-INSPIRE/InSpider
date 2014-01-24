/**
 * 
 */
package nl.ipo.cds.admin.ba.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import nl.ipo.cds.admin.ba.util.DownloadUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author eshuism
 * 22 mei 2012
 */
public class CleanupListener implements HttpSessionListener, ApplicationListener<ContextRefreshedEvent> {

	private static final Log logger = LogFactory.getLog(CleanupListener.class);
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		// Nothing to do
		logger.debug("session created with id: " + se.getSession().getId());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		/* Clean up zip-files that are downloaded during this session
		 */
		String sessionId = se.getSession().getId();
		logger.debug("session destroyed with id: " + sessionId);

		// The directory to be deleted
		boolean deleteSuccess = DownloadUtils.deleteDownloadSessionDirectory(sessionId);
		if(!deleteSuccess){
			logger.warn("Not be able to delete download-directory for session with id: " + sessionId);
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		logger.debug("ApplicationContext started: " + event.getApplicationContext().getDisplayName());
		
		/* Delete the download directory at application startup
		 * It still could have files in it after a crash of the JVM cq Tomcat
		 */
		boolean deleteSuccess = DownloadUtils.deleteDownloadDirectory();
		if(!deleteSuccess){
			logger.warn("Not be able to delete download-directory at application startup.");
		}
	}
}
