package nl.ipo.cds.nagios.harvester;

import nl.ipo.cds.nagios.ast.HostStatusNode;
import nl.ipo.cds.nagios.ast.ServiceStatusNode;

public interface HarvesterListener {
	
	public void putStatus (ServiceStatusNode status);
	public void putStatus (HostStatusNode status);
}
