package nl.ipo.cds.admin.ba.controller.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class PreviewMappingResponse {
	private final List<PreviewLogLineResponse> logItems;
	private final List<Map<String, String>> inputFeatures;
	private final List<Map<String, String>> outputFeatures;
	
	public PreviewMappingResponse (final List<PreviewLogLineResponse> logItems, final List<Map<String, String>> inputFeatures, final List<Map<String, String>> outputFeatures) {
		this.logItems = new ArrayList<PreviewLogLineResponse> (logItems);
		this.inputFeatures = new ArrayList<Map<String, String>> (inputFeatures);
		this.outputFeatures = new ArrayList<Map<String, String>> (outputFeatures);
	}

	public List<PreviewLogLineResponse> getLogItems () {
		return Collections.unmodifiableList (logItems);
	}

	public List<Map<String, String>> getInputFeatures() {
		return inputFeatures;
	}

	public List<Map<String, String>> getOutputFeatures() {
		return outputFeatures;
	}
}
