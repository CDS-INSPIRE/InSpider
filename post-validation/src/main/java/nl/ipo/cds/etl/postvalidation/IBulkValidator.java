package nl.ipo.cds.etl.postvalidation;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import nl.ipo.cds.validation.domain.OverlapValidationPair;

/**
 * Responsible for validating that geometries do not overlap.
 */

public interface IBulkValidator<T extends Serializable> {

    public List<OverlapValidationPair<T, T>> overlapValidation(DataSource dataSource) throws SQLException, IOException, ClassNotFoundException;
}
