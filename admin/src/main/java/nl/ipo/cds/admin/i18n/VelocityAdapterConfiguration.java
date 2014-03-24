package nl.ipo.cds.admin.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VelocityAdapterConfiguration {	
	
	@Bean
	public VelocityAdapter velocityAdapter() {
		return new VelocityAdapter();
	}
}
