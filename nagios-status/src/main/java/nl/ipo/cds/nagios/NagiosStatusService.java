package nl.ipo.cds.nagios;

import java.util.Collection;

import nl.ipo.cds.nagios.ast.HostStatusNode;
import nl.ipo.cds.nagios.ast.ServiceStatusNode;

public interface NagiosStatusService {

	public Collection<HostStatusNode> getHostStatus ();
	public Collection<ServiceStatusNode> getServiceStatus ();
	
	public HostStatusNode getHostStatus (String hostName);
	public ServiceStatusNode getServiceStatus (String hostName, String serviceDescription);
	
	public Collection<String> getAvailableHosts ();
	public Collection<String> getAvailableServices ();
}
