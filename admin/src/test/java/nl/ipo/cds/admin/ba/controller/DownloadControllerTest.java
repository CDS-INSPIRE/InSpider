/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;
import nl.ipo.cds.admin.ba.util.DownloadUtils;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.JobLog;
import nl.ipo.cds.domain.ValidateJob;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteValidator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 * @author eshuism
 * 21 mei 2012
 */
@ContextConfiguration({ "classpath:/nl/ipo/cds/dao/dao-applicationContext-test.xml"
	   ,"classpath:/nl/ipo/cds/admin/ba/controller/admin-applicationContext-test.xml"
	   ,"classpath:/nl/ipo/cds/etl/reporting/geom/geometry-applicationContext.xml"
	   ,"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml"
	  })
public class DownloadControllerTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private DownloadController downloadController;
	
	@Autowired
	private ManagerDao managerDao;
	
	@Before
	public void before(){
		Assert.assertNotNull(downloadController);
		Assert.assertNotNull(managerDao);
		
		// Reset mocks between tests
		reset(this.managerDao);

	}
	
	@Test
	public void testCreateZipFile(){
		File zipFile = DownloadUtils.createZipFile(""+UUID.randomUUID(), "dataseType", new Timestamp(new Date().getTime()));
		
		Assert.assertNotNull(zipFile);
	}
	
	@Test
	public void testDownLoadShapeFile(){
		
		// Create Job
		ValidateJob job = new ValidateJob();
		job.setId(100L);
		job.setFinishTime(new Timestamp(new Date().getTime()));
		DatasetType datasetType = new DatasetType();
		datasetType.setNaam("testDatasetType");
		job.setDatasetType(datasetType);
		job.setGeometryErrorCount(2);
		// Create JobLog
		JobLog jobLog = new JobLog();
		jobLog.setId(1L);
		jobLog.setMessage("Dit is een geometrie-foutmelding");
		jobLog.setKey(ProtectedSiteValidator.MessageKey.GEOMETRY_INTERIOR_RINGS_INTERSECT.name());
		jobLog.setX(new Double(0));
		jobLog.setY(new Double(12.12));
		jobLog.setGmlId("theGmlId");
		jobLog.setJob(job);
		List<JobLog> jobLogs = new ArrayList<JobLog>();
		jobLogs.add(jobLog);

		expect(this.managerDao.getJob(job.getId())).andReturn(job);
		expect(this.managerDao.findJobLog(job)).andReturn(jobLogs);

		// Stop recording, start playing
		replay(this.managerDao);

		// MockHttpServlet classes
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
		downloadController.downLoadShapeFile(job.getId(), httpServletRequest, httpServletResponse);
		
		// Verify the expected behaviour occurred
		verify(this.managerDao);

		
	}
	
}
