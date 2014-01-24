package nl.ipo.cds.etl.theme.protectedSite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.AbstractValidator;
import nl.ipo.cds.etl.ValidatorMessageKey;
import nl.ipo.cds.validation.AttributeExpression;
import nl.ipo.cds.validation.DefaultValidatorContext;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationReporter;
import nl.ipo.cds.validation.Validator;
import nl.ipo.cds.validation.callbacks.Callback;
import nl.ipo.cds.validation.callbacks.UnaryCallback;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;

import org.apache.commons.io.IOUtils;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.primitive.DefaultPoint;

public class ProtectedSiteValidator 
	extends AbstractValidator<ProtectedSite, ProtectedSiteValidator.MessageKey, ProtectedSiteValidator.Context> {
	
	private static final Set<String> protectionClassification = new HashSet<String> (Arrays.asList(new String[] {
			"natureConservation",
			"archaeological",
			"cultural",
			"ecological",
			"landscape",
			"environment",
			"geological"
	}));
	
	private static final String protectionClassificationConcat = concat (protectionClassification);

	private final static Map<String, String[]> designations;
	static {
		final HashMap<String, String[]> designationsMap = new HashMap<String, String[]> ();
		
		designationsMap.put("AW", new String[]{"aardkundigeWaarden"});
		designationsMap.put("EHS", new String[]{"ecologischeHoofdstructuur"});
		designationsMap.put("WAV", new String[]{"WAVGebieden"});
		designationsMap.put("ST", new String[]{"stilteGebieden"});
		designationsMap.put("PM", new String[]{"provincialeMonumenten"});
		designationsMap.put("NL", new String[]{"nationaleLandschappen"});
		
		designations = Collections.unmodifiableMap (designationsMap);
	}
	
	private static final String separator = ", ";	
	
	private static final Point defaultPoint = new DefaultPoint(null, null, null, new double[]{1.0, 2.0});
	
	public enum MessageKey implements ValidatorMessageKey<MessageKey, Context> {
		ID_NULL,
		
		INSPIREID_NULL,
		INSPIREID_DUPLICATE(LogLevel.ERROR, null, "NL.9931.ST.70CD5476-1A7F-4476-A72D-B45E515A99BA"), 
		INSPIREID_PARTS(LogLevel.ERROR, null, "Invalid.ID"), 
		INSPIREID_NL(LogLevel.ERROR, null, "DE"), 
		INSPIREID_BRONHOUDER(LogLevel.ERROR, null, "9940","9931"),
		INSPIREID_DATASET(LogLevel.ERROR, null, "NE", "NL"),
		INSPIREID_UUID(LogLevel.ERROR, null, "70CD5476-1A7F-4A99BA"),
		
		LEGALFOUNDATIONDOCUMENT_ELEMENT_NULL,
		LEGALFOUNDATIONDOCUMENT_ELEMENT_EMPTY,
		LEGALFOUNDATIONDOCUMENT_ELEMENT_INVALID(LogLevel.ERROR, null, "invalid://url"),
		LEGALFOUNDATIONDOCUMENT_NOT_FOUND(LogLevel.ERROR, null, "http://host.invalid/document.html"),
		LEGALFOUNDATIONDOCUMENT_EMPTY(LogLevel.ERROR, null, "http://host.test/empty-document.html"),
		
		LEGALFOUNDATIONDATE_NULL,
		LEGALFOUNDATIONDATE_INVALID(LogLevel.ERROR, null, "17-DEC-10"),
		
		SITEDESIGNATION_NULL,
		SITEDESIGNATION_INVALID(LogLevel.ERROR, null, "AardkundigWaarde", protectionClassificationConcat),
		SITEDESIGNATION_ILLEGAL_FORMAT(LogLevel.ERROR, null, 
			"stilteGebieden:stilteGebieden|overigDesignationSchema:onzeDesignation:75:ongeldig"),
		
		SITEPROTECTIONCLASSIFICATION_NULL,
		SITEPROTECTIONCLASSIFICATION_INVALID(LogLevel.ERROR, null, "naturConservation", protectionClassificationConcat),
		
		GEOMETRY_NULL,		
		GEOMETRY_POINT_DUPLICATION(Integer.MAX_VALUE, true, pointToString(defaultPoint)),
		GEOMETRY_EXTERIOR_RING_CW(LogLevel.WARNING),
		GEOMETRY_INTERIOR_RING_CCW(LogLevel.WARNING),
		GEOMETRY_DISCONTINUITY(Integer.MAX_VALUE, true),
		GEOMETRY_SELF_INTERSECTION(Integer.MAX_VALUE, true, pointToString(defaultPoint)),
		GEOMETRY_RING_NOT_CLOSED(Integer.MAX_VALUE, true),
		GEOMETRY_RING_SELF_INTERSECTION(Integer.MAX_VALUE, true, pointToString(defaultPoint)),
		GEOMETRY_INTERIOR_RINGS_TOUCH(Integer.MAX_VALUE, true, pointToString(defaultPoint)),
		GEOMETRY_INTERIOR_RINGS_INTERSECT(Integer.MAX_VALUE, true, pointToString(defaultPoint)),
		GEOMETRY_INTERIOR_RINGS_WITHIN(Integer.MAX_VALUE, true),
		GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR(Integer.MAX_VALUE, true, pointToString(defaultPoint)),
		GEOMETRY_INTERIOR_RING_INTERSECTS_EXTERIOR(Integer.MAX_VALUE, true, pointToString(defaultPoint)),
		GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR(Integer.MAX_VALUE, true),
		GEOMETRY_INTERIOR_DISCONNECTED(Integer.MAX_VALUE, true),
		GEOMETRY_SRS_NULL,
		GEOMETRY_SRS_NOT_RD("EPSG:28992"),
		
		HAS_MORE_EVENTS(LogLevel.WARNING);
		
		private final String[] params;
		
		private final LogLevel logLevel;
		
		private final int maxMessageLog;

		private final boolean addToShapeFile;
		
		private MessageKey(LogLevel logLevel, Integer maxMessageLog, boolean addToShapeFile, String... params) {
			this.maxMessageLog = maxMessageLog == null ? 10 : maxMessageLog;
			this.logLevel = logLevel == null ? LogLevel.ERROR : logLevel;
			this.addToShapeFile = addToShapeFile;
			this.params = params;
		}

		private MessageKey(LogLevel logLevel, Integer maxMessageLog, String... params) {
			this(logLevel, maxMessageLog, false, params);
		}

		private MessageKey(Integer maxMessageLog, boolean addToShapeFile, String... params) {
			this(null, maxMessageLog, addToShapeFile, params);
		}
		
		private MessageKey(LogLevel logLevel) {
			this(logLevel, null, false);
		}
		
		private MessageKey(String... params) {
			this(null, null, false, params);
		}
		
		@Override
		public int getMaxMessageLog () {
			return maxMessageLog;
		}
		
		@Override
		public boolean isAddToShapeFile () {
			return addToShapeFile;
		}

		public String[] getParams () {
			return params;
		}
		
		@Override
		public LogLevel getLogLevel () {
			return logLevel;
		}
		
		@Override
		public List<Expression<MessageKey, Context, ?>> getMessageParameters () {
			final List<Expression<MessageKey, Context, ?>> params = new ArrayList<Expression<MessageKey, Context, ?>> ();
			
			params.add (new AttributeExpression<MessageKey, Context, String> ("id", String.class));
			params.add (new AttributeExpression<MessageKey, Context, String> ("inspireID", String.class));
			
			return params;
		}

		@Override
		public MessageKey getMaxMessageKey () {
			return HAS_MORE_EVENTS;
		}

		@Override
		public boolean isBlocking() {
			return getLogLevel ().equals (LogLevel.ERROR);
		}
	}
	
	public ProtectedSiteValidator (final Map<Object, Object> validatorMessages) throws CompilerException {
		super (Context.class, ProtectedSite.class, validatorMessages);
		
		compile ();
	}

	@Override
	public Context beforeJob (final EtlJob job, final CodeListFactory codeListFactory, final ValidationReporter<MessageKey, Context> reporter) {
		final String datasetCode = job.getDatasetType ().getNaam ();
		final String[] designation = designations.get (datasetCode);
		
		return new Context (codeListFactory, reporter, job, designation);
	}
	
	// =========================================================================
	// Validation rules:
	// =========================================================================
	public Validator<MessageKey, Context> getLegalFoundationDocumentValidator () {
		final UnaryCallback<MessageKey, Context, Boolean, String> checkSizeCallback = new UnaryCallback<MessageKey, Context, Boolean, String> () {
			@Override
			public Boolean call(String input, Context context) throws Exception {
				final String document = stripAnchor (input);
				if (context.hasDocument (document)) {
					return context.getDocumentSize (document) > 0;
				}
				
				final URL url = new URL (document);
				
				try {
					final InputStream inputStream = url.openConnection ().getInputStream ();
					final ByteArrayOutputStream outputStream = new ByteArrayOutputStream ();
					
					IOUtils.copy (inputStream, outputStream);

					context.storeDocumentSize (document, (long)outputStream.size ());
					
					return outputStream.size () > 0;
				} catch (IOException e) {
					// Return true, but don't add to the checkedUrls list so that the next test fails:
					return true;
				}
			}
		}; 
		final UnaryCallback<MessageKey, Context, Boolean, String> checkDocumentExistsCallback = new UnaryCallback<MessageKey, Context, Boolean, String> () {
			@Override
			public Boolean call(String input, Context context) throws Exception {
				return context.hasDocument (stripAnchor (input));
			}
		};
		
		return validate (
			and (
				// Some preconditions that terminate the validation:
				validate (not (stringAttr ("legalFoundationDocument").isNull ())).message (MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_NULL),
				validate (not (isBlank (stringAttr ("legalFoundationDocument")))).message (MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_EMPTY),
				validate (isUrl (stringAttr ("legalFoundationDocument"))).message (MessageKey.LEGALFOUNDATIONDOCUMENT_ELEMENT_INVALID, stringAttr ("legalFoundationDocument")),
				
				and (
					// Tests that don't short-circuit:
					validate (callback (Boolean.class, stringAttr ("legalFoundationDocument"), checkSizeCallback)).message (MessageKey.LEGALFOUNDATIONDOCUMENT_EMPTY, stringAttr ("legalFoundationDocument")),
					validate (callback (Boolean.class, stringAttr ("legalFoundationDocument"), checkDocumentExistsCallback)).message (MessageKey.LEGALFOUNDATIONDOCUMENT_NOT_FOUND, stringAttr ("legalFoundationDocument"))
				)
			).shortCircuit ()
		);
	}
	
	public Validator<MessageKey, Context> getLegalFoundationDateValidator () {
		return validate (
				not (attr ("legalFoundationDate", Date.class).isNull ())
			)
			.message (MessageKey.LEGALFOUNDATIONDATE_NULL);
	}
	
	public Validator<MessageKey, Context> getIdValidator () {
		return validate (
				not (stringAttr ("id").isNull ())
			)
			.message (MessageKey.ID_NULL);
	}
	
	public Validator<MessageKey, Context> getSiteDesignationValidator () {
		final Callback<MessageKey, Context, Boolean> initFlagCallback = new Callback<MessageKey, Context, Boolean> () {
			@Override
			public Boolean call (final Context context) throws Exception {
				context.setFlag (false);
				return true;
			}
		};
		
		final UnaryCallback<MessageKey, Context, Boolean, String[]> validateSiteDesignationCallback = new UnaryCallback<MessageKey, Context, Boolean, String[]> () {
			@Override
			public Boolean call (final String[] input, final Context context) throws Exception {
				final boolean siteDesignationValid = context.getFlag ();

				if(!siteDesignationValid) {
					if(input.length == 1 || (input.length > 1 && input[0].equals(input[1]))) {
						context.setFlag (contains (input[0], context.getValidDesignations ()));
					}
				}
				
				return true;
			}
		};
		
		final Callback<MessageKey, Context, Boolean> getFlagCallback = new Callback<MessageKey, Context, Boolean> () {
			@Override
			public Boolean call (final Context context) throws Exception {
				return context.getFlag ();
			}
		};
		
		return validate (
			and (
				validate (not (attr ("siteDesignation", String[].class).isNull ())).message (MessageKey.SITEDESIGNATION_NULL),
				callback (Boolean.class, initFlagCallback),
				forEach ("i", attr ("siteDesignation", String[].class), validate (
					split (stringAttr ("i"), constant (":"), validate (
						and (
							validate (lte (intAttr ("length"), constant (3))).message (MessageKey.SITEDESIGNATION_ILLEGAL_FORMAT, stringAttr ("i")),
							callback (Boolean.class, attr ("values", String[].class), validateSiteDesignationCallback)
						).shortCircuit ()
					))
				)),
				validate (callback (Boolean.class, getFlagCallback)).message (MessageKey.SITEDESIGNATION_INVALID, join (attr ("siteDesignation", String[].class), constant ("|")), join (attribute ("validDesignations", String[].class), constant (", ")))
			).shortCircuit ()
		);		
	}
	
	public Validator<MessageKey, Context> getSiteProtectionClassificationValidator () {
		return validate (
				and (
					validate (not (attr ("siteProtectionClassification", String[].class).isNull ())).message (MessageKey.SITEPROTECTIONCLASSIFICATION_NULL),
					forEach (
						"i",
						attr ("siteProtectionClassification", String[].class),
						validate (in (stringAttr ("i"), constant (protectionClassification))).message (MessageKey.SITEPROTECTIONCLASSIFICATION_INVALID, stringAttr ("i"), constant (protectionClassificationConcat))
					)
				).shortCircuit ()
			);
	}
	
	public Validator<MessageKey, Context> getGeometryValidator () {
		return validate (
				and (
					// The following validations short-circuit, there must be a non-null and non-empty geometry:
					validate (not (geometry ("geometry").isNull ())).message (MessageKey.GEOMETRY_NULL),
					validate (not (geometry ("geometry").isEmptyMultiGeometry ())).message (MessageKey.GEOMETRY_NULL),
					
					// Non short-circuited validations:
					and (
						// Short circuit to prevent the interiorDisconnected validation if
						// any of the other validations fail:
						and (
							and (
								validate (not (geometry ("geometry").hasCurveDuplicatePoint ())).message (MessageKey.GEOMETRY_POINT_DUPLICATION, lastLocation ()),
								validate (not (geometry ("geometry").hasCurveDiscontinuity ())).message (MessageKey.GEOMETRY_DISCONTINUITY),
								validate (not (geometry ("geometry").hasCurveSelfIntersection ())).message (MessageKey.GEOMETRY_SELF_INTERSECTION, lastLocation ()),
								validate (not (geometry ("geometry").hasUnclosedRing ())).message (MessageKey.GEOMETRY_RING_NOT_CLOSED),
								validate (not (geometry ("geometry").hasRingSelfIntersection ())).message (MessageKey.GEOMETRY_RING_SELF_INTERSECTION, lastLocation ()),
								validate (not (geometry ("geometry").hasTouchingInteriorRings ())).message(MessageKey.GEOMETRY_INTERIOR_RINGS_TOUCH, lastLocation ()),
								validate (not (geometry ("geometry").hasInteriorRingsWithin ())).message (MessageKey.GEOMETRY_INTERIOR_RINGS_WITHIN)
							),
							validate (not (this.geometry ("geometry").isInteriorDisconnected ())).message (MessageKey.GEOMETRY_INTERIOR_DISCONNECTED)
						).shortCircuit (),

						// Non-blocking validations:
						validate (not (geometry ("geometry").hasExteriorRingCW ())).nonBlocking ().message (MessageKey.GEOMETRY_EXTERIOR_RING_CW),
						validate (not (geometry ("geometry").hasInteriorRingCCW ())).nonBlocking ().message (MessageKey.GEOMETRY_INTERIOR_RING_CCW),
						validate (not (geometry ("geometry").hasInteriorRingTouchingExterior ())).nonBlocking ().message (MessageKey.GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR, lastLocation ()),
						validate (not (geometry ("geometry").hasInteriorRingOutsideExterior ())).nonBlocking ().message (MessageKey.GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR),
						
						// SRS validations:
						validate (this.geometry ("geometry").hasSrs ()).message (MessageKey.GEOMETRY_SRS_NULL),
						validate (this.geometry ("geometry").isSrs (constant ("28992"))).message (MessageKey.GEOMETRY_SRS_NOT_RD, this.geometry ("geometry").srsName ())
					)
				).shortCircuit ()
			);		
	}
	
	public Validator<MessageKey, Context> getInspireIDValidator () {
		//final String bronhouderCode = job.getBronhouder ().getCode ();
		//final String datasetCode = job.getDatasetType ().getNaam ();
		
		final UnaryCallback<MessageKey, Context, Boolean, String> isUniqueCallback = new UnaryCallback<MessageKey, Context, Boolean, String> () {
			@Override
			public Boolean call (final String input, final Context context) throws Exception {
				final boolean result = !context.hasInspireID (input);
				context.storeInspireID (input);
				return result;
			}
		};
		
		return validate (
			and (
				validate (not (stringAttr ("inspireID").isNull ())).message (MessageKey.INSPIREID_NULL),
				split (stringAttr ("inspireID"), constant ("\\."), validate (
					and (
						validate (eq (intAttr ("length"), constant (4))).message (MessageKey.INSPIREID_PARTS, stringAttr ("inspireID")),
						and (
							validate (eq (stringAttr ("0"), constant ("NL"))).message (MessageKey.INSPIREID_NL, stringAttr ("0")),
							validate (eq (stringAttr ("1"), stringAttr ("bronhouderCode"))).message (MessageKey.INSPIREID_BRONHOUDER, stringAttr ("1"), stringAttr ("bronhouderCode")),
							validate (eq (stringAttr ("2"), stringAttr ("datasetCode"))).message (MessageKey.INSPIREID_DATASET, stringAttr ("2"), stringAttr ("datasetCode")),
							validate (isUUID (stringAttr ("3"))).message (MessageKey.INSPIREID_UUID, stringAttr ("3"))
							
						)
					).shortCircuit ()
				)),
				validate (callback (Boolean.class, stringAttr ("inspireID"), isUniqueCallback)).message (MessageKey.INSPIREID_DUPLICATE, stringAttr ("inspireID"))
			).shortCircuit ()
		);		
	}
	
	// =========================================================================
	// Utilities:
	// =========================================================================
	static String concat (final String... s) {
		if(s == null) {
			return "";
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		
		for(int i = 0; i < s.length; i++) {
			if(i != 0) {
				stringBuilder.append(separator);
			}
			stringBuilder.append(s[i]);
		}
		
		return stringBuilder.toString();
	}
	
	static String concat (final Set<String> s) {
		final ArrayList<String> strings = new ArrayList<String> (s);
		Collections.sort (strings);
		return concat (strings.toArray (new String[0]));
	}
	
	public static String stripAnchor(String string) {
		int anchorLocation = string.indexOf("#");
		if(anchorLocation != -1) {
			return string.substring(0, anchorLocation);
		}
		return string;
	}
	
	protected static String pointToString(Point point) {
		if(point == null) {
			return "?";
		}
		
		StringBuilder stringBuilder = new StringBuilder("(");
		stringBuilder.append(point.get0());
		stringBuilder.append(separator);
		stringBuilder.append(point.get1());
		double p2 = point.get2();
		if(!Double.isNaN(p2)) {
			stringBuilder.append(separator);
			stringBuilder.append(p2);
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}
	
	@SafeVarargs
	static <T> boolean contains(T t, T... array) {
		if(t == null || array == null) {
			return false;
		}
		
		for(T current : array) {
			if(current.equals(t)) {
				return true;
			}
		}
		
		return false;
	}
	
	// =========================================================================
	// Context class:
	// =========================================================================
	public static class Context extends DefaultValidatorContext<MessageKey, Context> {
		private final Set<String> inspireIDs = new HashSet<String> ();
		private final HashMap<String, Long> checkedUrls = new HashMap<String, Long> ();
		private final EtlJob job;
		private final String[] validDesignations;
		
		private boolean flag = false;
		
		private int geometryErrorCount = 0;
		private int errorCount = 0;
		
		public Context (final CodeListFactory codeListFactory, final ValidationReporter<MessageKey, Context> reporter, final EtlJob job, final String[] validDesignations) {
			super (codeListFactory, reporter);
			
			this.validDesignations = validDesignations;
			this.job = job;
		}
		
		public boolean getFlag () {
			return flag;
		}
		
		public void setFlag (final boolean value) {
			flag = value;
		}

		public boolean hasDocument (final String url) {
			return checkedUrls.containsKey (url);
		}
		
		public long getDocumentSize (final String url) {
			final Long value = checkedUrls.get (url);
			return value == null ? 0 : value;
		}
		
		public void storeDocumentSize (final String url, final long size) {
			checkedUrls.put (url, size);
		}
		
		public boolean hasInspireID (final String id) {
			return inspireIDs.contains (id);
		}
		
		public void storeInspireID (final String id) {
			inspireIDs.add (id);
		}

		public int getGeometryErrorCount() {
			return geometryErrorCount;
		}

		public void setGeometryErrorCount(int geometryErrorCount) {
			this.geometryErrorCount = geometryErrorCount;
		}

		public int getErrorCount() {
			return errorCount;
		}

		public void setErrorCount(int errorCount) {
			this.errorCount = errorCount;
		}
		
		public String[] getValidDesignations () {
			return this.validDesignations;
		}
		
		public String getBronhouderCode () {
			return job.getBronhouder ().getCode ();
		}
		
		public String getDatasetCode () {
			return job.getDatasetType().getNaam ();
		}
	}
}
