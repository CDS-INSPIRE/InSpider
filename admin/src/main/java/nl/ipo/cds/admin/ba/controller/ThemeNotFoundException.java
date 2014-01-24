package nl.ipo.cds.admin.ba.controller;

public class ThemeNotFoundException extends Exception {

	private static final long serialVersionUID = 3255380769214570440L;
	
	private final String themeName;
	
	public ThemeNotFoundException (final String themeName) {
		this.themeName = themeName;
	}

	public ThemeNotFoundException (final String themeName, final String message) {
		super (message);
		this.themeName = themeName;
	}

	public ThemeNotFoundException (final String themeName, final Throwable cause) {
		super(cause);
		this.themeName = themeName;
	}

	public ThemeNotFoundException (final String themeName, final String message, final Throwable cause) {
		super(message, cause);
		this.themeName = themeName;
	}

	public String getThemeName () {
		return themeName;
	}
}
