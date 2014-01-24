package nl.ipo.cds.admin.ba.controller;

public class AttributeNotFoundException extends Exception {

	private static final long serialVersionUID = -124949160096426793L;
	
	private final String themeName;
	private final String attributeName;
	
	public AttributeNotFoundException (final String themeName, final String attributeName) {
		this.themeName = themeName;
		this.attributeName = attributeName;
	}

	public AttributeNotFoundException (final String themeName, final String attributeName, String message) {
		super(message);
		
		this.themeName = themeName;
		this.attributeName = attributeName;
	}

	public AttributeNotFoundException (final String themeName, final String attributeName, final Throwable cause) {
		super(cause);
		
		this.themeName = themeName;
		this.attributeName = attributeName;
	}

	public AttributeNotFoundException (final String themeName, final String attributeName, final String message, final Throwable cause) {
		super(message, cause);
		
		this.themeName = themeName;
		this.attributeName = attributeName;
	}

	public String getThemeName () {
		return themeName;
	}

	public String getAttributeName () {
		return attributeName;
	}
}
