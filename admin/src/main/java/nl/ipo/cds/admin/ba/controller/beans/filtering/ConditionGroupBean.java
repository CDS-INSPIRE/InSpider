package nl.ipo.cds.admin.ba.controller.beans.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class ConditionGroupBean {

	private List<ConditionBean> conditions = new ArrayList<ConditionBean> ();

	public List<ConditionBean> getConditions () {
		return Collections.unmodifiableList (conditions);
	}

	public void setConditions(List<ConditionBean> conditions) {
		this.conditions = new ArrayList<ConditionBean> (conditions);
	}
}
