package nl.ipo.cds.nagios.ast;

import java.util.Date;
import java.util.List;

import nl.ipo.cds.nagios.parser.ParserContext;

public class HostStatusNode extends ObjectNode {
	public HostStatusNode (final ParserContext parserContext, int line, int column, final List<KVPNode> kvps) {
		super (parserContext, line, column, kvps);
	}
	
	public String getHostName () {
		return getValue ("host_name");
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
	
	public Date getLastCheck () {
		return new Date (Long.parseLong (getValue ("last_check")));
	}
	
	public Date getLastStateChange () {
		return new Date (Long.parseLong (getValue ("last_state_change")));
	}
	
	public Date getLastHardStateChange () {
		return new Date (Long.parseLong (getValue ("last_hard_state_change")));
	}
	
	public boolean isFlapping () {
		return Integer.parseInt (getValue ("is_flapping")) != 0;
	}
	
	public int getScheduledDowntimeDepth () {
		return Integer.parseInt (getValue ("scheduled_downtime_depth"));
	}
}
