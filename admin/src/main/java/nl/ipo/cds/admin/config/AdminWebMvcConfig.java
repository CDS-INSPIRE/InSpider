package nl.ipo.cds.admin.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import nl.idgis.commons.mvc.config.annotation.EnableJSONViewResolver;
import nl.idgis.commons.mvc.config.annotation.EnableVelocityViewResolver;
import nl.idgis.commons.mvc.config.annotation.VelocityViewConfiguration;
import nl.ipo.cds.admin.BaseConfiguration;
import nl.ipo.cds.admin.ba.ViewContextHandlerInterceptorAdapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.springframework.web.servlet.theme.FixedThemeResolver;

@Configuration
@EnableWebMvc
@EnableJSONViewResolver (prefixJson = true)
@EnableVelocityViewResolver (cache = true, layoutUrl = "layouts/baLayout.vm", reloadVelocityMacroLibrary = true, resourceLoaderPath = "/WEB-INF/views/", velocityMacroLibrary = "velocity-macros.vm")
@ComponentScan (useDefaultFilters = false, basePackageClasses = nl.ipo.cds.admin.ba.controller.Package.class, includeFilters = {
	@ComponentScan.Filter (type = FilterType.ANNOTATION, value = Controller.class)
})
public class AdminWebMvcConfig extends WebMvcConfigurerAdapter implements VelocityViewConfiguration, ServletContextAware {
	
	private @Inject LocaleChangeInterceptor localeChangeInterceptor;
	private @Inject ViewContextHandlerInterceptorAdapter viewContextHandlerInterceptorAdapter;
	private @Inject WebContentInterceptor webContentInterceptor;
	
	private @Inject BaseConfiguration baseConfiguration;
	
	private ServletContext servletContext;
	
	@Override
	public void addResourceHandlers (final ResourceHandlerRegistry registry) {
	    // Handles HTTP GET requests for /resources/** by efficiently serving up static resources:
		registry
			.addResourceHandler ("/resources/**")
			.addResourceLocations ("/resources/**", "classpath:/META-INF/web-resources/");
	}
	
	@Override
	public void configureDefaultServletHandling (final DefaultServletHandlerConfigurer configurer) {
		// Allows for mapping the DispatcherServlet to "/" by forwarding static resource requests to the container's default Servlet
		configurer.enable ();
	}
	
	@Override
	public void addInterceptors (final InterceptorRegistry registry) {
		// register "global" interceptor beans to apply to all registered HandlerMappings		
		registry.addInterceptor (localeChangeInterceptor ());
		registry.addInterceptor (viewContextHandlerInterceptorAdapter ());
		registry.addInterceptor (webContentInterceptor ());
	}
	
	/**
	 * Route exceptions to defaultException.vm with a default status code of 500.
	 */
	public @Bean SimpleMappingExceptionResolver simpleMappingExceptionResolver () {
		final SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver ();
		
		resolver.setDefaultErrorView ("defaultException");
		resolver.setDefaultStatusCode (500);
		
		return resolver;
	}
	
	/**
	 * Selects a static view for rendering without the need for an explicit controller.
	 */
	public void addViewControllers (final ViewControllerRegistry registry) {
		registry.addViewController ("/uncaughtException");
		registry.addViewController ("/resourceNotFound");
	}
	
	/**
	 * Resolves localized messages*.properties and application.properties files in the application to allow for internationalization. 
     * The messages*.properties files translate Roo generated messages which are part of the admin interface, the application.properties
     * resource bundle localizes all application specific messages such as entity names and menu items.
	 */
	public @Bean ReloadableResourceBundleMessageSource messageSource () {
		final ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource ();
		
		source.setBasenames ("WEB-INF/i18n/messages", "WEB-INF/i18n/application");
		source.setFallbackToSystemLocale (false);
		
		return source;
	}
	
	/**
	 * Store preferred language configuration in a cookie
	 */
	public @Bean CookieLocaleResolver localeResolver () {
		final CookieLocaleResolver resolver = new CookieLocaleResolver ();
		
		resolver.setCookieName ("locale");
		
		return resolver;
	}

	/**
	 * Resolves localized <theme_name>.properties files in the classpath to allow for theme support.
	 */
	public @Bean ResourceBundleThemeSource themeSource () {
		return new ResourceBundleThemeSource ();
	}

	/**
	 * Set a fixed default theme name.
	 */
	public @Bean FixedThemeResolver themeResolver () {
		final FixedThemeResolver themeResolver = new FixedThemeResolver ();
		
		themeResolver.setDefaultThemeName ("default");
		
		return themeResolver;
	}

	/**
	 * Allows for integration of file upload functionality.
	 */
	public @Bean CommonsMultipartResolver multipartResolver () {
		return new CommonsMultipartResolver ();
	}
	
	public @Bean LocaleChangeInterceptor localeChangeInterceptor () {
		if (this.localeChangeInterceptor == null) {
			this.localeChangeInterceptor = new LocaleChangeInterceptor ();
			
			localeChangeInterceptor.setParamName ("lang");
		}
		
		return this.localeChangeInterceptor;
	}
	
	public @Bean ViewContextHandlerInterceptorAdapter viewContextHandlerInterceptorAdapter () {
		if (this.viewContextHandlerInterceptorAdapter == null) {
			this.viewContextHandlerInterceptorAdapter = new ViewContextHandlerInterceptorAdapter (); 
		}
		return this.viewContextHandlerInterceptorAdapter;
	}
	
	public @Bean WebContentInterceptor webContentInterceptor () {
		if (this.webContentInterceptor == null) {
			this.webContentInterceptor = new WebContentInterceptor ();
		
			this.webContentInterceptor.setAlwaysUseFullPath (true);
			this.webContentInterceptor.setCacheSeconds (0);
			
			this.webContentInterceptor.setCacheMappings (new Properties () {
				private static final long serialVersionUID = 1L;
				
				{
					put ("/styles/**", "86400");
					put ("/scripts/**", "86400");
					put ("/images/**", "86400");
				}
			});
		}
		
		return this.webContentInterceptor;
	}

	@Override
	public void setServletContext (final ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Adds default properties to each view. This method adds the 'dojoDebug' property based on which the
	 * default layout decides to use a CDN version of dojo (debug) or a custom built version (production).
	 * Tests whether a production build of dojo is available in the war.
	 */
	@Override
	public Map<String, Object> getAttributes () {
		
		// Test whether a production build of dojo is available in the war:
		final InputStream dojoStream = servletContext.getResourceAsStream ("/scripts/cds/dojo/dojo.js");
		final boolean hasDojoProduction;
		
		if (dojoStream != null) {
			hasDojoProduction = true;
			try {
				dojoStream.close ();
			} catch (IOException e) {
			}
		} else {
			hasDojoProduction = false;
		}

		// Return default properties for use in velocity views:
		return new HashMap<String, Object> () {
			private static final long serialVersionUID = 1L;
			{
				put ("dojoDebug", !hasDojoProduction);
				put ("mavenVersion", baseConfiguration.getMavenVersion ());
				put ("cdsVersion", baseConfiguration.getCdsVersion ());
				put ("buildVersion", baseConfiguration.getBuild());
			}
		};
	}
}
