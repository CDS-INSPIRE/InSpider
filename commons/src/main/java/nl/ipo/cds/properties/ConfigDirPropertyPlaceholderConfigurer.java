package nl.ipo.cds.properties;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class ConfigDirPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private static final String CONFIGDIR_PROPERTY = "CONFIGDIR";
	
	@Override
	protected void loadProperties(Properties props) throws IOException {
		String configDirString = System.getProperty(CONFIGDIR_PROPERTY);
		if(configDirString == null) {
		    configDirString = System.getenv(CONFIGDIR_PROPERTY);
		    if(configDirString == null) {
		        throw new IllegalStateException(CONFIGDIR_PROPERTY + " system property is not set!");
		    }
		}

		File configDir = new File(configDirString);
		if(!configDir.exists()){
			throw new IllegalStateException(CONFIGDIR_PROPERTY + " does not exist!");
		}
		
		Properties configDirProps = PropertyFilesReader.readPropertyFiles(configDir, props);
		props.putAll(configDirProps);
		
		props.put(CONFIGDIR_PROPERTY, configDirString);
		
		props.put ("dojo.debug", true);
		
		super.loadProperties(props);
		
	}

}
