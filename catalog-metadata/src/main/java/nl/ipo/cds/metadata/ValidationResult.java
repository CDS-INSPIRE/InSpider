package nl.ipo.cds.metadata;

public enum ValidationResult {

	VALID(""),	
	NOT_WELL_FORMED("metadata.notValid.notWellFormed"),
	SCHEMA_VIOLATION("metadata.notValid.schemaViolation"),
	DATE_PATH_MISSING("metadata.notValid.datePathMissing");
	
	ValidationResult(final String code) {
		this.code = code;
	}
	
	private final String code;
	
	public String getCode() {
		return code;
	}
}
