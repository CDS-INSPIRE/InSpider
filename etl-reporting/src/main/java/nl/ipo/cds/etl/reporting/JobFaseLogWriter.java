package nl.ipo.cds.etl.reporting;

import java.io.StringWriter;
import java.io.Writer;

/**
 * Abstract base class for JobFaseLog writers. Log items are written using a template, which is identified using a name
 * and a context. The template context typically denotes the type of output the template generates, e.g.: text, html, xml, etc.
 * Instances of this class provide defaults for the template name, the context and the default character encoding.
 * 
 * @author Erik Orbons
 *
 */
public abstract class JobFaseLogWriter {
	
	private String defaultTemplate = "default";
	private String defaultContext = "default";
	private String defaultEncoding = "UTF-8";
	
	/**
	 * 
	 * @return The name of the default template.
	 */
	public String getDefaultTemplate () {
		return defaultTemplate;
	}
	
	/**
	 * 
	 * @param defaultTemplate The name of the default template.
	 */
	public void setDefaultTemplate (String defaultTemplate) {
		this.defaultTemplate = defaultTemplate;
	}
	
	/**
	 * 
	 * @return The name of the default context.
	 */
	public String getDefaultContext () {
		return defaultContext;
	}
	
	/**
	 * 
	 * @param defaultContext The new default context name.
	 */
	public void setDefaultContext (String defaultContext) {
		this.defaultContext = defaultContext;
	}
	
	/**
	 * 
	 * @return The default encoding for templates ("UTF-8").
	 */
	public String getDefaultEncoding () {
		return defaultEncoding;
	}
	
	/**
	 * 
	 * @param defaultEncoding The new default encoding for templates (e.g. "UTF-8").
	 */
	public void setDefaultEncoding (String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * 
	 * @param context			The context containing log lines and optional parameters.
	 * @param template			The name of the template to use.
	 * @param templateContext	The name of the template context to use.
	 * @param encoding			The character encoding to use for the template.
	 * @param writer			The writer to which the template output is written.
	 * @throws WriterException
	 */
	public abstract void write (LogWriterContext context, String template, String templateContext, String encoding, Writer writer) throws WriterException;

	/**
	 * 
	 * @param context			The context containing log lines and optional parameters.
	 * @param writer			The writer to which the template output is written.
	 * @throws WriterException
	 */
	public void write (LogWriterContext context, Writer writer) throws WriterException {
		write (context, getDefaultTemplate (), getDefaultContext (), getDefaultEncoding (), writer);
	}

	/**
	 * 
	 * @param context			The context containing log lines and optional parameters.
	 * @param template			The name of the template to use.
	 * @param templateContext	The name of the template context to use.
	 * @param writer			The writer to which the template output is written.
	 * @throws WriterException
	 */
	public void write (LogWriterContext context, String template, String templateContext, Writer writer) throws WriterException {
		write (context, template, templateContext, getDefaultEncoding (), writer);
	}

	/**
	 * 
	 * @param context			The context containing log lines and optional parameters.
	 * @param template			The name of the template to use.
	 * @param writer			The writer to which the template output is written.
	 * @throws WriterException
	 */
	public void write (LogWriterContext context, String template, Writer writer) throws WriterException {
		write (context, template, getDefaultContext (), writer);
	}

	/**
	 * 
	 * @param context			The context containing log lines and optional parameters.
	 * @return					The output after applying the template.				
	 * @throws WriterException
	 */
	public String write (LogWriterContext context) throws WriterException {
		return write (context, getDefaultTemplate (), getDefaultContext ());
	}

	/**
	 * 
	 * @param context			The context containing log lines and optional parameters.
	 * @param template			The name of the template to use.
	 * @return					The output after applying the template.
	 * @throws WriterException
	 */
	public String write (LogWriterContext context, String template) throws WriterException {
		return write (context, template, getDefaultContext ());
	}

	/**
	 * 
	 * @param context			The context containing log lines and optional parameters.
	 * @param template			The name of the template to use.
	 * @param templateContext	The name of the template context to use.
	 * @return					The output after applying the template.
	 * @throws WriterException
	 */
	public String write (LogWriterContext context, String template, String templateContext) throws WriterException {
		final StringWriter stringWriter = new StringWriter ();
		
		write (context, template, templateContext, stringWriter);
		
		return stringWriter.toString ();
	}
}
