package nl.ipo.cds.admin.ba.controller;


public class MonitoringConfiguration {
	private String nagiosUrl;
	private String muninUrl;
	private String[] awstatsUrls;
	private String[] awstatsNames;
	private String[] nagiosHosts;
	private String nagiosHostgroup;

	/**
	 * @return the nagiosUrl
	 */
	public String getNagiosUrl() {
		return nagiosUrl;
	}
	/**
	 * @param nagiosUrl the nagiosUrl to set
	 */
	public void setNagiosUrl(String nagiosUrl) {
		this.nagiosUrl = nagiosUrl;
	}
	/**
	 * @return the muninUrl
	 */
	public String getMuninUrl() {
		return muninUrl;
	}
	/**
	 * @param muninUrl the muninUrl to set
	 */
	public void setMuninUrl(String muninUrl) {
		this.muninUrl = muninUrl;
	}
	/**
	 * @return the awstatsUrls
	 */
	public String[] getAwstatsUrls() {
		return awstatsUrls;
	}
	/**
	 * @param awstatsUrls the awstatsUrls to set
	 */
	public void setAwstatsUrls(String[] awstatsUrls) {
		this.awstatsUrls = awstatsUrls;
	}
	/**
	 * @return the awstatsNames
	 */
	public String[] getAwstatsNames() {
		return awstatsNames;
	}
	/**
	 * @param awstatsNames the awstatsNames to set
	 */
	public void setAwstatsNames(String[] awstatsNames) {
		this.awstatsNames = awstatsNames;
	}
	/**
	 * @return the nagiosHost
	 */
	public String[] getNagiosHosts() {
		return nagiosHosts;
	}
	/**
	 * @param nagiosHost the nagiosHost to set
	 */
	public void setNagiosHosts(String[] nagiosHosts) {
		this.nagiosHosts = nagiosHosts;
	}
	/**
	 * @return the nagiosHostgroup
	 */
	public String getNagiosHostgroup() {
		return nagiosHostgroup;
	}
	/**
	 * @param nagiosHostgroup the nagiosHostgroup to set
	 */
	public void setNagiosHostgroup(String nagiosHostgroup) {
		this.nagiosHostgroup = nagiosHostgroup;
	}
	
}
