package nl.ipo.cds.nagios.config;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class NagiosStatusConfiguration {
	private File location = new File ("/var/cache/nagios3/status.dat");
	private Charset charset = Charset.forName ("ISO-8859-1");
	private Set<String> hosts = new HashSet<String> ();
	private Set<String> services = new HashSet<String> ();
	
	public File getLocation() {
		return location;
	}
	public void setLocation (File location) {
		this.location = location;
	}
	public Set<String> getHosts () {
		return hosts;
	}
	public void setHosts (Set<String> hosts) {
		this.hosts = hosts;
	}
	public Set<String> getServices () {
		return services;
	}
	public void setServices (Set<String> services) {
		this.services = services;
	}
	public Charset getCharset() {
		return charset;
	}
	public void setCharset(Charset charset) {
		this.charset = charset;
	}
}
