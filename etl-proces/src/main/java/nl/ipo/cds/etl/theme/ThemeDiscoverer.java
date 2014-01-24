package nl.ipo.cds.etl.theme;

import java.util.Set;

import javax.inject.Inject;

import nl.ipo.cds.etl.PersistableFeature;

import org.springframework.stereotype.Component;

@Component
public class ThemeDiscoverer {

	@Inject
	private Set<ThemeConfig<?>> themeConfigurations;
	
	public ThemeConfig<?> getThemeConfiguration (final String themeName) {
		if (themeName == null) {
			throw new IllegalArgumentException ("themeName cannot be null");
		}
		
		for (final ThemeConfig<?> config: themeConfigurations) {
			if (themeName.equals (config.getThemeName ())) {
				return config;
			}
			
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends PersistableFeature> ThemeConfig<T> getThemeConfiguration (final String themeName, final Class<? extends ThemeConfig<T>> configClass) {
		if (configClass == null) {
			throw new IllegalArgumentException ("configClass cannot be null");
		}
		
		final ThemeConfig<?> themeConfig = getThemeConfiguration (themeName);
		if (themeConfig == null) {
			return null;
		}
		
		if (!themeConfig.getClass ().equals (configClass)) {
			return null;
		}
		
		return (ThemeConfig<T>)themeConfig;
	}
}
