package nl.ipo.cds.admin;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import nl.ipo.cds.admin.ba.listener.CleanupListener;
import nl.ipo.cds.admin.config.AdminConfig;
import nl.ipo.cds.admin.config.AdminWebMvcConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class WebInit implements WebApplicationInitializer {

	private static final Log logger = LogFactory.getLog (WebInit.class);
	
	@Override
	public void onStartup (final ServletContext servletContext) throws ServletException {
		// Create the root application context:
		final AnnotationConfigWebApplicationContext rootContext = 
				new AnnotationConfigWebApplicationContext ();
		
		rootContext.setServletContext (servletContext);
		rootContext.register (AdminConfig.class);
		rootContext.refresh ();

		// Manage the lifecycle of the root application context:
		servletContext.addListener (new ContextLoaderListener (rootContext));
		servletContext.setInitParameter ("defaultHtmlEscape", "true");
		
		servletContext.addListener (new CleanupListener ());
		
		// Character encoding filter: force UTF-8
		final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter ();
		
		characterEncodingFilter.setEncoding ("UTF-8");
		characterEncodingFilter.setForceEncoding (true);
		
		servletContext
			.addFilter ("characterEncodingFilter", characterEncodingFilter)
			.addMappingForUrlPatterns (EnumSet.of (DispatcherType.REQUEST), true, "/*");
		
		// Spring HTTP method filter: provides overrides for HTTP method types using hidden form fields (e.g. PUT)
		servletContext
			.addFilter ("httpMethodFilter", new HiddenHttpMethodFilter ())
			.addMappingForUrlPatterns (EnumSet.of (DispatcherType.REQUEST), true, "/*");
		
		// Spring security filter:
		servletContext
			.addFilter ("springSecurityFilterChain", new DelegatingFilterProxy ())
			.addMappingForUrlPatterns (EnumSet.of (DispatcherType.REQUEST), true, "/*");
		
		// Configure the dispatcher servlet
		final AnnotationConfigWebApplicationContext mvcContext =
				new AnnotationConfigWebApplicationContext ();
		
		mvcContext.register (AdminWebMvcConfig.class);
		
		// Register the Spring MVC servlet:
		final ServletRegistration.Dynamic dispatcher = servletContext.addServlet (
				"dispatcher", new DispatcherServlet (mvcContext)
			);
		
		dispatcher.setLoadOnStartup (1);
		dispatcher.setAsyncSupported (true);
		
		// Attempt to map the servlet on "/":
		final Set<String> mappingConflicts = dispatcher.addMapping ("/");
		
		if (!mappingConflicts.isEmpty ()) {
			for (final String conflict: mappingConflicts) {
				logger.error (String.format ("Mapping conflict: %s", conflict));
			}
			throw new IllegalStateException ("Servlet cannot be mapped to '/' under Tomcat versions <= 7.0.14");
		}
	}
}
