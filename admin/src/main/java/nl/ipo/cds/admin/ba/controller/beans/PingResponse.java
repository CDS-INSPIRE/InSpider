package nl.ipo.cds.admin.ba.controller.beans;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class PingResponse {

	public String getStatus () {
		return "OK";
	}
}
