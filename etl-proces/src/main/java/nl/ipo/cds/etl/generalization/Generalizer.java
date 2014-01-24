package nl.ipo.cds.etl.generalization;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.vividsolutions.jts.geom.Geometry;

public interface Generalizer extends Runnable {

	void setGeometries(List<Geometry> geometries);
	void setEventQueue(BlockingQueue<Event> eventQueue);
	void setColumnValues(String[] columnValues);
}
