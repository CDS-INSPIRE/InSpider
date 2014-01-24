package nl.ipo.cds.admin.ba.controller.beans.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize
public final class Mapping {

	private String featureTypeNamespace;
	private String featureTypeName;
	private String attributeName;
	private List<Operation> operations = new ArrayList<Operation> ();

	public String getFeatureTypeNamespace () {
		return featureTypeNamespace;
	}

	public void setFeatureTypeNamespace (final String featureTypeNamespace) {
		this.featureTypeNamespace = featureTypeNamespace;
	}

	public String getFeatureTypeName () {
		return featureTypeName;
	}

	public void setFeatureTypeName (final String featureTypeName) {
		this.featureTypeName = featureTypeName;
	}

	public String getAttributeName () {
		return attributeName;
	}

	public void setAttributeName (final String attributeName) {
		this.attributeName = attributeName;
	}

	public List<Operation> getOperations () {
		return Collections.unmodifiableList (operations);
	}

	public void setOperations (final List<Operation> operations) {
		this.operations = new ArrayList<Operation> (operations);
	}
}