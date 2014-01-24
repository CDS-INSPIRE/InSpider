package nl.ipo.cds.admin;

public class BaseConfiguration {

	private String cdsVersion;
	private String mavenVersion;
	private String build;
	
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



}
