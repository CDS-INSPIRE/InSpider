package nl.ipo.cds.etl.generalization;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class DefaultGeneralizer implements Generalizer {
	
	private final static int DISTANCE = 30;
	
	private static final Log logger = LogFactory.getLog(DefaultGeneralizer.class);
	
	private GeometryFactory geometryFactory;
	private BlockingQueue<Event> eventQueue;
	
	private List<Geometry> geometries;
	private String[] columnValues;	
	
	public DefaultGeneralizer() {
		geometryFactory = new GeometryFactory();
	}
	

	@Override
	public void setGeometries(List<Geometry> geometries) {
		this.geometries = geometries;		
	}

	@Override
	public void setEventQueue(BlockingQueue<Event> eventQueue) {
		this.eventQueue = eventQueue;		
	}
	
	@Override
	public void setColumnValues(String[] columnValues) {
		this.columnValues = columnValues;
	}

	@Override
	public void run() {
		logger.debug("starting");
		
		Geometry[] geometriesArray = new Geometry[geometries.size()];
		for(int i = 0; i < geometriesArray.length; i++) {
			Geometry geometry = geometries.get(i);
			geometriesArray[i] = geometry.buffer(DISTANCE);
		}
		
		GeometryCollection collection = geometryFactory.createGeometryCollection(geometriesArray);
		Geometry union = TopologyPreservingSimplifier.simplify(collection.union(), DISTANCE);
		
		logger.debug("geometry offered");
		try {
			eventQueue.put(new GeometryResult(union, columnValues));
		} catch(InterruptedException e) {
			logger.debug(e);
		}
		logger.debug("finished");
	}
}
