package nl.ipo.cds.admin.ba.controller.beans.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class DatasetFilterBean {

	private List<ConditionGroupBean> conditionGroups = new ArrayList<ConditionGroupBean> ();

	public List<ConditionGroupBean> getConditionGroups() {
		return Collections.unmodifiableList (conditionGroups);
	}

	public void setConditionGroups (final List<ConditionGroupBean> conditionGroups) {
		this.conditionGroups = new ArrayList<ConditionGroupBean> (conditionGroups);
	}
}
