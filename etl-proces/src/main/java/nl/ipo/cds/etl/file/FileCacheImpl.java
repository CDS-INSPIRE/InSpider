/**
 * 
 */
package nl.ipo.cds.etl.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.FileCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.xml.XMLAdapter;
import org.springframework.core.io.FileSystemResource;

/**
 * @author Rob
 *
 */
public class FileCacheImpl implements FileCache {
	private static final Log technicalLog = LogFactory.getLog(FileCacheImpl.class);

	private String cdsFileCacheRoot = "/tmp";

	protected File makeFile(EtlJob job) {
		return makeFile(job, true);	
	}	
	
	protected File getFile(EtlJob job) {
		return makeFile(job, false);	
	}

	public String getFilename(EtlJob job) {
		return job.getDatasetType().getNaam()+".xml";
	}

	private String getFiledirUuid(EtlJob job) {
		//get root dir
		String dirname = cdsFileCacheRoot;
		// get bronhouder id
		Bronhouder b = job.getBronhouder();
		dirname = dirname  + File.separator + b.getCode();
		// get uuid
		dirname = dirname + File.separator + getIdentifier (job);
		return dirname;
	}
	
	public String getFiledir(EtlJob job) {
		String dirname = getFiledirUuid(job);
		// get job id
		dirname = dirname + File.separator + job.getId();
		return dirname;
	}
	
	protected File makeFile(EtlJob job, boolean mkDir) {
		File file = null;
		String dirPath = getFiledir(job);
		File dir =  new File(dirPath);
		boolean newFile = true;
		if (mkDir){
			boolean dirCreated = dir.mkdirs();
			newFile = dirCreated || dir.exists();
		}
		if (newFile){
			String filename = getFilename(job);
			String filePath = dirPath + File.separator + filename;
			file = new File(filePath);
			technicalLog.debug("File: " + file.getAbsolutePath());
		}
		return file; 
	}
	
	/**
	 * Store the content of an OMElement to file cache.
	 * @param job
	 * @param xmlStream
	 */
	@Override
	public URL storeToCache(EtlJob job, XMLStreamReader xmlStream) {
		// prepare FileCache
		File file = this.makeFile(job);
		URL cacheURL = null;
		if (file!=null){
			try {
				cacheURL = new FileSystemResource(file).getURL();
				OutputStream os = new FileOutputStream(file);
				XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
				XMLAdapter.writeElement(xmlWriter, xmlStream);
				xmlWriter.close();
				os.close();
				technicalLog.debug(" - WFS getfeature result written to " + file.getAbsolutePath());
			} catch (Exception e) {
				// Catch for adding context info to the error message
				throw new RuntimeException("Not be able to write features to the filecache. Directory: "+ file.getAbsolutePath() + ". Cause: " + e.getMessage(), e);
			}
		} else {
			throw new RuntimeException("Filecache directory structure: \"" + this.getFiledir(job) + "\" not available.");
		}
		
		return cacheURL;
	}

	@Override
	public boolean removeFromCache(EtlJob job){
		boolean success = false;
		File file = this.getFile(job);
		if (file!=null){
			success = file.delete();
			if (success){
				String dirPath = getFiledir(job);
				File dir =  new File(dirPath);
				dir.delete();
			}
			technicalLog.debug(" - Deleting file:  " + file.getAbsolutePath() + " was" + (success ? " " : " not ") +"succesfull.");
		}
		return success;
	}

	@Override
	public boolean removeFromCacheRecursive(EtlJob job) {
		boolean success = false;
		String dirPath = getFiledirUuid(job);
		File dir =  new File(dirPath);		
		try {
			deleteDir(dir);
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return success;
	}
	
	  /**
	   * Deletes the specified directory and any files and directories in it
	   * recursively.
	   * 
	   * @param dir The directory to remove.
	   * @throws IOException If the directory could not be removed.
	   */
	private void deleteDir(File dir) throws IOException {
		if (!dir.isDirectory()) {
			throw new IOException("Not a directory " + dir);
		}

		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			if (file.isDirectory()) {
				deleteDir(file);
			} else {
				boolean deleted = file.delete();
				if (!deleted) {
					throw new IOException("Unable to delete file" + file);
				}
			}
		}

		dir.delete();
	  }
	
	public void setCdsFileCacheRoot(String cdsFileCacheRoot) {
		this.cdsFileCacheRoot = cdsFileCacheRoot;
	}

	public String getCdsFileCacheRoot() {
		return cdsFileCacheRoot;
	}

	protected String getIdentifier (final EtlJob job) {
		final String uuid = job.getUuid ();
		
		if (uuid.toLowerCase ().startsWith ("http")) {
			final String[] parts = uuid.split ("/");
			for (int i = parts.length - 1; i >= 0; -- i) {
				if (parts[i].length () > 0) {
					return parts[i];
				}
			}
			return uuid;
		} else if (!uuid.contains (";")) {
			return uuid;
		} 
		
		final String[] parts = uuid.split (";");
		
		return parts[parts.length - 1];
	}
}
