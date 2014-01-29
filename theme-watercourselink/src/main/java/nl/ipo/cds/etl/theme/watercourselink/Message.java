package nl.ipo.cds.etl.theme.watercourselink;

import java.util.ArrayList;
import java.util.List;

import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.etl.ValidatorMessageKey;
import nl.ipo.cds.validation.AttributeExpression;
import nl.ipo.cds.validation.Expression;

public enum Message implements ValidatorMessageKey<Message, Context> {

	ATTRIBUTE_NULL,
	ATTRIBUTE_EMPTY,
	ATTRIBUTE_NOT_URL,
	ATTRIBUTE_VALUE_NEGATIVE,
	ATTRIBUTE_VALUE_TOO_LOW,
	ATTRIBUTE_VALUE_TOO_HIGH,
	ATTRIBUTE_CODE_CODESPACE_INVALID,
	ATTRIBUTE_CODE_INVALID,
	ATTRIBUTE_GROUP_INCONSISTENT,

	GEOMETRY_EMPTY_MULTIGEOMETRY,
	GEOMETRY_ONLY_POINT_OR_MULTIPOINT,
	GEOMETRY_ONLY_CURVE_OR_MULTICURVE,
	GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE,
	GEOMETRY_POINT_NOT_ALLOWED,
	GEOMETRY_POINT_DUPLICATION(Integer.MAX_VALUE, true),
	GEOMETRY_EXTERIOR_RING_CW(LogLevel.WARNING),
	GEOMETRY_INTERIOR_RING_CCW(LogLevel.WARNING),
	GEOMETRY_DISCONTINUITY(Integer.MAX_VALUE, true),
	GEOMETRY_SELF_INTERSECTION(Integer.MAX_VALUE, true),
	GEOMETRY_RING_NOT_CLOSED(Integer.MAX_VALUE, true),
	GEOMETRY_RING_SELF_INTERSECTION(Integer.MAX_VALUE, true),
	GEOMETRY_INTERIOR_RINGS_TOUCH(Integer.MAX_VALUE, true),
	GEOMETRY_INTERIOR_RINGS_INTERSECT(Integer.MAX_VALUE, true),
	GEOMETRY_INTERIOR_RINGS_WITHIN(Integer.MAX_VALUE, true),
	GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR(Integer.MAX_VALUE, true),
	GEOMETRY_INTERIOR_RING_INTERSECTS_EXTERIOR(Integer.MAX_VALUE, true),
	GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR(Integer.MAX_VALUE, true),
	GEOMETRY_INTERIOR_DISCONNECTED(Integer.MAX_VALUE, true),
	GEOMETRY_SRS_NULL,
	GEOMETRY_SRS_NOT_RD("EPSG:28992"),

	HAS_MORE_ERRORS(LogLevel.WARNING)
	;

	private final String[] params;

	private final LogLevel logLevel;

	private final int maxMessageLog;

	private final boolean addToShapeFile;

	private Message(LogLevel logLevel, Integer maxMessageLog, boolean addToShapeFile, String... params) {
		this.maxMessageLog = maxMessageLog == null ? 10 : maxMessageLog;
		this.logLevel = logLevel == null ? LogLevel.ERROR : logLevel;
		this.addToShapeFile = addToShapeFile;
		this.params = params;
	}

	private Message(LogLevel logLevel, Integer maxMessageLog, String... params) {
		this(logLevel, maxMessageLog, false, params);
	}

	private Message(Integer maxMessageLog, boolean addToShapeFile, String... params) {
		this(null, maxMessageLog, addToShapeFile, params);
	}

	private Message(LogLevel logLevel) {
		this(logLevel, null, false);
	}

	private Message(String... params) {
		this(null, null, false, params);
	}

	@Override
	public boolean isBlocking() {
		return getLogLevel ().equals (LogLevel.ERROR);
	}

	@Override
	public List<Expression<Message, Context, ?>> getMessageParameters () {
		final List<Expression<Message, Context, ?>> params = new ArrayList<> ();
		// why twice???
		params.add (new AttributeExpression<Message, Context, String> ("id", String.class));
		params.add (new AttributeExpression<Message, Context, String> ("id", String.class));
		params.add (new AttributeExpression<Message, Context, String> ("inspireIdLocalId", String.class));
		return params;
	}

	@Override
	public int getMaxMessageLog() {
		return maxMessageLog;
	}

	@Override
	public boolean isAddToShapeFile () {
		return addToShapeFile;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public Message getMaxMessageKey() {
		return HAS_MORE_ERRORS;
	}

}
