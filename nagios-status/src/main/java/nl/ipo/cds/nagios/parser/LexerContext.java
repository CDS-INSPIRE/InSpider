package nl.ipo.cds.nagios.parser;

import java.io.Serializable;

public class LexerContext implements Serializable {
	private static final long serialVersionUID = 3861109371242145935L;
	
	private String	filename;
	
	public LexerContext (final String filename) {
		this.filename = filename;
	}
	
	public String getFilename () {
		return filename;
	}
}
