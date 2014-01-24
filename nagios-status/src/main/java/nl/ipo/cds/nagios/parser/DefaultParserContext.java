package nl.ipo.cds.nagios.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DefaultParserContext implements ParserContext, Serializable {

	private static final long serialVersionUID = 5672004746032281743L;
	
	private String filename;
	private List<ParserError> errors = new ArrayList<ParserError> ();
	
	public DefaultParserContext (final String filename) {
		this.filename = filename;
	}
	
	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public void reportError(int line, int column, String message) {
		errors.add (new ParserError (this, line, column, message));
	}

	public boolean hasErrors () {
		return errors.size () > 0;
	}
	
	public List<ParserError> getErrors () {
		return errors;
	}
}
