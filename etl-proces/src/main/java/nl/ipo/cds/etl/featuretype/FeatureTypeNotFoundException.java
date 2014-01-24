package nl.ipo.cds.etl.featuretype;

import org.deegree.feature.types.AppSchema;

public class FeatureTypeNotFoundException extends Exception {

	private static final long serialVersionUID = 1547299197666654575L;

	private final String featureTypeName;
	private final AppSchema appSchema;
	
	public FeatureTypeNotFoundException (final String featureTypeName, final AppSchema appSchema) {
		super (String.format ("Feature type not found `%s`", featureTypeName));
		
		this.featureTypeName = featureTypeName;
		this.appSchema = appSchema;
	}

	public String getFeatureTypeName () {
		return featureTypeName;
	}
	
	public AppSchema getAppSchema () {
		return appSchema;
	}
}
