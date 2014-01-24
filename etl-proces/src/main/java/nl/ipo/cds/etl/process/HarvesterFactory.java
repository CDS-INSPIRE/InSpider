package nl.ipo.cds.etl.process;

import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.log.EventLogger;

public class HarvesterFactory {

	private String pgrBaseUrl;
	private EventLogger<HarvesterMessageKey> userLog;
	
	public void setPgrBaseUrl(String pgrBaseUrl) {
		this.pgrBaseUrl = pgrBaseUrl;
	}
	
	public void setUserLog(EventLogger<HarvesterMessageKey> userLog) {
		this.userLog = userLog;
	}
	
	public Harvester createHarvester(EtlJob job) {
		return new Harvester(userLog, job, createMetadataHarvester ());
	}
	
	public MetadataHarvester createMetadataHarvester () {
		return new MetadataHarvester (pgrBaseUrl);
	}
}
