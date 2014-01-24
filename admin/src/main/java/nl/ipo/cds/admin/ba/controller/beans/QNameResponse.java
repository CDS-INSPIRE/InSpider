package nl.ipo.cds.admin.ba.controller.beans;

import nl.ipo.cds.domain.QName;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class QNameResponse {

	@JsonIgnore
	private final QName qname;
	
	public QNameResponse (final QName qname) {
		this.qname = qname;
	}
	
	public String getLocalPart () {
		return qname.getLocalPart ();
	}
	
	public String getNamespace () {
		return qname.getNamespace ();
	}
}
