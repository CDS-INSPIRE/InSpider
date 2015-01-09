package nl.ipo.cds.etl.postvalidation;

/**
 * Responsible for validating that geometries do not overlap.
 */

public interface IBulkValidator {

    public void overlapValidation(String jdbcConnectStr);
}
