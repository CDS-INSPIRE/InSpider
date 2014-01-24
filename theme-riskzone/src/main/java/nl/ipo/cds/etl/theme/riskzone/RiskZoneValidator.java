package nl.ipo.cds.etl.theme.riskzone;

import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTES_EXCLUSIVE;
import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTE_VALUE_INVALID;
import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTE_VALUE_NEGATIVE;
import static nl.ipo.cds.etl.theme.riskzone.Message.ATTRIBUTE_VALUE_TOO_HIGH;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_DISCONTINUITY;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_EMPTY_MULTIGEOMETRY;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_EXTERIOR_RING_CW;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_INTERIOR_DISCONNECTED;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_INTERIOR_RINGS_TOUCH;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_INTERIOR_RINGS_WITHIN;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_INTERIOR_RING_CCW;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_RING_NOT_CLOSED;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_RING_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.riskzone.Message.GEOMETRY_SRS_NULL;
import static nl.ipo.cds.etl.theme.riskzone.Message.QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_EXCLUSIVE;
import static nl.ipo.cds.etl.theme.riskzone.Message.QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_REQUIRED;

import java.util.Map;

import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.AbstractValidator;
import nl.ipo.cds.validation.AttributeExpression;
import nl.ipo.cds.validation.ValidationReporter;
import nl.ipo.cds.validation.Validator;
import nl.ipo.cds.validation.constants.Constant;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.geometry.GeometryExpression;
import nl.ipo.cds.validation.gml.CodeExpression;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;

import org.deegree.geometry.Geometry;

public class RiskZoneValidator extends AbstractValidator<RiskZone, Message, Context> {

	private final CodeExpression<Message,Context> inspireIdDatasetCode = code ("inspireIdDatasetCode");

	private final AttributeExpression<Message, Context, String> inspireIdLocalId = stringAttr ("inspireIdLocalId");

	private final AttributeExpression<Message, Context, String> hazardAreaId = stringAttr ("hazardAreaId");

	private final AttributeExpression<Message, Context, String> determinationMethod = stringAttr ("determinationMethod");

	private final CodeExpression<Message,Context> typeOfHazardHazardCategory = code ("typeOfHazardHazardCategory");

	private final GeometryExpression<Message, Context, Geometry> geometry = geometry ("geometry");

	private final AttributeExpression<Message, Context, String> likelihoodOfOccurrenceAssessmentMethodName = stringAttr ("likelihoodOfOccurrenceAssessmentMethodName");

	private final AttributeExpression<Message, Context, String> likelihoodOfOccurrenceAssessmentMethodLink = stringAttr ("likelihoodOfOccurrenceAssessmentMethodLink");

	private final AttributeExpression<Message, Context, String> likelihoodOfOccurrenceQualitativeLikelihood = stringAttr ("likelihoodOfOccurrenceQualitativeLikelihood");

	private final AttributeExpression<Message, Context, Double> likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence = doubleAttr( "likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence");

	private final AttributeExpression<Message, Context, Double> likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod = doubleAttr( "likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod");

	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/HazardArea");

	private final Constant<Message, Context, String> typeOfHazardHazardCategoryCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/RiskOrHazardCategoryValue");

	public RiskZoneValidator(final Map<Object, Object> validatorMessages) throws CompilerException {
		super(Context.class, RiskZone.class, validatorMessages);
		compile();
	}

	@Override
	public Context beforeJob(final EtlJob job, final CodeListFactory codeListFactory,
			final ValidationReporter<Message, Context> reporter) {
		return new Context(codeListFactory, reporter);
	}

	public Validator<Message, Context> getInspireIdDatasetCodeValidator () {
		return getNotNullCodeValidator(inspireIdDatasetCode, inspireIdDatasetCodeSpace);
	}

	public Validator<Message, Context> getInspireIdLocalIdValidator () {
		return getNotNullNotEmptyStringValidator(inspireIdLocalId);
	}

	public Validator<Message, Context> getHazardAreaIdValidator () {
		return getNotNullNotEmptyStringValidator(hazardAreaId);
	}

	public Validator<Message, Context> getDeterminationMethodValidator () {
		return validate (
			validate(
				and(
					not (determinationMethod.isNull ()),
					or(
						eq (constant ("modelling"), determinationMethod ),
						eq (constant ("indirectDetermination"), determinationMethod )
					)
				).shortCircuit()
			).message(ATTRIBUTE_VALUE_INVALID, determinationMethod, constant(determinationMethod.name), constant ("modelling, indirectDetermination"))
		);
	}

	public Validator<Message, Context> getTypeOfHazardHazardCategoryValidator () {
		return getNotNullCodeValidator(typeOfHazardHazardCategory, typeOfHazardHazardCategoryCodeSpace);
	}

	public Validator<Message, Context> getGeometryValidator () {
		return getNotNullSurfaceGeometryValidator(geometry);
	}

	public Validator<Message, Context> getLikelihoodOfOccurrenceValidator () {
		return validate (
			and (
				validate (
					not (
						and (
							likelihoodOfOccurrenceQualitativeLikelihood.isNull(),
							and (
								likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence.isNull(),
								likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod.isNull()
							)
						)
					)
				).message(QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_REQUIRED),
				validate (
					not (
						and (
							not (likelihoodOfOccurrenceQualitativeLikelihood.isNull()),
							or (
								not (likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence.isNull()),
								not (likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod.isNull())
							)
						)
					)
				).message(QUALITATIVE_OR_QUANTITATIVE_LIKELIKHOOD_EXCLUSIVE)
			)
		);
	}


	public Validator<Message, Context> getLikelihoodOfOccurrenceAssessmentMethodValidator () {
		return validate (
			or (
				not (likelihoodOfOccurrenceAssessmentMethodName.isNull()),
				likelihoodOfOccurrenceAssessmentMethodLink.isNull()
			)
		).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (likelihoodOfOccurrenceAssessmentMethodName.name), constant (likelihoodOfOccurrenceAssessmentMethodLink.name));
	}

	public Validator<Message, Context> getLikelihoodOfOccurrenceQualitativeLikelihoodValidator () {
		return getNullOrNotEmptyStringValidator(likelihoodOfOccurrenceQualitativeLikelihood);
	}

	public Validator<Message, Context> getLikelihoodOfOccurrenceAssessmentMethodNameValidator () {
		return getNullOrNotEmptyStringValidator(likelihoodOfOccurrenceAssessmentMethodName);
	}

	public Validator<Message, Context> getLikelihoodOfOccurrenceAssessmentMethodLinkValidator () {
		return getNullOrNotEmptyStringValidator(likelihoodOfOccurrenceAssessmentMethodLink);
	}

	public Validator<Message, Context> getLikelihoodOfOccurrenceQuantitativeLikelihoodValidator () {
		return validate (
			not (
				and (
				   not (likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence.isNull()),
				   not (likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod.isNull())
				)
		    )
		).message(ATTRIBUTES_EXCLUSIVE,
			constant(likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence.name),
			constant(likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod.name));
	}

	public Validator<Message, Context> getLikelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrenceValidator () {
		return validate (
			ifExp (
				likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence.isNull (),
				constant (true),
				and (
					validate (gte(likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence, constant(0.0))).message (ATTRIBUTE_VALUE_NEGATIVE, constant (likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence.name)),
					validate (lte(likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence, constant(1.0))).message (ATTRIBUTE_VALUE_TOO_HIGH, constant (likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence.name))
				)
			)
		);
	}

	public Validator<Message, Context> getLikelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriodValidator () {
		return validate (
			ifExp (
				likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod.isNull (),
				constant (true),
				validate (gte(likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod, constant(0.0))).message (ATTRIBUTE_VALUE_NEGATIVE, constant (likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod.name))
			)
		);
	}

	private Validator<Message, Context> getNotNullSurfaceGeometryValidator (GeometryExpression<Message, Context, Geometry> surfaceGeometry) {
		return validate (
			and (
				// The following validations short-circuit, there must be a non-empty, Surface geometry:
				validate (not (geometry.isNull ())).message (ATTRIBUTE_NULL, constant (surfaceGeometry.name)),
				validate (not (surfaceGeometry.isEmptyMultiGeometry())).message (GEOMETRY_EMPTY_MULTIGEOMETRY),
				validate (surfaceGeometry.isSurfaceOrMultiSurface()).message (GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE),
				// Non short-circuited validations:
				and (
					// Short circuit to prevent the interiorDisconnected validation if
					// any of the other validations fail:
					and (
						and (
							validate (not (surfaceGeometry.hasCurveDuplicatePoint ())).message (GEOMETRY_POINT_DUPLICATION, lastLocation ()),
							validate (not (surfaceGeometry.hasCurveDiscontinuity ())).message (GEOMETRY_DISCONTINUITY),
							validate (not (surfaceGeometry.hasCurveSelfIntersection ())).message (GEOMETRY_SELF_INTERSECTION, lastLocation ()),
							validate (not (surfaceGeometry.hasUnclosedRing ())).message (GEOMETRY_RING_NOT_CLOSED),
							validate (not (surfaceGeometry.hasRingSelfIntersection ())).message (GEOMETRY_RING_SELF_INTERSECTION, lastLocation ()),
							validate (not (surfaceGeometry.hasTouchingInteriorRings ())).message(GEOMETRY_INTERIOR_RINGS_TOUCH, lastLocation ()),
							validate (not (surfaceGeometry.hasInteriorRingsWithin ())).message (GEOMETRY_INTERIOR_RINGS_WITHIN)
						),
						validate (not (surfaceGeometry.isInteriorDisconnected ())).message (GEOMETRY_INTERIOR_DISCONNECTED)
					).shortCircuit (),

					// Non-blocking validations:
					validate (not (surfaceGeometry.hasExteriorRingCW ())).nonBlocking ().message (GEOMETRY_EXTERIOR_RING_CW),
					validate (not (surfaceGeometry.hasInteriorRingCCW ())).nonBlocking ().message (GEOMETRY_INTERIOR_RING_CCW),
					validate (not (surfaceGeometry.hasInteriorRingTouchingExterior ())).nonBlocking ().message (GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR, lastLocation ()),
					validate (not (surfaceGeometry.hasInteriorRingOutsideExterior ())).nonBlocking ().message (GEOMETRY_DISCONTINUITY),

					// SRS validations:
					and (
					    validate (surfaceGeometry.hasSrs ()).message (GEOMETRY_SRS_NULL),
					    validate (surfaceGeometry.isSrs (constant ("28992"))).message (GEOMETRY_SRS_NOT_RD, surfaceGeometry.srsName ())
					).shortCircuit()
				)
			).shortCircuit ()
		);
	}

	private Validator<Message, Context> getNotNullCodeValidator (CodeExpression<Message,Context> codeAttr, Constant<Message, Context, String> codeSpace) {
		return validate (
			and(
				validate (not (codeAttr.isNull ())).message (ATTRIBUTE_NULL, constant (codeAttr.name)),
				validate (not (isBlank (codeAttr.code()))).message (ATTRIBUTE_EMPTY, constant (codeAttr.name)),
				validate (codeAttr.hasCodeSpace (codeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, codeAttr.codeSpace(), constant(codeAttr.name), codeSpace),
				validate (codeAttr.isValid ()).message (ATTRIBUTE_CODE_INVALID, codeAttr.code(), constant (codeAttr.name), codeSpace)
			).shortCircuit()
		);
	}

	private Validator<Message, Context> getNotNullNotEmptyStringValidator (AttributeExpression<Message, Context, String> attr) {
		return validate (
			and(
				validate (not (attr.isNull ())).message (ATTRIBUTE_NULL, constant(attr.name)),
				validate (not (isBlank (attr))).message (ATTRIBUTE_EMPTY, constant(attr.name))
			).shortCircuit()
		);
	}

	private Validator<Message, Context> getNullOrNotEmptyStringValidator (AttributeExpression<Message, Context, String> attr) {
		return validate (
			ifExp(
				attr.isNull (),
				constant(true),
				validate (not (isBlank (attr))).message (ATTRIBUTE_EMPTY, constant(attr.name))
			)
		);
	}

}
