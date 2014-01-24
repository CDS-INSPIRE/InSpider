package nl.ipo.cds.etl.process;

public class HarvesterException extends Exception {

	private static final long serialVersionUID = -7677003557201194483L;
	
	private final HarvesterMessageKey messageKey;
	private final String url;
	private final String[] parameters;
	
	public HarvesterException (final Throwable cause, final HarvesterMessageKey messageKey, final String url, final String ... parameters) {
		super (cause);
		
		this.messageKey = messageKey;
		this.url = url;
		this.parameters = parameters;
	}
	
	public HarvesterException (final HarvesterMessageKey messageKey, final String url, final String ... parameters) {
		this (null, messageKey, url, parameters);
	}
	
	public HarvesterMessageKey getMessageKey () {
		return messageKey;
	}
	
	public String getUrl () {
		return url;
	}
	
	public String[] getParameters () {
		return parameters;
	}
	
	public String getMessage () {
		return String.format ("%s: %s", messageKey, url);
	}
}