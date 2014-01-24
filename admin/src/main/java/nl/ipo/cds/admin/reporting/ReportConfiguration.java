package nl.ipo.cds.admin.reporting;

import nl.ipo.cds.admin.BaseConfiguration;

public class ReportConfiguration extends BaseConfiguration {
	private String pgrBaseUrl;
	
	public String getPgrBaseUrl() {
		return pgrBaseUrl;
	}

	public void setPgrBaseUrl(String pgrBaseUrl) {
		this.pgrBaseUrl = pgrBaseUrl;
	}

}
