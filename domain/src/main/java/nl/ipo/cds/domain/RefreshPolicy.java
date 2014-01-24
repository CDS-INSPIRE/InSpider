package nl.ipo.cds.domain;

public enum RefreshPolicy {
	/** Allow a new refresh each day **/
	ONCE_A_DAY,
	/** Allow, if last-modified HTTP header changed **/
	IF_MODIFIED_HTTP_HEADER,
	/** Allow, if last-modified information in metadata record changed **/
	IF_MODIFIED_METADATA
}
