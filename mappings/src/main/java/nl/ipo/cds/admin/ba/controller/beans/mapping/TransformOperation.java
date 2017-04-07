package nl.ipo.cds.admin.ba.controller.beans.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class TransformOperation extends Operation {
	
	private List<Operation> operationInputs = new ArrayList<Operation> ();
	private Map<String, Object> settings = new HashMap<String, Object> ();

	public List<Operation> getOperationInputs () {
		return Collections.unmodifiableList (operationInputs);
	}

	public void setOperationInputs (final List<Operation> operationInputs) {
		this.operationInputs = new ArrayList<Operation> (operationInputs);
	}

	public Map<String, Object> getSettings () {
		return Collections.unmodifiableMap (settings);
	}

	public void setSettings (final Map<String, Object> settings) {
		this.settings = new HashMap<String, Object> (settings);
	}
}
