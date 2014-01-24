package nl.ipo.cds.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyFilesReader {

	private static final Log logger = LogFactory.getLog(PropertyFilesReader.class);

	public static Properties readPropertyFiles(File file) throws FileNotFoundException, IOException {
		return readPropertyFiles(file, new Properties());
	}
	
	public static Properties readPropertyFiles(File file, Properties allProps) throws FileNotFoundException, IOException {

		if(file.isDirectory()){
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				readPropertyFiles(files[i], allProps);
			}
		} else if(file.getAbsolutePath().endsWith(".properties")){
			Properties props = new Properties();

			logger.debug("Read properties from: " + file.getAbsolutePath());
			props.load(new FileInputStream(file));

			allProps.putAll(props);
		} else {
			logger.debug("Not a property-file: " + file.getAbsolutePath());
		}
		
		return allProps;
	}
}
