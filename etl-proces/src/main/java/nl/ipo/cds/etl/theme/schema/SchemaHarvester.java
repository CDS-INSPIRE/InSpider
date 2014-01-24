package nl.ipo.cds.etl.theme.schema;

import nl.ipo.cds.etl.process.DatasetMetadata;
import nl.ipo.cds.etl.process.HarvesterException;

import org.deegree.feature.types.AppSchema;

/**
 * Retrieves the <code>AppSchema</code> for a given dataset.
 */
public interface SchemaHarvester {

	public AppSchema parseApplicationSchema (final DatasetMetadata metadata) throws HarvesterException;
	
}
