package nl.ipo.cds.etl.config;

import javax.sql.DataSource;

import nl.ipo.cds.etl.util.ScriptExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptExecutorConfiguration {

	@Bean
	public ScriptExecutor scriptExecutor(final DataSource dataSource) {
		return new ScriptExecutor(dataSource);
	}
}
