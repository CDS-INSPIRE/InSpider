package nl.ipo.cds.etl.generalization;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import nl.ipo.cds.etl.util.CopyInOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgis.PGgeometry;
import org.postgis.binary.BinaryWriter;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class GeneralizeWriter implements Runnable {
	
	private static final Log logger = LogFactory.getLog(GeneralizeWriter.class);
	
	private NativeJdbcExtractorAdapter nativeJdbcExtractorAdapter;
	private BaseConnection connection;
	private BlockingQueue<Event> eventQueue;
	private String destination;
	private String[] columnNames;
	
	private Semaphore semaphore;
	private WKTWriter wktWriter;
	private BinaryWriter binaryWriter;
	
	public GeneralizeWriter() {
		semaphore = new Semaphore(0);
		
		wktWriter = new WKTWriter();
		binaryWriter = new BinaryWriter();			
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public void setConnection(Connection connection) throws SQLException {
		this.connection = (BaseConnection)nativeJdbcExtractorAdapter.getNativeConnection(connection);
	}
	
	public void setEventQueue(BlockingQueue<Event> eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	public void setNativeJdbcExtractorAdapter(NativeJdbcExtractorAdapter nativeJdbcExtractorAdapter) {
		this.nativeJdbcExtractorAdapter = nativeJdbcExtractorAdapter;
	}
	
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public void run() {
		logger.debug("starting");
				
		try {
			CopyManager copyManager = new CopyManager(connection);
			
			StringBuilder queryBuilder = new StringBuilder("copy ");
			queryBuilder.append(destination);
			queryBuilder.append(" (");
			for(int i = 0; i < columnNames.length; i++) {
				if(i != 0) {
					queryBuilder.append(", ");
				}
				queryBuilder.append(columnNames[i]);
			}
			if(columnNames.length > 0) {
				queryBuilder.append(", ");
			}
			queryBuilder.append("geometry) from stdin csv");
			
			CopyIn copyIn = copyManager.copyIn(queryBuilder.toString());
			
			CopyInOutputStream outputStream = new CopyInOutputStream(copyIn);
			Writer writer = new OutputStreamWriter(outputStream, "utf-8");
			PrintWriter printWriter = new PrintWriter(writer);
			
			logger.debug("database connection established");
			
			long featureCounter = 0;
			for(;;) {
				Event event = eventQueue.take();
				logger.debug("event received");
				if(event instanceof GeometryResult) {
					GeometryResult geometryResult = (GeometryResult)event;
					Geometry geometry = geometryResult.getGeometry();
					String[] columnValues = geometryResult.getColumnValues();
					
					for(int i = 0; i < geometry.getNumGeometries(); i++) {					
						String wkt = wktWriter.write(geometry.getGeometryN(i));
						org.postgis.Geometry pgGeometry = PGgeometry.geomFromString(wkt);
						pgGeometry.setSrid(28992);
						
						featureCounter++;
						
						for(int j = 0; j < columnValues.length; j++) {
							if(j != 0) {
								printWriter.print(',');
							}
							
							String columnValue = columnValues[j];
							if(columnValue != null) {
								printWriter.print('"');
								printWriter.print(columnValue);
								printWriter.print('"');
							}
						}
						
						if(columnValues.length > 0) {
							printWriter.print(',');
						}
						
						printWriter.print('"');
						printWriter.print(binaryWriter.writeHexed(pgGeometry));
						printWriter.println('"');
					}
					
					semaphore.release();
					logger.debug("geometry received (" + semaphore.availablePermits() + ")");
				} else if(event instanceof Finalize) {
					logger.debug("finalize event received");
					break;
				} else {
					logger.debug("Unknown event type: " + event.getClass().getCanonicalName());
				}
			}
			
			printWriter.close();
			
			logger.debug("finished: " + featureCounter + " geometries written");
			semaphore.release();
		} catch(Exception e) {
			logger.debug("couldn't write generalized geometrie", e);
		}
	}

	public void join(int counter) throws InterruptedException {
		logger.debug("join called with counter: " + counter + " available: " + semaphore.availablePermits());
		
		semaphore.acquire(counter);
	}

	public void join() throws InterruptedException {
		join(1);		
	}
}
