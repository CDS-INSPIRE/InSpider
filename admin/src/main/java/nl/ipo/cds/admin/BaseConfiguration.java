package nl.ipo.cds.admin;

public class BaseConfiguration {

	private String cdsVersion;
	private String mavenVersion;
	private String build;
	private String requestAuthorizationPrompt;
	private String requestAuthorizationHref;
	
	public String getCdsVersion() {
		System.err.println("get cdsVersion: "+cdsVersion);
		return cdsVersion;
	}

	public void setCdsVersion(String cdsVersion) {
		System.err.println("set cdsVersion: "+cdsVersion);
		this.cdsVersion = cdsVersion;
	}

	public String getMavenVersion() {
		System.err.println("get mavenVersion: "+mavenVersion);
		return mavenVersion;
	}

	public void setMavenVersion(String mavenVersion) {
		System.err.println("set mavenVersion: "+mavenVersion);
		this.mavenVersion = mavenVersion;
	}

	public String getBuild() {
		System.err.println("get build: "+build);
		return build;
	}

	public void setBuild(String build) {
		System.err.println("set build: "+build);
		this.build = build;
	}

	/**
	 * @return The prompt to display on the login page to request authorization.
	 */
	public String getRequestAuthorizationPrompt () {
		return requestAuthorizationPrompt;
	}

	/**
	 * @param requestAuthorizationPrompt The prompt to display on the login page to request authorization.
	 */
	public void setRequestAuthorizationPrompt (final String requestAuthorizationPrompt) {
		this.requestAuthorizationPrompt = requestAuthorizationPrompt;
	}

	/**
	 * @return The href to use on the authorization prompt.
	 */
	public String getRequestAuthorizationHref () {
		return requestAuthorizationHref;
	}

	/**
	 * @param requestAuthorizationHref The href to use on the authorization prompt.
	 */
	public void setRequestAuthorizationHref (final String requestAuthorizationHref) {
		this.requestAuthorizationHref = requestAuthorizationHref;
	}
}
