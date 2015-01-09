package nl.ipo.cds.etl.postvalidation;

import org.springframework.stereotype.Service;

/**
 * Responsible for validating that geometries do not overlap.
 */
@Service
public class BulkValidator implements IBulkValidator {

    @Override
    public void overlapValidation() {

    }

}
