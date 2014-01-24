/**
 * 
 */
package nl.ipo.cds.etl.reporting.geom;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.domain.JobLog;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteValidator;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * @author eshuism
 * 22 mei 2012
 */
@ContextConfiguration({ "classpath:/nl/ipo/cds/dao/dao-applicationContext-test.xml"
	   ,"classpath:/nl/ipo/cds/etl/reporting/geom/geometry-applicationContext.xml"
//	   ,"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml"
	  })
public class FeatureCollectionFactoryTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private ManagerDao managerDao;
	
	@Autowired
	private FeatureCollectionFactory featureCollectionFactory;
	
	@Before
	public void before(){
		Assert.assertNotNull(managerDao);
		Assert.assertNotNull(featureCollectionFactory);
		
		// Reset mocks between tests
		reset(this.managerDao);
	}

	/**
	 * Test method for {@link nl.ipo.cds.etl.reporting.geom.FeatureCollectionFactory#createFeatureCollection(nl.ipo.cds.domain.CdsJob)}.
	 */
	@Test
	public void testCreateFeatureCollection() {

		ImportJob job = new ImportJob();
		job.setId(100L);
		job.setFinishTime(new Timestamp(new Date().getTime()));
		DatasetType datasetType = new DatasetType();
		datasetType.setNaam("IMPORT");
		job.setDatasetType(datasetType);
		job.setGeometryErrorCount(2);

		final ImportJob importJob = new ImportJob ();
		importJob.setId (job.getId ());
		importJob.setFinishTime (job.getFinishTime ());
		importJob.setDatasetType (job.getDatasetType ());
		importJob.setGeometryErrorCount (job.getGeometryErrorCount ());
		
		// List with all joblogs
		List<JobLog> jobLogs = new ArrayList<JobLog>();
		// Create JobLog which is an geometry-error that should be added to the featureCollection/shapefile
		JobLog jobLog = new JobLog();
		jobLog.setId(1L);
		jobLog.setX(new Double(0));
		jobLog.setY(new Double(12.12));
		jobLog.setGmlId("theGmlId");
		jobLog.setMessage("Dit is een geometrie-foutmelding");
		ProtectedSiteValidator.MessageKey messageKey = ProtectedSiteValidator.MessageKey.GEOMETRY_INTERIOR_RINGS_INTERSECT;
		Assert.assertTrue(messageKey.isAddToShapeFile());
		jobLog.setKey(messageKey.name());
		jobLog.setJob(job);
		jobLogs.add(jobLog);

		// Create JobLog which is a geometry-error that should be added to the featureCollection/shapefile
		jobLog = new JobLog();
		jobLog.setId(2L);
		jobLog.setMessage("Dit is een geometrie-foutmelding die niet aan de shape-file toegevoegd moet worden");
		messageKey = ProtectedSiteValidator.MessageKey.GEOMETRY_SRS_NULL;
		Assert.assertFalse(messageKey.isAddToShapeFile());
		jobLog.setKey(messageKey.name());
		jobLog.setJob(job);
		jobLogs.add(jobLog);

		// Create JobLog which is not a geometry-error at all
		jobLog = new JobLog();
		jobLog.setId(3L);
		jobLog.setMessage("Dit is een geometrie-foutmelding die niet aan de shape-file toegevoegd moet worden");
		jobLog.setKey("ONBEKENDE_MESSAGE_KEY");
		jobLog.setJob(job);
		jobLogs.add(jobLog);

		expect(this.managerDao.findJobLog(job)).andReturn(jobLogs);

		// Stop recording, start playing
		replay(this.managerDao);

		SimpleFeatureCollection featureCollection = featureCollectionFactory.createFeatureCollection(importJob);
		
		// So only one out of three jobLogs should be in the featureCollection
		Assert.assertEquals(1, featureCollection.size());
		
		// Verify the expected behaviour occurred
		verify(this.managerDao);
	}

	/**
	 * Test method for {@link nl.ipo.cds.etl.reporting.geom.FeatureCollectionFactory#createFeatureCollection(nl.ipo.cds.domain.CdsJob)}.
	 */
	@Test
	public void testCreateFeatureCollectionMissingValues() {

		ImportJob job = new ImportJob();
		job.setId(100L);
		job.setFinishTime(new Timestamp(new Date().getTime()));
		DatasetType datasetType = new DatasetType();
		datasetType.setNaam("IMPORT");
		job.setDatasetType(datasetType);
		job.setGeometryErrorCount(2);

		final ImportJob importJob = new ImportJob ();
		importJob.setId (job.getId ());
		importJob.setFinishTime (job.getFinishTime ());
		importJob.setDatasetType (job.getDatasetType ());
		importJob.setGeometryErrorCount (job.getGeometryErrorCount ());
		
		// List with all joblogs
		List<JobLog> jobLogs = new ArrayList<JobLog>();
		// Create JobLog which is an geometry-error that should be added to the featureCollection/shapefile
		JobLog jobLog = new JobLog();
		jobLog.setId(1L);
		jobLog.setX(new Double(0));
		jobLog.setY(new Double(12.12));
		jobLog.setGmlId("theGmlId");
		jobLog.setMessage("Dit is een geometrie-foutmelding");
		ProtectedSiteValidator.MessageKey messageKey = ProtectedSiteValidator.MessageKey.GEOMETRY_INTERIOR_RINGS_INTERSECT;
		Assert.assertTrue(messageKey.isAddToShapeFile());
		jobLog.setKey(messageKey.name());
		jobLog.setJob(job);
		jobLogs.add(jobLog);

		// Create JobLog which is an geometry-error that should be added, but misses gemlId
		jobLog = new JobLog();
		jobLog.setId(2L);
		jobLog.setX(new Double(0));
		jobLog.setY(new Double(12.12));
		// Do NOT set the gmlId: jobLog.setGmlId("theGmlId");
		jobLog.setMessage("Dit is een geometrie-foutmelding");
		messageKey = ProtectedSiteValidator.MessageKey.GEOMETRY_INTERIOR_RINGS_INTERSECT;
		Assert.assertTrue(messageKey.isAddToShapeFile());
		jobLog.setKey(messageKey.name());
		jobLog.setJob(job);
		jobLogs.add(jobLog);

		// Create JobLog which is an geometry-error that should be added, but misses X and Y
		jobLog = new JobLog();
		jobLog.setId(2L);
		jobLog.setX(Double.NaN);
		jobLog.setY(Double.NaN);
		jobLog.setGmlId("theGmlId");
		jobLog.setMessage("Dit is een geometrie-foutmelding");
		messageKey = ProtectedSiteValidator.MessageKey.GEOMETRY_INTERIOR_RINGS_INTERSECT;
		Assert.assertTrue(messageKey.isAddToShapeFile());
		jobLog.setKey(messageKey.name());
		jobLog.setJob(job);
		jobLogs.add(jobLog);

		expect(this.managerDao.findJobLog(job)).andReturn(jobLogs);

		// Stop recording, start playing
		replay(this.managerDao);

		SimpleFeatureCollection featureCollection = featureCollectionFactory.createFeatureCollection(importJob);
		
		// So only one out of three jobLogs should be in the featureCollection
		Assert.assertEquals(3, featureCollection.size());
		
		// Verify the expected behaviour occurred
		verify(this.managerDao);
	}

}
