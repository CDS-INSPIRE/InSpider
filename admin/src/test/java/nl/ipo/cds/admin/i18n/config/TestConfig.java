package nl.ipo.cds.admin.i18n.config;

import java.util.Locale;

import nl.ipo.cds.admin.i18n.EnableI18N;
import nl.ipo.cds.admin.i18n.LocaleProvider;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.StaticMessageSource;

@Configuration
@EnableI18N(basePackage="nl.ipo.cds.admin.i18n.messages")
public class TestConfig {
	
	@Bean
	MessageSource messageSource() {
		StaticMessageSource messageSource = new StaticMessageSource();			
		messageSource.addMessage("login.username", Locale.ENGLISH, "Username");
		messageSource.addMessage("login.loggedIn", Locale.ENGLISH, "Logged in as: {0}");
		return messageSource;
	}
	
	@Bean
	LocaleProvider localeProvider() {
		return new LocaleProvider() {

			@Override
			public Locale getLocale() {
				return Locale.ENGLISH;
			}				
		};
	}
}
