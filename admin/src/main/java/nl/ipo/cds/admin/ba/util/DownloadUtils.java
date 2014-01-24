/**
 * 
 */
package nl.ipo.cds.admin.ba.util;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.Assert;

/**
 * @author eshuism
 * 22 mei 2012
 */
public class DownloadUtils {

	private static final String SYSTEM_PROPERTY_TEMP_DIR = "java.io.tmpdir";

	private static final Log logger = LogFactory.getLog(DownloadUtils.class);

	public static boolean deleteDownloadSessionDirectory(String dirName){
		boolean deleteSuccess = true;
		File sessionDownloadDir = getDownLoadSessionDirectory(dirName);
		if(sessionDownloadDir.exists()){
			try {
				FileUtils.deleteDirectory(sessionDownloadDir);
			} catch (IOException ioe) {
				logger.warn("Not be able to delete directory: " + sessionDownloadDir.getAbsolutePath(), ioe);
				deleteSuccess = false;
			}
		}		
		return deleteSuccess;
	}
	
	public static boolean deleteDownloadDirectory(){
		boolean deleteSuccess = true;
		File downloadDir = getDownloadDirectory();
		if(downloadDir.exists()){
			try {
				FileUtils.deleteDirectory(downloadDir);
			} catch (IOException ioe) {
				logger.warn("Not be able to delete directory: " + downloadDir.getAbsolutePath(), ioe);
				deleteSuccess = false;
			}
		}		
		return deleteSuccess;
	}

	/**
	 * @param eindTijd 
	 * @param string
	 * @return
	 */
	protected static File getDownLoadSessionDirectory(String dirName) {
		File downloadDir = DownloadUtils.getDownloadDirectory();
		File sessionDownloadDir = new File(downloadDir, dirName);
		sessionDownloadDir.deleteOnExit();
		return sessionDownloadDir;
	}
	
	public static File createZipFile(String dirName, String datasetType, Timestamp eindTijd) {
		Assert.notNull(dirName, "Not be able to create a shape-zip-file when dirName = null");
		Assert.notNull(dirName, "Not be able to create a shape-zip-file when datasetType = null");
		Assert.notNull(dirName, "Not be able to create a shape-zip-file when eindtijd = null");
		
		DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("hh-mm-ss-SSS");
		DateTime formattedDate = new DateTime(eindTijd.getTime());
		String datumString = dateFormatter.print(formattedDate);
		String tijdString = timeFormatter.print(formattedDate);
	
		String fileIdentifier = datasetType + "_" + datumString + "T" + tijdString;

		File downloadSessionDirectory = getDownLoadSessionDirectory(dirName);
		File zipFileDirectory = makeZipfileDirectory(downloadSessionDirectory, fileIdentifier);

		String fileName = fileIdentifier + ".zip";
		File zipFile = new File(zipFileDirectory, fileName);
		zipFile.deleteOnExit();
		logger.debug("Creating geometry-errors shape(zip)File: " + zipFile);
		return zipFile;
	}

	/**
	 * Actually create the dirs on file-system
	 * @param downloadSessionDirectory 
	 * @param dirName
	 * @return
	 */
	protected static File makeZipfileDirectory(File downloadSessionDirectory, String dirName) {
		File zipfileDirectory = new File(downloadSessionDirectory, dirName);
		// Delete after JVM shuts down
		zipfileDirectory.deleteOnExit();
		/* If directory already exists, do not create it again
		 * It's standard behavior to have more than one shape-file: for each job one 
		 */
		if(!zipfileDirectory.exists()){
			boolean createDirSuccess = zipfileDirectory.mkdirs();
			Assert.isTrue(createDirSuccess, "Not be able to create directory " + zipfileDirectory.getAbsolutePath());
		}
		return zipfileDirectory;
	}

	public static File getDownloadDirectory() {
		String tempDirString = System.getProperty(SYSTEM_PROPERTY_TEMP_DIR);
		if(tempDirString == null) {
	        throw new IllegalStateException(SYSTEM_PROPERTY_TEMP_DIR + " system property is not set!");
		}
		File tempDir = new File(tempDirString);
		File downloadDir = new File(tempDir, "CDS-INSPIRE");
		// Delete after JVM shuts down
		downloadDir.deleteOnExit();
		downloadDir = new File(downloadDir, "downloads");
		downloadDir.deleteOnExit();

		return downloadDir;
	}

}
