package nl.ipo.cds.metadata.config;

import java.io.File;
import java.io.IOException;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.metadata.MetadataManager;
import nl.ipo.cds.metadata.MetadataTransformer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.SAXException;

@Configuration
public class MetadataManagerConfiguration {
	
	@Value("${metadataFolder}")
	private File metadataFolder;
	
	@Bean
	public MetadataManager metadataManager() throws IOException, SAXException {
		return new MetadataManager(metadataFolder);
	}
	
	@Bean
	public MetadataTransformer metadataTransformer(final MetadataManager metadataManager, final ManagerDao managerDao) {
		return new MetadataTransformer(metadataManager, managerDao);
	}
}
