package nl.ipo.cds.etl.reporting.velocity;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import nl.ipo.cds.etl.reporting.JobFaseLogWriter;
import nl.ipo.cds.etl.reporting.LogWriterContext;
import nl.ipo.cds.etl.reporting.WriterException;

/**
 * An implementation of JobFaseLogWriter that uses velocity as a template engine.
 * 
 * Templates are loaded in the form:
 *   [templatePath]/[templateName].[templateContext].vm
 *   
 * @author Erik Orbons
 */
public class VelocityJobFaseLogWriter extends JobFaseLogWriter {

	private VelocityEngine velocityEngine;
	private String templatePath = "";
	private Class<? extends Context> contextClass = VelocityContext.class;
	
	/**
	 * Constructs a VelocityJobFaseLogWriter using a new instance of the Velocity engine. The engine
	 * is initialized to use the classpath loader and the UTF-8 character encoding.
	 */
	public VelocityJobFaseLogWriter () {
		velocityEngine = new VelocityEngine ();
		
		velocityEngine.setProperty (RuntimeConstants.RESOURCE_LOADER, "classpath");
		velocityEngine.setProperty ("classpath.resource.loader.class", ClasspathResourceLoader.class.getName ());
		velocityEngine.setProperty ("input.encoding", "UTF-8");
		
		velocityEngine.init ();
	}
	
	/**
	 * Constructs a VelocityJobFaseLogWriter using an existing velocity engine. The engine must have been initialized (e.g. the init
	 * method must have been invoked).
	 * 
	 * @param velocityEngine The velocity engine to be used by this writer.
	 */
	public VelocityJobFaseLogWriter (VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}
	
	/**
	 * Returns the velocity engine that is used by this writer.
	 * 
	 * @return The velocity engine instance.
	 */
	public VelocityEngine getVelocityEngine () {
		return velocityEngine;
	}

	/**
	 * Returns the template path. The template path serves as a prefix when resolving template names and using most velocity resource
	 * loaders the prefix corresponds to a path.
	 * 
	 * @return The velocity template path.
	 */
	public String getTemplatePath () {
		return templatePath;
	}
	
	/**
	 * Sets the template path. The template path serves as a prefix when resolving template names and using most velocity resource
	 * loaders the prefix corresponds to a path.
	 * 
	 * @param templatePath The new velocity template path.
	 */
	public void setTemplatePath (String templatePath) {
		this.templatePath = templatePath;
	}
	
	/**
	 * Returns the velocity context class.
	 * 
	 * @return The current context class to use in this writer.
	 */
	public Class<? extends Context> getContextClass () {
		return contextClass;
	}

	/**
	 * 
	 * @param contextClass
	 */
	public void setContextClass (Class<? extends Context> contextClass) {
		try {
			contextClass.getConstructor (Context.class);
		} catch (SecurityException e) {
			throw new IllegalArgumentException ("Context class has no accessible constructor that takes a Velocity context as the only argument.", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException ("Context class has no accessible constructor that takes a Velocity context as the only argument.", e);
		}
		
		this.contextClass = contextClass;
	}
	
	@Override
	public void write (LogWriterContext context, String template, String templateContext, String encoding, Writer writer) throws WriterException {
		final Map<String, Object> parameters = context.getParameters ();
		final String velocityTemplate = String.format ("%s/%s.%s.vm", getTemplatePath (), template, templateContext);
		
		// Create a velocity context:
		Context velocityContext;
		if (parameters != null) {
			// Use an inner context to protect the parameter map from mutations:
			try {
				Constructor<? extends Context> ctr = contextClass.getConstructor (Context.class);
				velocityContext = ctr.newInstance (new VelocityContext (parameters));
			} catch (SecurityException e) {
				throw new WriterException ("Error instantiating Velocity context class: constructor inaccessible", e);
			} catch (NoSuchMethodException e) {
				throw new WriterException ("Error instantiating Velocity context class: no suitable constructor found", e);
			} catch (InvocationTargetException e) {
				throw new WriterException ("Error instantiating Velocity context class: error invoking constructor", e);
			} catch (IllegalAccessException e) {
				throw new WriterException ("Error instantiating Velocity context class: error invoking constructor", e);
			} catch (InstantiationException e) {
				throw new WriterException ("Error instantiating Velocity context class: error instantiating context", e);
			}
		} else {
			try {
				velocityContext = contextClass.newInstance ();
			} catch (InstantiationException e) {
				throw new WriterException ("Error instantiating Velocity context class", e);
			} catch (IllegalAccessException e) {
				throw new WriterException ("Error instantiating Velocity context class", e);
			}
			velocityContext = new VelocityContext ();
		}
		velocityContext.put ("logItems", context.getLogItems ());
		
		// Merge the context and the template:
		try {
			getVelocityEngine ().mergeTemplate (velocityTemplate, encoding, velocityContext, writer);
		} catch (ResourceNotFoundException e) {
			throw new WriterException (e);
		} catch (ParseErrorException e) {
			throw new WriterException (e);
		} catch (MethodInvocationException e) {
			throw new WriterException (e);
		}
	}
}
