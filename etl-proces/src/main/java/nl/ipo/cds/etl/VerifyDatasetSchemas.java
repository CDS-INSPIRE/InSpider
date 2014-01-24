package nl.ipo.cds.etl;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.sql.DataSource;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.etl.process.DatasetMetadata;
import nl.ipo.cds.etl.process.HarvesterFactory;
import nl.ipo.cds.etl.process.MetadataHarvester;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;
import nl.ipo.cds.etl.util.BlockingExecutor;
import nl.ipo.cds.executor.ConfigDir;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class VerifyDatasetSchemas implements Runnable {

	private static final int ADVISORY_LOCK_KEY = 0;
	
	@Configuration
	@ComponentScan (basePackageClasses = { nl.ipo.cds.etl.config.Package.class, nl.ipo.cds.etl.theme.Package.class })
	@ImportResource ({
		"/nl/ipo/cds/dao/dataSource-applicationContext.xml",
		"/nl/ipo/cds/dao/dao-applicationContext.xml"
	})
	public static class Config {
		@Bean
		public ConfigDir configDir (final @Value("file:${CONFIGDIR}") String configDirPath) {
			return new ConfigDir (configDirPath);
		}
		
		@Bean
		public VerifyDatasetSchemas verifyDatasetSchemas () {
			return new VerifyDatasetSchemas ();
		}
		
		@Bean
		public Executor executor () {
			return new BlockingExecutor (1);
		}
	}
	
	@Inject
	private ManagerDao managerDao;

	@Inject
	private HarvesterFactory harvesterFactory;
	
	@Inject
	private DataSource dataSource;
	
	@Inject
	private ThemeDiscoverer themeDiscoverer;
	
	public static void main (final String ... args) {
		try {
			final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext (Config.class);
			
			applicationContext.getBean (VerifyDatasetSchemas.class).run ();
			
			applicationContext.close ();
		} catch(Exception e) {
			e.printStackTrace ();
			System.exit(1);
		}		
		System.exit(0);
	}

	@Override
	public void run () {
		try {
			Connection connection = DataSourceUtils.getConnection(dataSource);
			PreparedStatement stmt = connection.prepareStatement("select pg_try_advisory_lock(?)");
			stmt.setLong(1, ADVISORY_LOCK_KEY);
			ResultSet rs = stmt.executeQuery();
			boolean result = false;
			while(rs.next()) {
				result = rs.getBoolean(1);
				
			}
			
			rs.close();
			stmt.close();
			
			if(!result) {
				System.err.println("Couldn't acquire lock (JobExecutor already running?)");				
				return;
			}
			
			System.out.println ("Listing dataset");
			final OutputStream os = new FileOutputStream ("datasets.csv");
			final PrintWriter writer = new PrintWriter (new OutputStreamWriter (os, Charset.forName ("UTF-8")));
			
			for (final Dataset ds: managerDao.getAllDatasets ()) {
				processDataset (ds, writer);
			}
			
			writer.close ();
			os.close ();
			
			DataSourceUtils.releaseConnection(connection, dataSource);
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}
	
	public void processDataset (final Dataset dataset, final PrintWriter writer) {
		String result = "OK";
		String describeFeatureTypeURL = "";
		try {
			final MetadataHarvester metadataHarvester = harvesterFactory.createMetadataHarvester ();
			final DatasetMetadata metadata = metadataHarvester.parseMetadata (dataset.getUuid ());			
			describeFeatureTypeURL = metadata.getSchemaUrl ();
			ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration(dataset.getDatasetType().getThema().getNaam());
			themeConfig.getSchemaHarvester().parseApplicationSchema(metadata);
		} catch (Exception e) {
			result = String.format ("%s: %s", e.getClass ().getCanonicalName (), e.getMessage ());
		}
		
		writer.println (String.format (
				"%s;%s;%s;%s;%s", 
				dataset.getBronhouder ().getNaam (), 
				dataset.getDatasetType ().getNaam (), 
				dataset.getUuid (),
				describeFeatureTypeURL,
				result
			));
	}
}
