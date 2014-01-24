package nl.ipo.cds.etl.config;

import nl.ipo.cds.etl.theme.annotation.SkipConfiguration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan (basePackageClasses = nl.ipo.cds.etl.theme.Package.class, excludeFilters = {
	@ComponentScan.Filter (type = FilterType.ANNOTATION, value = SkipConfiguration.class)
})
public class ThemeConfiguration {

}
