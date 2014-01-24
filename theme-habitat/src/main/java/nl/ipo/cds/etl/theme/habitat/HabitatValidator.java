package nl.ipo.cds.etl.theme.habitat;

import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.habitat.Message.ATTRIBUTE_VALUE_NEGATIVE;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_DISCONTINUITY;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_EXTERIOR_RING_CW;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_INTERIOR_DISCONNECTED;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_INTERIOR_RINGS_TOUCH;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_INTERIOR_RINGS_WITHIN;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_INTERIOR_RING_CCW;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_RING_NOT_CLOSED;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_RING_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.habitat.Message.GEOMETRY_SRS_NULL;

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

public class HabitatValidator extends AbstractValidator<Habitat, Message, Context> {

	private final CodeExpression<Message,Context> inspireIdDatasetCode = code ("inspireIdDatasetCode");
	
	private final AttributeExpression<Message, Context, String> inspireIdLocalId = stringAttr ("inspireIdLocalId");

	private final GeometryExpression<Message, Context, Geometry> geometry = geometry ("geometry");	

	private final CodeExpression<Message,Context> habitatReferenceHabitatTypeIdCode = code ("habitatReferenceHabitatTypeIdCode");
	
	private final CodeExpression<Message,Context> habitatReferenceHabitatTypeSchemeCode = code ("habitatReferenceHabitatTypeSchemeCode");

	private final AttributeExpression<Message, Context, String> localHabitatNameLocalScheme = stringAttr ("localHabitatNameLocalScheme");

	private final CodeExpression<Message,Context> localHabitatNameLocalNameCode = code ("localHabitatNameLocalNameCode");

	private final AttributeExpression<Message, Context, String> localHabitatNameLocalName = stringAttr ("localHabitatNameLocalName");	

	private final CodeExpression<Message,Context> localHabitatNameQualifierLocalName = code ("localHabitatNameQualifierLocalName");	
	
	private final AttributeExpression<Message, Context, Double> habitatAreaCovered = doubleAttr ("habitatAreaCovered");
		
	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat");
	
	private final Constant<Message, Context, String> referenceHabitatTypeSchemeCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue");

	private final Constant<Message, Context, String> localHabitatNameQualifierLocalNameCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue");

	
	public HabitatValidator(final Map<Object, Object> validatorMessages) throws CompilerException {
		super(Context.class, Habitat.class, validatorMessages);
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
				validate (inspireIdDatasetCode.hasCodeSpace (inspireIdDatasetCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, inspireIdDatasetCode.codeSpace(), constant(inspireIdDatasetCode.name), inspireIdDatasetCodeSpace),
				validate (not (isBlank (inspireIdDatasetCode.code()))).message (ATTRIBUTE_EMPTY, constant (inspireIdDatasetCode.name)),				
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
						validate (not (this.geometry ("geometry").isInteriorDisconnected ())).message (GEOMETRY_INTERIOR_DISCONNECTED)
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
	
	public Validator<Message, Context> getHabitatReferenceHabitatTypeIdCodeValidator () {
		return validate (							
			and(
				validate (not (habitatReferenceHabitatTypeIdCode.isNull ())).message (ATTRIBUTE_NULL, constant(habitatReferenceHabitatTypeIdCode.name)),
				validate (not (isBlank (habitatReferenceHabitatTypeIdCode.code()))).message (ATTRIBUTE_EMPTY, constant(habitatReferenceHabitatTypeIdCode.name))
			).shortCircuit()
		);
	}	

	public Validator<Message, Context> getHabitatReferenceHabitatTypeSchemeCode () {
		return validate (							
			and(
				validate (not (habitatReferenceHabitatTypeSchemeCode.isNull ())).message (ATTRIBUTE_NULL, constant(habitatReferenceHabitatTypeSchemeCode.name)),
				validate (habitatReferenceHabitatTypeSchemeCode.hasCodeSpace (referenceHabitatTypeSchemeCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, habitatReferenceHabitatTypeSchemeCode.codeSpace(), constant(habitatReferenceHabitatTypeSchemeCode.name), constant (referenceHabitatTypeSchemeCodeSpace.value)),
				validate (not (isBlank (habitatReferenceHabitatTypeSchemeCode.code()))).message (ATTRIBUTE_EMPTY, constant(habitatReferenceHabitatTypeSchemeCode.name)),				
				validate (habitatReferenceHabitatTypeSchemeCode.isValid ()).message (ATTRIBUTE_CODE_INVALID, habitatReferenceHabitatTypeSchemeCode.code(), constant(habitatReferenceHabitatTypeSchemeCode.name), constant (referenceHabitatTypeSchemeCodeSpace.value))
			).shortCircuit()
		);
	}

	public Validator<Message, Context> getLocalHabitatNameLocalSchemeValidator () {
		return validate (
			ifExp (
				localHabitatNameLocalScheme.isNull(),
				and (
					validate (localHabitatNameLocalNameCode.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalScheme.name), constant (localHabitatNameLocalNameCode.name)),
					validate (localHabitatNameLocalName.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalScheme.name), constant (localHabitatNameLocalName.name)),
					validate (localHabitatNameQualifierLocalName.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalScheme.name), constant (localHabitatNameQualifierLocalName.name))
				).shortCircuit(),
				validate (not (isBlank (localHabitatNameLocalScheme))).message (ATTRIBUTE_EMPTY, constant(localHabitatNameLocalScheme.name))
			)
		);
	}
	
	public Validator<Message, Context> getLocalHabitatNameLocalNameCodeValidator () {
		return validate (
			ifExp (
				localHabitatNameLocalNameCode.isNull(),
				and (
					validate (localHabitatNameLocalScheme.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalNameCode.name), constant (localHabitatNameLocalScheme.name)),
					validate (localHabitatNameLocalName.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalNameCode.name), constant (localHabitatNameLocalName.name)),
					validate (localHabitatNameQualifierLocalName.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalNameCode.name), constant (localHabitatNameQualifierLocalName.name))
				).shortCircuit(),
				validate (not (isBlank (localHabitatNameLocalNameCode.code()))).message (ATTRIBUTE_EMPTY, constant(localHabitatNameLocalNameCode.name))
			)
		);
	}
	
	public Validator<Message, Context> getLocalHabitatNameLocalNameValidator () {
		return validate (
			ifExp (
				localHabitatNameLocalName.isNull(),
				and (
					validate (localHabitatNameLocalScheme.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalName.name), constant (localHabitatNameLocalScheme.name)),
					validate (localHabitatNameLocalNameCode.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalName.name), constant (localHabitatNameLocalNameCode.name)),
					validate (localHabitatNameQualifierLocalName.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameLocalName.name), constant (localHabitatNameQualifierLocalName.name))
				).shortCircuit(),
				validate (not (isBlank (localHabitatNameLocalName))).message (ATTRIBUTE_EMPTY, constant(localHabitatNameLocalName.name))
			)
		);
	}
	
	public Validator<Message, Context> getLocalHabitatNameQualifierLocalName() {
		return validate (
			ifExp (
				localHabitatNameQualifierLocalName.isNull(),
				and (
					validate (localHabitatNameLocalScheme.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameQualifierLocalName.name), constant (localHabitatNameLocalScheme.name)),
					validate (localHabitatNameLocalNameCode.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameQualifierLocalName.name), constant (localHabitatNameLocalNameCode.name)),
					validate (localHabitatNameLocalName.isNull()).message(ATTRIBUTE_GROUP_INCONSISTENT, constant (localHabitatNameQualifierLocalName.name), constant (localHabitatNameLocalName.name))
				).shortCircuit(),
				and (
					validate (localHabitatNameQualifierLocalName.hasCodeSpace (localHabitatNameQualifierLocalNameCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, localHabitatNameQualifierLocalName.codeSpace(), constant(localHabitatNameQualifierLocalName.name), constant (localHabitatNameQualifierLocalNameCodeSpace.value)),
					validate (not (isBlank(localHabitatNameQualifierLocalName.code()))).message (ATTRIBUTE_EMPTY, constant(localHabitatNameQualifierLocalName.name)),
					validate (localHabitatNameQualifierLocalName.isValid ()).message (ATTRIBUTE_CODE_INVALID, localHabitatNameQualifierLocalName.code(), constant(localHabitatNameQualifierLocalName.name), constant (localHabitatNameQualifierLocalNameCodeSpace.value))
			    ).shortCircuit()
			)
		);
	}

	public Validator<Message, Context> getHabitatAreaCoveredValidator () {
		return validate (
			ifExp (
				habitatAreaCovered.isNull(),
				constant (true),
				validate (gte(habitatAreaCovered, constant(0.0))).message (ATTRIBUTE_VALUE_NEGATIVE, habitatAreaCovered, constant (habitatAreaCovered.name))
			)
	    );
	}	
	
}
