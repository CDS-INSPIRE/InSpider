package nl.ipo.cds.executor;

import nl.idgis.commons.jobexecutor.JobExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Explicitly do not catch Exceptions. The Process-es itself are responsible for catching Exceptions
 * and decide if an exception is handable cq recoverable. If not the Process-es propagate RuntimeExceptions
 * which cause the JobExecutor to stop.
 * 
 * @author eshuism
 *
 */
public class CdsJobExecuter {	

	private static final int ADVISORY_LOCK_KEY = 0;

	private static final Log technicalLog = LogFactory.getLog(CdsJobExecuter.class);
	
	private final DataSource dataSource;
	private final JobExecutor jobExecutor;
	
	public CdsJobExecuter (final DataSource dataSource, final JobExecutor jobExecutor) {
		this.dataSource = dataSource;
		this.jobExecutor = jobExecutor;
	}

	public DataSource getDataSource () {
		return dataSource;
	}
	
	public JobExecutor getJobExecutor () {
		return jobExecutor;
	}
	
	@Configuration
	@ComponentScan (basePackageClasses = { nl.ipo.cds.executor.config.Package.class, 
			nl.ipo.cds.metadata.config.Package.class })
	@ImportResource ({
		"/nl/ipo/cds/dao/dataSource-applicationContext.xml",
		"/nl/ipo/cds/dao/dao-applicationContext.xml"
	})
	public static class Config {
		@Bean
		public ConfigDir configDir (final @Value("file:${CONFIGDIR}") String configDirPath) {
			return new ConfigDir (configDirPath);
		}
	}
	
	public static void main(String[] args) {
		try {
			AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext (Config.class);
			
			final CdsJobExecuter jobExecuter = applicationContext.getBean (CdsJobExecuter.class);		
			jobExecuter.run();
			
			applicationContext.close ();
		} catch(Exception e) {
			technicalLog.error("Uncaught exception", e);
			System.exit(1);
		}		
		System.exit(0);
	}
	
	void run() throws InterruptedException, SQLException {
		technicalLog.info("CdsJobExecuter started");
		
		Connection lockConnection = acquireAdvisoryLock();
		
		if(lockConnection == null) {
			technicalLog.info("Couldn't acquire lock (JobExecutor already running?)");				
			return;
		}
	
		jobExecutor.run ();
		
		lockConnection.close();
		
		technicalLog.info("CdsJobExecuter terminated");
	}

	/**
	 * Acquire advisory lock to ensure that only a single instance of
	 * the JobExecutor is operating on the database.
	 * 
	 * The database connection is obtained directly from the connection pool 
	 * (= not using Spring's DataSourceUtils) in order to be able to switch to 
	 * auto commit mode.
	 * 
	 * As this connection is maintained for the entire lifetime of the JobExecutor,
	 * it is imperative that no transaction is started! 
	 * 
	 * @return
	 * @throws SQLException
	 */
	private Connection acquireAdvisoryLock() throws SQLException {		
		Connection connection = dataSource.getConnection(); 
		
		connection.setAutoCommit(true);
	/*
		Statement appNameStmt = connection.createStatement();
		appNameStmt.execute("set application_name = 'JobExecutor (lock)'");
		appNameStmt.close();
		
		PreparedStatement lockStmt = connection.prepareStatement("select pg_try_advisory_lock(?)");
		lockStmt.setLong(1, ADVISORY_LOCK_KEY);
		ResultSet rs = lockStmt.executeQuery();
		boolean result = false;
		while(rs.next()) {
			result = rs.getBoolean(1);			
		}
		
		rs.close();
		lockStmt.close();
		
		if(!result) {
			connection.close();
			connection = null;
		}
		*/
		
		return connection;
	}	
}
