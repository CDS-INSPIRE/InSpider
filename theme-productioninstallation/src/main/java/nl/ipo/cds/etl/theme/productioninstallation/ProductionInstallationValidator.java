package nl.ipo.cds.etl.theme.productioninstallation;

import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_DISCONTINUITY;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_EMPTY_MULTIGEOMETRY;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_EXTERIOR_RING_CW;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_INTERIOR_DISCONNECTED;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_INTERIOR_RINGS_TOUCH;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_INTERIOR_RINGS_WITHIN;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_INTERIOR_RING_CCW;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_ONLY_CURVE_OR_MULTICURVE;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_ONLY_POINT_OR_MULTIPOINT;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_RING_NOT_CLOSED;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_RING_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.productioninstallation.Message.GEOMETRY_SRS_NULL;

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

public class ProductionInstallationValidator extends AbstractValidator<ProductionInstallation, Message, Context> {

	private final CodeExpression<Message,Context> inspireIdDatasetCode = code ("inspireIdDatasetCode");

	private final AttributeExpression<Message, Context, String> inspireIdLocalId = stringAttr ("inspireIdLocalId");

	private final AttributeExpression<Message, Context, String> productionFacilityId = stringAttr ("productionFacilityId");

	private final AttributeExpression<Message, Context, String> productionInstallationId = stringAttr ("productionInstallationId");

	private final AttributeExpression<Message, Context, String> thematicIdentifier = stringAttr ("thematicIdentifier");

	private final AttributeExpression<Message, Context, String> thematicIdentifierScheme = stringAttr ("thematicIdentifierScheme");

	private final GeometryExpression<Message, Context, Geometry> pointGeometry = geometry ("pointGeometry");

	private final GeometryExpression<Message, Context, Geometry> surfaceGeometry = geometry ("surfaceGeometry");

	private final GeometryExpression<Message, Context, Geometry> lineGeometry = geometry ("lineGeometry");

	private final AttributeExpression<Message, Context, String> name = stringAttr ("name");

	private final AttributeExpression<Message, Context, String> description = stringAttr ("description");

	private final CodeExpression<Message,Context> statusType = code ("statusType");

	private final AttributeExpression<Message, Context, String> statusDescription = stringAttr ("statusDescription");

	private final CodeExpression<Message,Context> type = code ("type");

	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallation");

	private final Constant<Message, Context, String> statusTypeCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue");

	public ProductionInstallationValidator(final Map<Object, Object> validatorMessages) throws CompilerException {
		super(Context.class, ProductionInstallation.class, validatorMessages);
		compile();
	}

	@Override
	public Context beforeJob(final EtlJob job, final CodeListFactory codeListFactory,
			final ValidationReporter<Message, Context> reporter) {
		return new Context(codeListFactory, reporter);
	}

	public Validator<Message, Context> getInspireIdDatasetCodeValidator () {
		return validate (
			and(
				validate (not (inspireIdDatasetCode.isNull ())).message (ATTRIBUTE_NULL, constant (inspireIdDatasetCode.name)),
				validate (not (isBlank (inspireIdDatasetCode.code()))).message (ATTRIBUTE_EMPTY, constant (inspireIdDatasetCode.name)),
				validate (inspireIdDatasetCode.hasCodeSpace (inspireIdDatasetCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, inspireIdDatasetCode.codeSpace(), constant(inspireIdDatasetCode.name), inspireIdDatasetCodeSpace),
				validate (inspireIdDatasetCode.isValid ()).message (ATTRIBUTE_CODE_INVALID, inspireIdDatasetCode.code(), constant (inspireIdDatasetCode.name), inspireIdDatasetCodeSpace)
			).shortCircuit()
		);
	}

	public Validator<Message, Context> getInspireIdLocalIdValidator () {
		return validate (
			and(
				validate (not (inspireIdLocalId.isNull ())).message (ATTRIBUTE_NULL, constant(inspireIdLocalId.name)),
				validate (not (isBlank (inspireIdLocalId))).message (ATTRIBUTE_EMPTY, constant(inspireIdLocalId.name))
			).shortCircuit()
		);
	}

	public Validator<Message, Context> getProductionFacilityIdValidator () {
		return validate (
			and(
				validate (not (productionFacilityId.isNull ())).message (ATTRIBUTE_NULL, constant(productionFacilityId.name)),
				validate (not (isBlank (productionFacilityId))).message (ATTRIBUTE_EMPTY, constant(productionFacilityId.name))
			).shortCircuit()
		);
	}

	public Validator<Message, Context> getProductionInstallationIdValidator () {
		return validate (
			and(
				validate (not (productionInstallationId.isNull ())).message (ATTRIBUTE_NULL, constant(productionInstallationId.name)),
				validate (not (isBlank (productionInstallationId))).message (ATTRIBUTE_EMPTY, constant(productionInstallationId.name))
			).shortCircuit()
		);
	}

	public Validator<Message, Context> getThematicIdentifierValidator () {
		return validate (
			and(
			    ifExp (not (or (thematicIdentifier.isNull (), isBlank (thematicIdentifier))),
			    	and (
			    	   validate (not (thematicIdentifierScheme.isNull())).message (ATTRIBUTE_GROUP_INCONSISTENT, constant (thematicIdentifierScheme.name), constant (thematicIdentifier.name)),
			    	   validate (not (isBlank (thematicIdentifierScheme))).message (ATTRIBUTE_GROUP_INCONSISTENT, constant (thematicIdentifierScheme.name), constant (thematicIdentifier.name))
			    	).shortCircuit(),
			    	constant(true)
			    ),
			    ifExp (not (or (thematicIdentifierScheme.isNull (), isBlank (thematicIdentifierScheme))),
			    	and (
			    	   validate (not (thematicIdentifier.isNull())).message (ATTRIBUTE_GROUP_INCONSISTENT, constant (thematicIdentifier.name), constant (thematicIdentifierScheme.name)),
			    	   validate (not (isBlank (thematicIdentifier))).message (ATTRIBUTE_GROUP_INCONSISTENT, constant (thematicIdentifier.name), constant (thematicIdentifierScheme.name))
			    	).shortCircuit(),
			    	constant(true)
			    )
		    )
		);
	}

	public Validator<Message, Context> getPointGeometryValidator () {
		return validate (
			ifExp(
				pointGeometry.isNull (),
				constant(true),
				and (
					// The following validations short-circuit, there must be a non-empty, point-valued geometry
					validate (not (pointGeometry.isEmptyMultiGeometry())).message (GEOMETRY_EMPTY_MULTIGEOMETRY),
					validate (pointGeometry.isPointOrMultiPoint()).message (GEOMETRY_ONLY_POINT_OR_MULTIPOINT),
					// SRS validations:
					and (
					    validate (pointGeometry.hasSrs ()).message (GEOMETRY_SRS_NULL),
					    validate (pointGeometry.isSrs (constant ("28992"))).message (GEOMETRY_SRS_NOT_RD, pointGeometry.srsName ())
					).shortCircuit()
				).shortCircuit ()
			)
		);
	}

	public Validator<Message, Context> getSurfaceGeometryValidator () {
		return validate (
			ifExp(
				surfaceGeometry.isNull (),
				constant(true),
				and (
					// The following validations short-circuit, there must be a non-empty, Surface geometry:
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
						validate (not (surfaceGeometry.hasInteriorRingOutsideExterior ())).nonBlocking ().message (GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR),

						// SRS validations:
						and (
						    validate (surfaceGeometry.hasSrs ()).message (GEOMETRY_SRS_NULL),
						    validate (surfaceGeometry.isSrs (constant ("28992"))).message (GEOMETRY_SRS_NOT_RD, surfaceGeometry.srsName ())
						).shortCircuit()
					)
				).shortCircuit ()
			)
		);
	}

	public Validator<Message, Context> getLineGeometryValidator () {
		return validate (
			ifExp(
				lineGeometry.isNull (),
				constant(true),
				and (
					// The following validations short-circuit, there must be a non-null and non-empty, non-point geometry:
					validate (not (lineGeometry.isEmptyMultiGeometry())).message (GEOMETRY_EMPTY_MULTIGEOMETRY),
					validate (lineGeometry.isCurveOrMultiCurve()).message (GEOMETRY_ONLY_CURVE_OR_MULTICURVE),
					// Non short-circuited validations:
					and (
						// Short circuit to prevent the interiorDisconnected validation if
						// any of the other validations fail:
						and (
							validate (not (lineGeometry.hasCurveDuplicatePoint ())).message (GEOMETRY_POINT_DUPLICATION, lastLocation ()),
							validate (not (lineGeometry.hasCurveDiscontinuity ())).message (GEOMETRY_DISCONTINUITY),
							validate (not (lineGeometry.hasCurveSelfIntersection ())).message (GEOMETRY_SELF_INTERSECTION, lastLocation ())
						),
						// SRS validations:
						and (
						    validate (lineGeometry.hasSrs ()).message (GEOMETRY_SRS_NULL),
						    validate (lineGeometry.isSrs (constant ("28992"))).message (GEOMETRY_SRS_NOT_RD, lineGeometry.srsName ())
						).shortCircuit()
					)
				).shortCircuit ()
			)
		);
	}

	public Validator<Message, Context> getNameValidator () {
		return validate (
			ifExp (
				name.isNull (),
	            constant(true),
				validate (not (isBlank (name))).message (ATTRIBUTE_EMPTY, constant(name.name))
			)
		);
	}

	public Validator<Message, Context> getDescriptionValidator () {
		return validate (
			ifExp (
				description.isNull (),
	            constant(true),
				validate (not (isBlank (description))).message (ATTRIBUTE_EMPTY, constant(description.name))
			)
		);
	}

	public Validator<Message, Context> getStatusValidator () {
		return validate (
			not (
				and (
					statusType.isNull(),
					not (statusDescription.isNull())
				)
			)
		).message (ATTRIBUTE_GROUP_INCONSISTENT, constant(statusType.name), constant(statusDescription.name));
	}

	public Validator<Message, Context> getStatusTypeValidator () {
		return validate (
			ifExp (
				statusType.isNull (),
	            constant(true),
				and(
					validate (statusType.hasCodeSpace (statusTypeCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, statusType.codeSpace(), constant(statusType.name), statusTypeCodeSpace),
					validate (not (isBlank (statusType.code()))).message (ATTRIBUTE_EMPTY, constant(statusType.name)),
					validate (statusType.isValid ()).message (ATTRIBUTE_CODE_INVALID, statusType.code(), constant(statusType.name), statusTypeCodeSpace)
				).shortCircuit()
			)
		);
	}

	public Validator<Message, Context> getStatusDescriptionValidator () {
		return validate (
			ifExp (
				statusDescription.isNull (),
	            constant(true),
				validate (not (isBlank (statusDescription))).message (ATTRIBUTE_EMPTY, constant(statusDescription.name))
			)
		);
	}

	public Validator<Message, Context> getTypeValidator () {
		return validate (
			ifExp (
                type.isNull (),
                constant(true),
                validate (not (isBlank (type.code()))).message (ATTRIBUTE_EMPTY, constant(type.name))
		    )
		);
	}

}
