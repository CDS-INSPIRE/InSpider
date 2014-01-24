/**
 * 
 */
package nl.ipo.cds.etl.reporting.geom;

import java.util.Iterator;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.JobLog;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteValidator;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.springframework.util.Assert;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author eshuism
 * 16 mei 2012
 */
public class FeatureCollectionFactory {
	
	private ManagerDao managerDao;

    public final static SimpleFeatureType FEATURE_TYPE_GEOMETRY_ERROR = createFeatureType();

    /**
     * Create featureCollection based on geometry-errors of given job
     * @param job
     * @return
     */
    public SimpleFeatureCollection createFeatureCollection(EtlJob job){
		Assert.notNull(job, "Job cannot be null");

        /* We create a FeatureCollection into which we will put each Feature created from the Job's JobLogs
         */
        SimpleFeatureCollection featureCollection = FeatureCollections.newCollection();

		// Test if there is at least one Geometry error that must be added to the shapeFile
		if(job.getGeometryErrorCount() == 0){
			return featureCollection;
		}

        /*
         * GeometryFactory will be used to create the geometry attribute of each feature (a Point
         * object for the location)
         */
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(FEATURE_TYPE_GEOMETRY_ERROR);

        for (Iterator<JobLog> jobLogIterator = this.managerDao.findJobLog(job).iterator(); jobLogIterator.hasNext();) {
			JobLog jobLog = jobLogIterator.next();

			// Test if this error is an error that must be added to the Shapefile
			if(!isGeometryErrorForShapeFile(jobLog.getKey())){
				continue;
			}
			
			// Get x, y and inspireId from jobLog
	        Double longitude = jobLog.getX();
	        Double latitude = jobLog.getY();
	        
	        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); /* Longitude (= x coord) first ! */
	        featureBuilder.add(point);
	        String gmlId = jobLog.getGmlId();
	        featureBuilder.add(gmlId);
	        featureBuilder.add(jobLog.getMessage());
	        featureCollection.add(featureBuilder.buildFeature(null));

		}

		return featureCollection;
	}
	
    /**
	 * @param key
	 * @return
	 */
	protected boolean isGeometryErrorForShapeFile(String key) {
		boolean geometryErrorForShapeFile = false;
		Assert.notNull(key);
		try {
			ProtectedSiteValidator.MessageKey messageKey = ProtectedSiteValidator.MessageKey.valueOf(key);
			if(messageKey.isAddToShapeFile()){
				geometryErrorForShapeFile = true;
			}
		} catch (Exception e) {
			// Nothing todo
		}
		return geometryErrorForShapeFile;
	}

	/**
     * Create the schema for your FeatureType cq shapefile
     */
    private static SimpleFeatureType createFeatureType() {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("GeometryError");
        
        try {
        	CRSAuthorityFactory authorityFactory = ReferencingFactoryFinder.getCRSAuthorityFactory("epsg", null);
        	builder.setCRS(authorityFactory.createCoordinateReferenceSystem("28992")); // <- Coordinate reference system
        } catch(Exception e) {
        	throw new RuntimeException(e);
        }

        // add attributes
        builder.add("Location", Point.class);
        builder.add("InspireId", String.class);
        builder.add("Message", String.class);

        return builder.buildFeatureType();
    }

	public ManagerDao getManagerDao() {
		return managerDao;
	}

	public void setManagerDao(ManagerDao managerDao) {
		this.managerDao = managerDao;
	}

}
