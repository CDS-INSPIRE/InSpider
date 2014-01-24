package nl.ipo.cds.deegree;

import nl.ipo.cds.properties.ConfigDirPropertyPlaceholderConfigurer;

import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({ "/nl/ipo/cds/dao/metadata/dao-applicationContext.xml",
"/nl/ipo/cds/dao/metadata/dataSource-applicationContext.xml" })
public class Config {

	@Bean
	public SQLDialect sqlDialect() {
		return new PostGISDialect( "1.5" );
	}

	@Bean
	public static ConfigDirPropertyPlaceholderConfigurer properties() {
		return new ConfigDirPropertyPlaceholderConfigurer();
	}

}
