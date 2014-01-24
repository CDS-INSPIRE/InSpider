package nl.ipo.cds.nagios.harvester;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import nl.ipo.cds.nagios.ast.HostStatusNode;
import nl.ipo.cds.nagios.ast.ServiceStatusNode;
import nl.ipo.cds.nagios.config.NagiosStatusConfiguration;

import org.junit.Before;
import org.junit.Test;

public class HarvesterTest {

	private ClassLoader loader;
	private Harvester harvester;
	
	@Before
	public void before () {
		final NagiosStatusConfiguration config = new NagiosStatusConfiguration ();
		
		config.setLocation(new File ("/var/cache/nagiosn3/status.dat"));
		config.setHosts (new HashSet<String> (Arrays.asList (new String[] { "inspire-host-a" })));
		config.setServices (new HashSet<String> (Arrays.asList (new String[] { "Aborted ETL jobs" })));
		
		loader = ClassLoader.getSystemClassLoader ();
		
		harvester = new Harvester (config);
	}
	
	@Test
	public void testHarvester () throws Exception {
		final InputStream input = loader.getResourceAsStream ("nl/ipo/cds/nagios/harvester/status.dat");
		final List<ServiceStatusNode> services = new ArrayList<ServiceStatusNode> ();
		final List<HostStatusNode> hosts = new ArrayList<HostStatusNode> ();
		
		assertNotNull (input);
		
		harvester.harvest (new HarvesterListener() {
			@Override
			public void putStatus(HostStatusNode status) {
				hosts.add (status);
			}
			
			@Override
			public void putStatus(ServiceStatusNode status) {
				services.add (status);
			}
		}, input);
		
		assertEquals (1, hosts.size ());
		assertEquals (1, services.size ());
		assertEquals ("inspire-host-a", hosts.get (0).getHostName ());
		assertEquals ("inspire-host-a", services.get (0).getHostName ());
		assertEquals ("Aborted ETL jobs", services.get (0).getServiceDescription ());
	}
	
}
