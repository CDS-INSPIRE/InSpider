package nl.ipo.cds.admin.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FactoryConfiguration {

	@Bean
	public Factory i18nFactory(MessageSource messageSource, LocaleProvider localeProvider) throws NoSuchMethodException, SecurityException {
		return new Factory(messageSource, localeProvider);
	}
}
