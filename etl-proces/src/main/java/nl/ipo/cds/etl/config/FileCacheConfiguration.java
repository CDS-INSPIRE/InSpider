package nl.ipo.cds.etl.config;

import nl.ipo.cds.etl.FileCache;
import nl.ipo.cds.etl.file.FileCacheImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileCacheConfiguration {
	@Bean
	public FileCache fileCache (final @Value("${cdsFileCacheRoot}") String cdsFileCacheRoot) {
		final FileCacheImpl fileCache = new FileCacheImpl ();
		
		fileCache.setCdsFileCacheRoot (cdsFileCacheRoot);
		
		return fileCache;
	}
}