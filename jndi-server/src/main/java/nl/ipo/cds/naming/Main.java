package nl.ipo.cds.naming;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import nl.ipo.cds.properties.PropertyFilesReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Main {

	private static final Log logger = LogFactory.getLog(Main.class);
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		setupJndiServer();
	}

	private static void setupJndiServer() throws Exception {

		new org.jnp.server.Main().start();
		Context context = new InitialContext();
		populateContext(context);
	}

	private static void populateContext(Context context) throws NamingException, FileNotFoundException, IOException {
		Properties props = new Properties();
		
		String configDirString = System.getProperty("CONFIGDIR");
		if(configDirString == null) {
		    configDirString = System.getenv("TM_CONFIG_DIR");
		    if(configDirString == null) {
		        throw new IllegalStateException("CONFIGDIR system property is not set!");
		    }
		}

		File configDir = new File(configDirString);
		props = PropertyFilesReader.readPropertyFiles(configDir, props);
		
		Set<Map.Entry<Object,Object>> entrySet = props.entrySet();
		for (Iterator<Entry<Object, Object>> iterator = entrySet.iterator(); iterator.hasNext();) {
			Entry<Object, Object> entry = iterator.next();
			context.bind((String)entry.getKey(), entry.getValue());
		}
	}


}
