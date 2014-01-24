/**
 * 
 */
package nl.ipo.cds.etl.reporting.geom;

import java.io.File;
import java.util.UUID;

import junit.framework.Assert;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * NOTE: To be able to run this test not from maven, for example from the java-command-line or Eclipse IDE, supply
 * the system property for the directory where the shapefiles must be generated:
 * ie:<code><pre>-DprojectBuildDirectory=${project_loc}\target</pre></code>
 * 
 * @author eshuism
 * 16 mei 2012
 */
@ContextConfiguration({ "classpath:/nl/ipo/cds/dao/dao-applicationContext-test.xml"
					   ,"classpath:/nl/ipo/cds/etl/reporting/geom/geometry-applicationContext.xml"
//					   ,"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml"
					  })
public class ShapeFileGeneratorTest extends AbstractJUnit4SpringContextTests {

	private static final String PROJECT_BUILD_DIRECTORY = "projectBuildDirectory";
	
	@Autowired
	private ShapeFileGenerator shapeFileGenerator;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void before() throws Exception {
		Assert.assertNotNull(this.shapeFileGenerator);
	}

	/**
	 * Test method for {@link nl.ipo.cds.etl.reporting.geom.ShapeFileGenerator#createShapeFile(SimpleFeatureCollection, File)}.
	 */
	@Test
	public void testCreateShapeFile() {
		SimpleFeatureCollection featureCollection = this.createFeatureCollection();
		
		File shapeFile = createFile("geometryErrors.shp");
		this.shapeFileGenerator.createShapeFile(featureCollection, shapeFile);
		Assert.assertTrue(shapeFile.exists());
	}

	/**
	 * Test method for {@link nl.ipo.cds.etl.reporting.geom.ShapeFileGenerator#createZippedShapeFile(SimpleFeatureCollection, File)}.
	 */
	@Test
	public void testCreateZippedShapeFile() {
		SimpleFeatureCollection featureCollection = this.createFeatureCollection();
		
		File shapeFileZip = createFile("geometryErrors.zip");
		this.shapeFileGenerator.createZippedShapeFile(featureCollection, shapeFileZip);
		Assert.assertTrue(shapeFileZip.exists());
	}

	private File createFile(String fileName) {
		String dummySessionId = UUID.randomUUID().toString();
		String projectBuildDirectoryString = System.getProperty(PROJECT_BUILD_DIRECTORY);
		if(projectBuildDirectoryString == null) {
	        throw new IllegalStateException("projectBuildDirectory system property is not set! See the javadoc of this class");
		}
		File projectBuildDirectory = new File(projectBuildDirectoryString);
		File shapeFileDirectory = new File(projectBuildDirectory, dummySessionId);
		boolean createDirSuccess = shapeFileDirectory.mkdir();
		Assert.assertTrue(createDirSuccess);
		File shapeFile = new File(shapeFileDirectory, fileName);
		System.out.println("shape(zip)File: " + shapeFile);
		Assert.assertFalse(shapeFile.exists());
		return shapeFile;
	}

	private SimpleFeatureCollection createFeatureCollection(){
        // We create a FeatureCollection into which we will put each geometryError
        SimpleFeatureCollection featureCollection = FeatureCollections.newCollection();
        
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(FeatureCollectionFactory.FEATURE_TYPE_GEOMETRY_ERROR);

        double longitude = 52.415194;
        double latitude = 6.397194;
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); /* Longitude (= x coord) first ! */
        featureBuilder.add(point);
        featureBuilder.add("DitIsDeGmlId_0");
        featureBuilder.add("DitIsDeErrorMessage");
        featureCollection.add(featureBuilder.buildFeature(null));

        longitude = 51.415194;
        latitude = 6.197194;
        point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); /* Longitude (= x coord) first ! */
        featureBuilder.add(point);
        featureBuilder.add("DitIsDeGmlId_1");
        featureBuilder.add("DitIsDeErrorMessage");
        featureCollection.add(featureBuilder.buildFeature(null));

        // Missing gmlId
        longitude = 50.415194;
        latitude = 6.197194;
        point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); /* Longitude (= x coord) first ! */
        featureBuilder.add(point);
        //featureBuilder.add("DitIsDeGmlId_1");
        featureBuilder.add("DitIsDeErrorMessage");
        featureCollection.add(featureBuilder.buildFeature(null));

        // Missing longitude
        longitude = Double.NaN;
        latitude = 6.197194;
        point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); /* Longitude (= x coord) first ! */
        featureBuilder.add(point);
        featureBuilder.add("DitIsDeGmlId_1");
        featureBuilder.add("DitIsDeErrorMessage");
        featureCollection.add(featureBuilder.buildFeature(null));

        // Missing latitude
        longitude = 49.415194;
        latitude = Double.NaN;
        point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); /* Longitude (= x coord) first ! */
        featureBuilder.add(point);
        featureBuilder.add("DitIsDeGmlId_1");
        featureBuilder.add("DitIsDeErrorMessage");
        featureCollection.add(featureBuilder.buildFeature(null));

        // Missing both longitude and latitude
        longitude = Double.NaN;
        latitude = Double.NaN;
        point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); /* Longitude (= x coord) first ! */
        featureBuilder.add(point);
        featureBuilder.add("DitIsDeGmlId_1");
        featureBuilder.add("DitIsDeErrorMessage");
        featureCollection.add(featureBuilder.buildFeature(null));


        return featureCollection;
	}
}
