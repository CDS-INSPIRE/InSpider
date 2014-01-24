package nl.ipo.cds.admin.ba.controller.beans;

import java.util.Locale;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import nl.ipo.cds.attributemapping.operations.OperationInputType;

@JsonSerialize (include = Inclusion.ALWAYS)
@JsonPropertyOrder ({
	"name",
	"description",
	"type",
	"variableInputCount"
})
public class OperationInputTypeResponse {

	@JsonIgnore
	private final OperationInputType inputType;
	
	public OperationInputTypeResponse (final OperationInputType inputType) {
		this.inputType = inputType;
	}

	public String getName () {
		return inputType.getName ();
	}

	public String getDescription () {
		try {
			return inputType.getDescription (Locale.getDefault ());
		} catch (Exception e) {
			e.printStackTrace ();
			return "";
		}
	}

	public String getType () {
		return inputType.getInputType ().toString ();
	}
	
	public boolean getVariableInputCount () {
		return inputType.isVariableInputCount ();
	}
}
