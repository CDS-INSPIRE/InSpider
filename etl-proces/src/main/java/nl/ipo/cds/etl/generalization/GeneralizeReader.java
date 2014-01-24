package nl.ipo.cds.etl.generalization;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.WKBReader;

public class GeneralizeReader implements ApplicationContextAware {
	
	private static final Log logger = LogFactory.getLog(GeneralizeReader.class);
	
	private ApplicationContext applicationContext;	

	private DataSource dataSource;
 
	private Executor executer;
	private GeneralizerConfig generalizerConfig;
		
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setGeneralizerConfig(GeneralizerConfig generalizerConfig) {
		this.generalizerConfig = generalizerConfig;
	}	
	
	public void setExecuter(Executor executer) {
		this.executer = executer;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	protected <T> boolean arrayEquals(T[] a, T[] b) {
		for(int i = 0; i < a.length; i++) {
			if(a[i] == null && b[i] == null) {
				continue;
			}
			
			if(a[i] != null && b[i] != null && a[i].equals(b[i])) {
				continue;
			}
			
			return false;
		}
		
		return true;
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	public void delete() throws SQLException, IOException {
		Connection connection = DataSourceUtils.getConnection(dataSource);
		
		Statement stmt = connection.createStatement();
		for(GeneralizeJob job : generalizerConfig.getGeneralizeJobs()) {
			String destination = job.getDestination();
			
			logger.debug("" + stmt.executeUpdate("delete from " + destination) + 
				" features deleted from " + destination);
		}
		stmt.close();
		
		DataSourceUtils.releaseConnection(connection, dataSource);
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	public void populate() throws SQLException, IOException, InterruptedException {
		logger.debug("started");
		
		Connection connection = DataSourceUtils.getConnection(dataSource);
		
		WKBReader wkbReader = new WKBReader();
		
		Statement stmt = connection.createStatement();
		for(GeneralizeJob job : generalizerConfig.getGeneralizeJobs()) {
			String destination = job.getDestination();
			
			BlockingQueue<Event> eventQueue = new SynchronousQueue<Event>();
			
			ResultSet rs = stmt.executeQuery(job.getQuery());
			ResultSetMetaData rsMd = rs.getMetaData();
			
			String[] columnNames = new String[rsMd.getColumnCount() - 3];
			for(int i = 0; i < columnNames.length; i++) {
				String columnValue = rsMd.getColumnName(i + 4);
				columnNames[i] = columnValue;
			}
			
			GeneralizeWriter writer = applicationContext.getBean(GeneralizeWriter.class);
			writer.setDestination(destination);
			writer.setEventQueue(eventQueue);	
			writer.setConnection(connection);
			writer.setColumnNames(columnNames);
			
			executer.execute(writer);
			
			int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE;
			String[] lastColumnValues = new String[rsMd.getColumnCount() - 3];
			ArrayList<Geometry> geometries = new ArrayList<Geometry>();
			int counter = 0;
			
			try {
				while(rs.next()) {
					Geometry geometry = wkbReader.read(new InputStreamInStream(rs.getBinaryStream(1)));
					int x = rs.getInt(2), y = rs.getInt(3);
					
					String[] columnValues = new String[rsMd.getColumnCount() - 3];
					for(int i = 0; i < columnValues.length; i++) {
						String columnValue = rs.getString(i + 4);
						columnValues[i] = columnValue;
					}
					
					if(x != lastX || y != lastY || !arrayEquals(columnValues, lastColumnValues)) {
						lastX = x;
						lastY = y;					
						lastColumnValues = columnValues;
						
						if(geometries.size() > 0) {					
							Generalizer generalizer = applicationContext.getBean(Generalizer.class);
							generalizer.setGeometries(geometries);
							generalizer.setEventQueue(eventQueue);
							generalizer.setColumnValues(columnValues);
							
							executer.execute(generalizer);
							logger.debug("generalizer constructed");
							
							geometries = new ArrayList<Geometry>();
							counter++;
						}
					}
					
					geometries.add(geometry);
				}
				
				rs.close();			
			} catch(Exception e) {
				logger.debug("exception during generalization", e);
				throw new IOException("exception during generalization", e);
			} finally {
				writer.join(counter);
				eventQueue.put(new Finalize());
				writer.join();
			}
		}
		
		stmt.close();
		
		DataSourceUtils.releaseConnection(connection, dataSource);		
		
		logger.debug("finished");
	}
}
