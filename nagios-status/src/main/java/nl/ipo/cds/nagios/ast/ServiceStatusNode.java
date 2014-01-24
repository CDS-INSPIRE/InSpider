package nl.ipo.cds.nagios.ast;

import java.util.List;

import nl.ipo.cds.nagios.parser.ParserContext;

public class ServiceStatusNode extends ObjectNode {
	public ServiceStatusNode (final ParserContext parserContext, int line, int column, final List<KVPNode> kvps) {
		super (parserContext, line, column, kvps);
	}
	
	public String getHostName () {
		return getValue ("host_name");
	}
	
	public String getServiceDescription () {
		return getValue ("service_description");
	}
	
	public int getCurrentState () {
		return Integer.parseInt (getValue ("current_state"));
	}

	public int getLastHardState () {
		return Integer.parseInt (getValue ("last_hard_state"));
	}
	
	public String getPluginOutput () {
		return getValue ("plugin_output");
	}
	
	public String getLongPluginOutput () {
		return getValue ("long_plugin_output");
	}
	
	public String getPerformanceData () {
		return getValue ("performance_data");
	}
	
	public boolean isFlapping () {
		return Integer.parseInt (getValue ("is_flapping")) != 0;
	}
	
	public int getScheduledDowntimeDepth () {
		return Integer.parseInt (getValue ("scheduled_downtime_depth"));
	}
}
