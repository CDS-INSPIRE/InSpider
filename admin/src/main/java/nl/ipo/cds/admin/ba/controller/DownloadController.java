/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.ipo.cds.admin.ba.util.DownloadUtils;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.reporting.geom.FeatureCollectionFactory;
import nl.ipo.cds.etl.reporting.geom.ShapeFileGenerator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 * @author eshuism
 * 21 mei 2012
 */
@Controller
public class DownloadController {

	private static final Log logger = LogFactory.getLog(DownloadController.class);

	@Autowired
	private ManagerDao managerDao;

	@Autowired
	private ShapeFileGenerator shapeFileGenerator;

	@Autowired
	private FeatureCollectionFactory featureCollectionFactory;

	@ModelAttribute("roleFunction")
	String getRoleFunction(){
		return "beheerder";
	}

	/**
	 * Download geometryErrors in a shapefile
	 */
	@RequestMapping(value = "/ba/download/shapefile/jobs/{jobId}", method = RequestMethod.GET)
	public void downLoadShapeFile (@PathVariable long jobId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		AbstractJob abstractJob = this.managerDao.getJob(jobId);
		Assert.notNull(abstractJob, "job with id " + jobId + " could not be found");
		Assert.isTrue (abstractJob instanceof EtlJob, String.format ("job with id %d must be an ETL job", jobId));
		EtlJob job = (EtlJob)abstractJob;
		
		HttpSession	session = httpServletRequest.getSession();
		String sessionId = session.getId();

		// If job has no endTime; it's not applicable to create a shapeFile with geometry-errors
		Assert.notNull(job.getFinishTime(), "The job has no endTime; it's not applicable to create a shapeFile with geometry-errors");
		File zipFile = DownloadUtils.createZipFile(sessionId, job.getDatasetType().getNaam(), job.getFinishTime());
		
		if(!zipFile.exists()){
			// Create FeatureCollection
			SimpleFeatureCollection featureCollection = featureCollectionFactory.createFeatureCollection(job);
			// Create shape file
			shapeFileGenerator.createZippedShapeFile(featureCollection, zipFile);
		}
		
		Assert.isTrue(zipFile.exists());
	
		FileInputStream zipFileInputStream = null;
	    try {
			// Set contenttype
			httpServletResponse.setContentType("application/zip");      
			httpServletResponse.setHeader("Content-Disposition", "attachment; filename="+zipFile.getName()); 

			// get your file as InputStream
			zipFileInputStream = new FileInputStream(zipFile);
			// copy it to response's OutputStream
			IOUtils.copy(zipFileInputStream, httpServletResponse.getOutputStream());
			httpServletResponse.flushBuffer();
	    } catch (IOException ioe) {
	    	throw new RuntimeException("IOError writing file to output stream");
	    } finally {
	    	if(zipFileInputStream != null){
	    		try {
					zipFileInputStream.close();
				} catch (IOException ioe) {
					logger.warn("Not be able to close FileInputStream", ioe);
				}
	    	}
	    }
	}

	public ManagerDao getManagerDao() {
		return managerDao;
	}

	public void setManagerDao(ManagerDao managerDao) {
		this.managerDao = managerDao;
	}

	public ShapeFileGenerator getShapeFileGenerator() {
		return shapeFileGenerator;
	}

	public void setShapeFileGenerator(ShapeFileGenerator shapeFileGenerator) {
		this.shapeFileGenerator = shapeFileGenerator;
	}

	public FeatureCollectionFactory getFeatureCollectionFactory() {
		return featureCollectionFactory;
	}

	public void setFeatureCollectionFactory(
			FeatureCollectionFactory featureCollectionFactory) {
		this.featureCollectionFactory = featureCollectionFactory;
	}

}
