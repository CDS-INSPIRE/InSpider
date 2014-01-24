package nl.ipo.cds.etl.process;

import java.sql.Timestamp;

public abstract class DatasetMetadata {

	/**
	 * Returns true if the metadata is valid.
	 */
	public abstract boolean isValid ();
	
	/**
	 * Returns the feature type name for this dataset.
	 */
	public abstract String getFeatureTypeName ();
	
	/**
	 * Returns the feature collection URL: either a WFS GetFeature URL
	 * or a reference to a GML feature collection.
	 */
	public abstract String getFeatureCollectionUrl ();
	
	/**
	 * Returns the schema URL: either a WFS DescribeFeatureType URL or
	 * a direct reference to a GML application schema.
	 */
	public abstract String getSchemaUrl ();
	
	/**
	 * Returns a timestamp indicating the last update time of the dataset, or
	 * null if no update time could be determined.
	 */
	public abstract Timestamp getTimestamp ();
}