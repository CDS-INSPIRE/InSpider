package nl.ipo.cds.nagios;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import nl.ipo.cds.nagios.ast.HostStatusNode;
import nl.ipo.cds.nagios.ast.ServiceStatusNode;
import nl.ipo.cds.nagios.config.NagiosStatusConfiguration;
import nl.ipo.cds.nagios.harvester.Harvester;
import nl.ipo.cds.nagios.harvester.HarvesterListener;

public class NagiosStatus implements NagiosStatusService {
	private static final Log log = LogFactory.getLog (NagiosStatus.class);

	private final static String DEFAULT_FILENAME = "/var/cache/nagios3/status.dat";
	
	private static class Pair<A,B> {
		public final A	a;
		public final B	b;
		
		public Pair (final A a, final B b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair<?,?> other = (Pair<?,?>) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (b == null) {
				if (other.b != null)
					return false;
			} else if (!b.equals(other.b))
				return false;
			return true;
		}
	}
	
	private NagiosStatusConfiguration configuration;
	private Harvester harvester;
	
	private Map<String, HostStatusNode> hostStatus = new ConcurrentHashMap<String, HostStatusNode> ();
	private Map<Pair<String, String>, ServiceStatusNode> serviceStatus = new ConcurrentHashMap<NagiosStatus.Pair<String,String>, ServiceStatusNode> ();
	
	
	public NagiosStatus (final String filename, final Collection<String> hosts, final Collection<String> services) {
		configuration = new NagiosStatusConfiguration ();
		
		configuration.setLocation (new File (filename));
		configuration.setHosts (new HashSet<String> (hosts));
		configuration.setServices (new HashSet<String> (services));
		
		harvester = new Harvester (configuration);
	}
	
	public void run () {
		while (true) {
			final Set<String> oldHosts = new HashSet<String> (hostStatus.keySet ());
			final Set<Pair<String, String>> oldServices = new HashSet<NagiosStatus.Pair<String,String>> (serviceStatus.keySet ());
			
			harvester.harvest(new HarvesterListener() {
				@Override
				public void putStatus(HostStatusNode status) {
					final String key = status.getHostName ();
					hostStatus.put (key, status);
					oldHosts.remove (key);
				}
				
				@Override
				public void putStatus(ServiceStatusNode status) {
					final Pair<String, String> key = new Pair<String, String> (status.getHostName (), status.getServiceDescription ());
					serviceStatus.put(key, status);
					oldServices.remove (key);
				}
			});
			
			for (final String key: oldHosts) {
				hostStatus.remove (key);
			}
			for (final Pair<String, String> key: oldServices) {
				serviceStatus.remove (key);
			}
			
			log.debug (String.format ("Host status: %d, service status: %d", hostStatus.size (), serviceStatus.size ()));
			
			try {
				Thread.sleep (10000);
			} catch (InterruptedException e) {
				log.error ("Harvester interrupted", e);
			}
		}
	}
	
	public static Options createOptions () {
		final Options options = new Options ();
		options
			.addOption ("f", "file", true, "Status filename")
			.addOption ("H", "hosts", true, "Hostnames, comma separated")
			.addOption ("s", "services", true, "Services, comma separated");
		
		return options;
	}
	
	public static void printUsage (final String applicationName, final Options options, final OutputStream out) {
		final PrintWriter writer = new PrintWriter (out);
		final HelpFormatter usageFormatter = new HelpFormatter ();
		usageFormatter.printUsage (writer, 80, applicationName, options);
		writer.close ();
	}
	
	private static List<String> parseList (final String value) {
		final List<String> values = new ArrayList<String> ();
		
		for (final String s: value.split(",")) {
			values.add (s.trim ());
		}
		
		return values;
	}
	
	public static void main (final String[] args) {
		final CommandLineParser cmdLineParser = new GnuParser ();
		final Options options = createOptions ();
		final String filename;
		final List<String> hosts;
		final List<String> services;
		
		try {
			final CommandLine commandLine = cmdLineParser.parse (options, args);
			
			filename = commandLine.getOptionValue ("f", DEFAULT_FILENAME);
			hosts = parseList (commandLine.getOptionValue ("H", ""));
			services = parseList (commandLine.getOptionValue ("s", ""));
		} catch (ParseException e) {
			log.error ("Encountered an exception while parsing using GnuParser", e);
			System.exit (1);
			return;
		}
		
		// Add the commandline arguments as beans to the root context:
		final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory ();
		final BeanDefinition hostsBeanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(ArrayList.class)
				.addConstructorArg (hosts)
				.getBeanDefinition ();
		final BeanDefinition servicesBeanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(ArrayList.class)
				.addConstructorArg (services)
				.getBeanDefinition ();
		final BeanDefinition filenameBeanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(String.class)
				.addConstructorArg (filename)
				.getBeanDefinition ();
		
		beanFactory.registerBeanDefinition ("hosts", hostsBeanDefinition);
		beanFactory.registerBeanDefinition ("services", servicesBeanDefinition);
		beanFactory.registerBeanDefinition ("filename", filenameBeanDefinition);
		
		final GenericApplicationContext argumentsContext = new GenericApplicationContext (beanFactory);
		argumentsContext.refresh ();
		
		// Create the application context:
		final ApplicationContext ctx = new ClassPathXmlApplicationContext (new String[] { "nl/ipo/cds/nagios/nagios-status-server.xml" }, argumentsContext);
		final NagiosStatus nagiosStatus = (NagiosStatus)ctx.getBean ("nagiosStatusService");
		
		// Run the parser in the background:
		new Thread (new Runnable() {
			public void run() {
				nagiosStatus.run ();
			}
		}).start ();
	}

	@Override
	public Collection<HostStatusNode> getHostStatus() {
		return null;
	}

	@Override
	public Collection<ServiceStatusNode> getServiceStatus() {
		return null;
	}

	@Override
	public HostStatusNode getHostStatus(String hostName) {
		return hostStatus.get (hostName);
	}

	@Override
	public ServiceStatusNode getServiceStatus(String hostName,
			String serviceDescription) {
		return serviceStatus.get(new Pair<String, String> (hostName, serviceDescription));
	}

	@Override
	public Collection<String> getAvailableHosts() {
		return new ArrayList<String> (hostStatus.keySet ());
	}

	@Override
	public Collection<String> getAvailableServices() {
		final Set<String> services = new HashSet<String> ();
		
		for (final Pair<String, String> pair: serviceStatus.keySet ()) {
			services.add (pair.b);
		}
		
		return services;
	}
}
