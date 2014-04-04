package nl.ipo.cds.admin.i18n;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

public class SpringLocaleProvider implements LocaleProvider {

	@Override
	public Locale getLocale() {
		return LocaleContextHolder.getLocale();
	}
}
