package nl.ipo.cds.etl.process;

import java.sql.Timestamp;

public class GmlMetadata extends DatasetMetadata {

	private final String featureTypeName;
	private final String featureCollectionUrl;
	private final String schemaUrl;
	
	public GmlMetadata (final String featureTypeName, final String featureCollectionUrl, final String schemaUrl) {
		this.featureTypeName = featureTypeName;
		this.featureCollectionUrl = featureCollectionUrl;
		this.schemaUrl = schemaUrl;
	}

	@Override
	public String getFeatureTypeName () {
		return featureTypeName;
	}

	@Override
	public String getFeatureCollectionUrl () {
		return featureCollectionUrl;
	}

	@Override
	public String getSchemaUrl () {
		return schemaUrl;
	}

	@Override
	public boolean isValid () {
		return featureTypeName != null && featureCollectionUrl != null && schemaUrl != null;
	}

	@Override
	public Timestamp getTimestamp () {
		return null;
	}
}
