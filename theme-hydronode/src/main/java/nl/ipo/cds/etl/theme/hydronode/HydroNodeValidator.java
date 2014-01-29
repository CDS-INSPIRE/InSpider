package nl.ipo.cds.etl.theme.hydronode;

import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_DISCONTINUITY;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_EMPTY_MULTIGEOMETRY;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_EXTERIOR_RING_CW;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_DISCONNECTED;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RINGS_TOUCH;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RINGS_WITHIN;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RING_CCW;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR;
import static nl.ipo.cds.etl.theme.hydronode.Message.GEOMETRY_ONLY_SURFACE_OR_MULTISURFACE;
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

public class HydroNodeValidator extends AbstractValidator<HydroNode, Message, Context> {

	private final CodeExpression<Message,Context> inspireIdDatasetCode = code ("inspireIdDatasetCode");

	private final AttributeExpression<Message, Context, String> inspireIdLocalId = stringAttr ("inspireIdLocalId");

	private final AttributeExpression<Message, Context, String> productionFacilityId = stringAttr ("productionFacilityId");

	private final AttributeExpression<Message, Context, String> thematicIdentifier = stringAttr ("thematicIdentifier");

	private final AttributeExpression<Message, Context, String> thematicIdentifierScheme = stringAttr ("thematicIdentifierScheme");

	private final GeometryExpression<Message, Context, Geometry> geometry = geometry ("geometry");

	private final CodeExpression<Message,Context> functionActivity = code ("functionActivity");

	private final CodeExpression<Message,Context> functionInput = code ("functionInput");

	private final CodeExpression<Message,Context> functionOutput = code ("functionOutput");

	private final AttributeExpression<Message, Context, String> functionDescription = stringAttr ("functionDescription");

	private final AttributeExpression<Message, Context, String> name = stringAttr ("name");

	private final GeometryExpression<Message, Context, Geometry> surfaceGeometry = geometry ("surfaceGeometry");

	private final AttributeExpression<Message, Context, String> statusDescription = stringAttr ("statusDescription");

	private final CodeExpression<Message,Context> statusType = code ("statusType");

	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility");

	private final Constant<Message, Context, String> functionActivityCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/EconomicActivityNACEValue");

	private final Constant<Message, Context, String> functionInputCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/ProductCPAValue");

	private final Constant<Message, Context, String> functionOutputCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/ProductCPAValue");

	private final Constant<Message, Context, String> statusTypeCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue");

	public HydroNodeValidator(final Map<Object, Object> validatorMessages) throws CompilerException {
		super(Context.class, HydroNode.class, validatorMessages);
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

	public Validator<Message, Context> getGeometryValidator () {
		return validate (
			and (
				// The following validations short-circuit, there must be a non-null and non-empty, non-point geometry:
				validate (not (geometry.isNull ())).message (ATTRIBUTE_NULL, constant(geometry.name)),
				// Non short-circuited validations:
				and (
					// Short circuit to prevent the interiorDisconnected validation if
					// any of the other validations fail:
					and (
						and (
							validate (not (geometry.hasCurveDuplicatePoint ())).message (GEOMETRY_POINT_DUPLICATION, lastLocation ()),
							validate (not (geometry.hasCurveDiscontinuity ())).message (GEOMETRY_DISCONTINUITY),
							validate (not (geometry.hasCurveSelfIntersection ())).message (GEOMETRY_SELF_INTERSECTION, lastLocation ()),
							validate (not (geometry.hasUnclosedRing ())).message (GEOMETRY_RING_NOT_CLOSED),
							validate (not (geometry.hasRingSelfIntersection ())).message (GEOMETRY_RING_SELF_INTERSECTION, lastLocation ()),
							validate (not (geometry.hasTouchingInteriorRings ())).message(GEOMETRY_INTERIOR_RINGS_TOUCH, lastLocation ()),
							validate (not (geometry.hasInteriorRingsWithin ())).message (GEOMETRY_INTERIOR_RINGS_WITHIN)
						),
						validate (not (geometry.isInteriorDisconnected ())).message (GEOMETRY_INTERIOR_DISCONNECTED)
					).shortCircuit (),

					// Non-blocking validations:
					validate (not (geometry.hasExteriorRingCW ())).nonBlocking ().message (GEOMETRY_EXTERIOR_RING_CW),
					validate (not (geometry.hasInteriorRingCCW ())).nonBlocking ().message (GEOMETRY_INTERIOR_RING_CCW),
					validate (not (geometry.hasInteriorRingTouchingExterior ())).nonBlocking ().message (GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR, lastLocation ()),
					validate (not (geometry.hasInteriorRingOutsideExterior ())).nonBlocking ().message (GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR),

					// SRS validations:
					and (
					    validate (geometry.hasSrs ()).message (GEOMETRY_SRS_NULL),
					    validate (geometry.isSrs (constant ("28992"))).message (GEOMETRY_SRS_NOT_RD, geometry.srsName ())
					).shortCircuit()
				)
			).shortCircuit ()
		);
	}

	public Validator<Message, Context> getFunctionActivityValidator () {
		return validate (
            and(
        		validate (not (functionActivity.isNull ())).message (ATTRIBUTE_NULL, constant(functionActivity.name)),                		
				validate (functionActivity.hasCodeSpace (functionActivityCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, functionActivity.codeSpace(), constant(functionActivity.name), functionActivityCodeSpace),
				validate (not (isBlank (functionActivity.code()))).message (ATTRIBUTE_EMPTY, constant(functionActivity.name))
				// must be deactivated as long as codelist http://inspire.ec.europa.eu/codeList/EconomicActivityNACEValue is empty
//				validate (functionActivity.isValid ()).message (ATTRIBUTE_CODE_INVALID, functionActivity.code(), constant(functionActivity.name), functionActivityCodeSpace)
		    ).shortCircuit()
		);
	}

	public Validator<Message, Context> getFunctionInputValidator () {
		return validate (
			ifExp (
                functionInput.isNull (),
                constant(true),
				and(
					validate (functionInput.hasCodeSpace (functionInputCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, functionInput.codeSpace(), constant(functionInput.name), functionInputCodeSpace),
					validate (not (isBlank (functionInput.code()))).message (ATTRIBUTE_EMPTY, constant(functionInput.name))
					// must be deactivated as long as codelist http://inspire.ec.europa.eu/codeList/ProductCPAValue is empty					
//					validate (functionInput.isValid ()).message (ATTRIBUTE_CODE_INVALID, functionInput.code(), constant(functionInput.name), functionInputCodeSpace)
				).shortCircuit()
		    )
		);
	}

	public Validator<Message, Context> getFunctionOutputValidator () {
		return validate (
			ifExp (
                functionOutput.isNull (),
                constant(true),
				and(
					validate (functionOutput.hasCodeSpace (functionOutputCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, functionOutput.codeSpace(), constant(functionOutput.name), functionOutputCodeSpace),
					validate (not (isBlank (functionOutput.code()))).message (ATTRIBUTE_EMPTY, constant(functionOutput.name))
					// must be deactivated as long as codelist http://inspire.ec.europa.eu/codeList/ProductCPAValue is empty					
//					validate (functionOutput.isValid ()).message (ATTRIBUTE_CODE_INVALID, functionOutput.code(), constant(functionOutput.name), functionOutputCodeSpace)
				).shortCircuit()
		    )
		);
	}

	public Validator<Message, Context> getFunctionDescriptionValidator () {
		return validate (
			ifExp (
				functionDescription.isNull (),
	            constant(true),
				validate (not (isBlank (functionDescription))).message (ATTRIBUTE_EMPTY, constant(functionDescription.name))
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

	public Validator<Message, Context> getStatusDescriptionValidator () {
		return validate (
			ifExp (
				statusDescription.isNull (),
	            constant(true),
				validate (not (isBlank (statusDescription))).message (ATTRIBUTE_EMPTY, constant(statusDescription.name))
			)
		);
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

}
