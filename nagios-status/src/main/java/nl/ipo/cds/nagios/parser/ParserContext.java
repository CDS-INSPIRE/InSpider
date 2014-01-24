package nl.ipo.cds.nagios.parser;

public interface ParserContext {
	public String getFilename ();
	public void reportError (int line, int column, String message);
}
