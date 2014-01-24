package nl.ipo.cds.nagios;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NagiosStatusClient {

	public static void main (String[] args) {
		final ApplicationContext ctx = new ClassPathXmlApplicationContext ("nl/ipo/cds/nagios/nagios-status-client.xml");
		final NagiosStatusService nagiosStatusService = (NagiosStatusService)ctx.getBean ("nagiosStatusService");
		
		System.out.println (nagiosStatusService.getAvailableHosts ());
		System.out.println (nagiosStatusService.getAvailableServices ());
	}
}
