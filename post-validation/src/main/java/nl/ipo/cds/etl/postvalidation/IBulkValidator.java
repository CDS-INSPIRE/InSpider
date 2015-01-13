package nl.ipo.cds.etl.postvalidation;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import nl.ipo.cds.validation.domain.OverlapValidationPair;

/**
 * Responsible for validating that geometries do not overlap.
 */

public interface IBulkValidator {

    public List<OverlapValidationPair<Blob, Blob>> overlapValidation(DataSource dataSource) throws SQLException;
}
