package nl.ipo.cds.nagios.harvester;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nl.ipo.cds.nagios.ast.HostStatusNode;
import nl.ipo.cds.nagios.ast.ObjectNode;
import nl.ipo.cds.nagios.ast.ServiceStatusNode;
import nl.ipo.cds.nagios.config.NagiosStatusConfiguration;
import nl.ipo.cds.nagios.parser.Lexer;
import nl.ipo.cds.nagios.parser.LexerContext;
import nl.ipo.cds.nagios.parser.Parser;
import nl.ipo.cds.nagios.parser.ParserContext;

public class Harvester {

	private NagiosStatusConfiguration configuration;
	private LexerContext lexerContext;
	private ParserContext parserContext;
	
	private static final Log log = LogFactory.getLog (Harvester.class);
	
	public Harvester (final NagiosStatusConfiguration configuration) {
		this.configuration = configuration;
		lexerContext = new LexerContext (configuration.getLocation ().getPath ());
		parserContext = new ParserContext() {
			@Override
			public void reportError(int line, int column, String message) {
				logError (configuration.getLocation ().getPath (), line, column, message);
			}
			
			@Override
			public String getFilename() {
				return configuration.getLocation ().getPath ();
			}
		};
	}
	
	public void harvest (final HarvesterListener harvesterListener) {
		harvest (harvesterListener, null);
	}
	
	protected void harvest (final HarvesterListener harvesterListener, final InputStream is) {
		try {
			final InputStream inputStream = is != null ? is : new FileInputStream (configuration.getLocation ());
			final InputStreamReader reader = new InputStreamReader (inputStream, configuration.getCharset ());
			final Parser parser = new Parser(parserContext, new Lexer (lexerContext, reader));
			
			for (final ObjectNode node: parser) {
				if (node instanceof ServiceStatusNode) {
					final ServiceStatusNode status = (ServiceStatusNode)node;
					if (configuration.getHosts ().contains (status.getHostName ()) && configuration.getServices().contains (status.getServiceDescription ())) {
						harvesterListener.putStatus ((ServiceStatusNode)node);
					}
				} else if (node instanceof HostStatusNode) {
					final HostStatusNode status = (HostStatusNode)node;
					if (configuration.getHosts ().contains (status.getHostName ())) {
						harvesterListener.putStatus ((HostStatusNode)node);
					}
				}
			}
		} catch (FileNotFoundException e) {
			logError (configuration.getLocation ().getPath (), 0, 0, "File not found");
		}
	}
	
	private void logError (final String filename, final int line, final int column, String message) {
		log.error (String.format ("%s:%d,%d: %s", filename, line, column, message));
	}
}
