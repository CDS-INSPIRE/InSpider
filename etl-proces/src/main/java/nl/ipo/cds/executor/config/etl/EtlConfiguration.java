package nl.ipo.cds.executor.config.etl;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan (basePackageClasses = { nl.ipo.cds.etl.config.Package.class, nl.ipo.cds.etl.process.Package.class } )
public class EtlConfiguration {

}
