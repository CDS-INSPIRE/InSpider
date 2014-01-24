package nl.ipo.cds.etl.process;

import java.sql.Timestamp;

import nl.ipo.cds.etl.process.MetadataHarvester.FeatureCollectionReference;
import nl.ipo.cds.etl.process.MetadataHarvester.FeatureCollectionType;

public class PgrMetadata extends DatasetMetadata {

	private final String metadataUrl;
	private final Timestamp timestamp;
	private final FeatureCollectionReference featureCollectionReference;
	
	public PgrMetadata (final String metadataUrl, final Timestamp timestamp, final FeatureCollectionReference featureCollectionReference) {
		assert metadataUrl != null;
		assert timestamp != null;
		assert featureCollectionReference != null;
		
		this.metadataUrl = metadataUrl;
		this.timestamp = timestamp;
		this.featureCollectionReference = featureCollectionReference;
	}

	public FeatureCollectionReference getFeatureCollectionReference () {
		return featureCollectionReference;
	}
	
	public String getMetadataUrl () {
		return metadataUrl;
	}

	@Override
	public boolean isValid () {
		return featureCollectionReference.featureTypeName != null;
	}
	
	@Override
	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String getWfsUrl() {
		return featureCollectionReference.url;
	}

	@Override
	public String getFeatureTypeName () {
		return featureCollectionReference.featureTypeName;
	}
	
	@Override
	public String getFeatureCollectionUrl () {
		return featureCollectionReference.url;
	}
	
	@Override
	public String getSchemaUrl() {
		// Perform a DescribeFeatureType on WFS datasets, get the schema from
		// a modified feature collection URL for GML datasets:
		if (featureCollectionReference.type == FeatureCollectionType.WFS) {
			final String url = getWfsUrl();
			final String typeName = getFeatureTypeName();
			final String separator = url.indexOf("?") == -1 ? "?" : url.endsWith("?") || url.endsWith("&") ? "" : "&";

			return url + separator + "request=DescribeFeatureType" + "&typename=" + typeName + "&service=WFS"
					+ "&version=1.1.0";
		} else {
			// if featureCollectionReference.xsdUrl exists return it,
			// else make xsd url name from featureCollectionUrl
			if (featureCollectionReference.xsdUrl == null || featureCollectionReference.xsdUrl.isEmpty()) {
				final String featureCollectionUrl = featureCollectionReference.url;
				final int stripIndex = featureCollectionUrl.indexOf('?');
				final String strippedUrl = 
					featureCollectionUrl.endsWith("?") ? featureCollectionUrl.substring(0, stripIndex) : featureCollectionUrl;
				if (strippedUrl.toLowerCase().endsWith(".gml") || strippedUrl.toLowerCase().endsWith(".xml")) {
					return strippedUrl.substring(0, strippedUrl.length() - 4) + ".xsd";
				} else {
					return strippedUrl + ".xsd";
				}
			} else {
				return featureCollectionReference.xsdUrl;
			}
		}
	}
}
