package nl.ipo.cds.etl.theme.hydronode;

import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_DISCONTINUITY;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_EXTERIOR_RING_CW;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_DISCONNECTED;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RINGS_TOUCH;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RINGS_WITHIN;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RING_CCW;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_RING_NOT_CLOSED;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_RING_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_SRS_NULL;

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

public class HydroNodeValidator extends
		AbstractValidator<HydroNode, Message, Context> {

	private final CodeExpression<Message, Context> inspireIdDatasetCode = code("inspireIdDatasetCode");

	private final AttributeExpression<Message, Context, String> inspireIdLocalId = stringAttr("inspireIdLocalId");

	private final GeometryExpression<Message, Context, Geometry> geometry = geometry("geometry");

	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility");

	public HydroNodeValidator(final Map<Object, Object> validatorMessages)
			throws CompilerException {
		super(Context.class, HydroNode.class, validatorMessages);
		compile();
	}

	@Override
	public Context beforeJob(final EtlJob job,
			final CodeListFactory codeListFactory,
			final ValidationReporter<Message, Context> reporter) {
		return new Context(codeListFactory, reporter);
	}

	public Validator<Message, Context> getInspireIdDatasetCodeValidator() {
		return validate(and(
				validate(not(inspireIdDatasetCode.isNull())).message(
						ATTRIBUTE_NULL, constant(inspireIdDatasetCode.name)),
				validate(not(isBlank(inspireIdDatasetCode.code()))).message(
						ATTRIBUTE_EMPTY, constant(inspireIdDatasetCode.name)),
				validate(
						inspireIdDatasetCode
								.hasCodeSpace(inspireIdDatasetCodeSpace))
						.message(ATTRIBUTE_CODE_CODESPACE_INVALID,
								inspireIdDatasetCode.codeSpace(),
								constant(inspireIdDatasetCode.name),
								inspireIdDatasetCodeSpace),
				validate(inspireIdDatasetCode.isValid()).message(
						ATTRIBUTE_CODE_INVALID, inspireIdDatasetCode.code(),
						constant(inspireIdDatasetCode.name),
						inspireIdDatasetCodeSpace)).shortCircuit());
	}

	public Validator<Message, Context> getInspireIdLocalIdValidator() {
		return validate(and(
				validate(not(inspireIdLocalId.isNull())).message(
						ATTRIBUTE_NULL, constant(inspireIdLocalId.name)),
				validate(not(isBlank(inspireIdLocalId))).message(
						ATTRIBUTE_EMPTY, constant(inspireIdLocalId.name)))
				.shortCircuit());
	}

	public Validator<Message, Context> getGeometryValidator() {
		return validate(and(
		// The following validations short-circuit, there must be a non-null and
		// non-empty, non-point geometry:
				validate(not(geometry.isNull())).message(ATTRIBUTE_NULL,
						constant(geometry.name)),
				// Non short-circuited validations:
				and(
				// Short circuit to prevent the interiorDisconnected validation
				// if
				// any of the other validations fail:
				and(
						and(validate(not(geometry.hasCurveDuplicatePoint()))
								.message(GEOMETRY_POINT_DUPLICATION,
										lastLocation()),
								validate(not(geometry.hasCurveDiscontinuity()))
										.message(GEOMETRY_DISCONTINUITY),
								validate(
										not(geometry.hasCurveSelfIntersection()))
										.message(GEOMETRY_SELF_INTERSECTION,
												lastLocation()),
								validate(not(geometry.hasUnclosedRing()))
										.message(GEOMETRY_RING_NOT_CLOSED),
								validate(
										not(geometry.hasRingSelfIntersection()))
										.message(
												GEOMETRY_RING_SELF_INTERSECTION,
												lastLocation()),
								validate(
										not(geometry.hasTouchingInteriorRings()))
										.message(GEOMETRY_INTERIOR_RINGS_TOUCH,
												lastLocation()),
								validate(not(geometry.hasInteriorRingsWithin()))
										.message(GEOMETRY_INTERIOR_RINGS_WITHIN)),
						validate(not(geometry.isInteriorDisconnected()))
								.message(GEOMETRY_INTERIOR_DISCONNECTED))
						.shortCircuit(),

						// Non-blocking validations:
						validate(not(geometry.hasExteriorRingCW()))
								.nonBlocking().message(
										GEOMETRY_EXTERIOR_RING_CW),
						validate(not(geometry.hasInteriorRingCCW()))
								.nonBlocking().message(
										GEOMETRY_INTERIOR_RING_CCW),
						validate(
								not(geometry.hasInteriorRingTouchingExterior()))
								.nonBlocking()
								.message(
										GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR,
										lastLocation()),
						validate(not(geometry.hasInteriorRingOutsideExterior()))
								.nonBlocking()
								.message(
										GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR),

						// SRS validations:
						and(
								validate(geometry.hasSrs()).message(
										GEOMETRY_SRS_NULL),
								validate(geometry.isSrs(constant("28992")))
										.message(GEOMETRY_SRS_NOT_RD,
												geometry.srsName()))
								.shortCircuit())).shortCircuit());
	}

}
