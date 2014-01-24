/**
 * 
 */
package nl.ipo.cds.admin.ba.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 * NOTE: To be able to run this test not from maven, for example from the java-command-line or Eclipse IDE, supply
 * the system property for the directory where the doemload-directories must be generated:
 * ie:<code><pre>-Djava.io.tmpdir=${project_loc}\target</pre></code>
 * 
 * @author eshuism
 * 21 mei 2012
 */
@ContextConfiguration({ "classpath:/nl/ipo/cds/dao/dao-applicationContext-test.xml"
	   ,"classpath:/nl/ipo/cds/admin/ba/controller/admin-applicationContext-test.xml"
	   ,"classpath:/nl/ipo/cds/etl/reporting/geom/geometry-applicationContext.xml"
	   ,"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml"
	  })
public class DownloadUtilsTest extends AbstractJUnit4SpringContextTests {

	private String sessionId = ""+UUID.randomUUID();

	/**
	 * The file is not actually created on the file-system
	 */
	@Test
	public void testCreateZipFile(){
		File zipFile = DownloadUtils.createZipFile(this.sessionId, "dataseType", new Timestamp(new Date().getTime()));
		Assert.assertNotNull(zipFile);
	}

	/**
	 * The directory is actually created on the filesystem
	 * @throws IOException 
	 */
	@Test
	public void testcreateAndDeleteZipFileDirectory() throws IOException{
		File zipFile = DownloadUtils.createZipFile(this.sessionId, "dataseType", new Timestamp(new Date().getTime()));
		FileOutputStream fos = new FileOutputStream(zipFile);
		fos.write(100);
		fos.close();
		boolean deleteSuccess = DownloadUtils.deleteDownloadSessionDirectory(sessionId);
		Assert.assertTrue(deleteSuccess);
		Assert.assertFalse(zipFile.getParentFile().exists());
		
	}

//	/**
//	 * The directory is actually created on the filesystem.
//	 * We are expecting that deleteing is not successfull, because we don't close the FileOutputStream
//	 * 
//	 * @throws IOException 
//	 */
//	@Test
//	public void testcreateAndDeleteZipFileDirectoryWithANoneClosedFileInIt() throws IOException{
//		File zipFile = DownloadUtils.createZipFile(this.sessionId, "dataseType", new Timestamp(new Date().getTime()));
//		FileOutputStream fos = new FileOutputStream(zipFile);
//		fos.write(100);
//		// Explicitly do NOT close the FileOutputStream
//		boolean deleteSuccess = DownloadUtils.deleteDownloadSessionDirectory(sessionId);
//		Assert.assertFalse(deleteSuccess);
//		Assert.assertTrue(zipFile.getParentFile().exists());
//
//		fos.close();
//	}

}
