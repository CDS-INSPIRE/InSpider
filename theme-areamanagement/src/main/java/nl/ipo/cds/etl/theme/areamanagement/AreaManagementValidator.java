package nl.ipo.cds.etl.theme.areamanagement;

import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_NOT_URL;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_VALUE_TOO_HIGH;
import static nl.ipo.cds.etl.theme.areamanagement.Message.ATTRIBUTE_VALUE_TOO_LOW;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_DISCONTINUITY;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_EXTERIOR_RING_CW;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_INTERIOR_DISCONNECTED;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_INTERIOR_RINGS_TOUCH;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_INTERIOR_RINGS_WITHIN;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_INTERIOR_RING_CCW;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_INTERIOR_RING_OUTSIDE_EXTERIOR;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_INTERIOR_RING_TOUCHES_EXTERIOR;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_POINT_DUPLICATION;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_RING_NOT_CLOSED;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_RING_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_SELF_INTERSECTION;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.areamanagement.Message.GEOMETRY_SRS_NULL;

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

public class AreaManagementValidator extends AbstractValidator<AreaManagement, Message, Context> {

	private final CodeExpression<Message,Context> inspireIdDatasetCode = code ("inspireIdDatasetCode");
	
	private final AttributeExpression<Message, Context, String> inspireIdLocalId = stringAttr ("inspireIdLocalId");
	
	private final GeometryExpression<Message, Context, Geometry> geometry = geometry ("geometry");
	
	private final CodeExpression<Message,Context> zoneTypeCode = code ("zoneTypeCode");
	
	private final CodeExpression<Message,Context> environmentalDomainCode = code ("environmentalDomainCode");
	
	private final AttributeExpression<Message, Context, String> thematicIdIdentifier = stringAttr ("thematicIdIdentifier");
	
	private final AttributeExpression<Message, Context, String> thematicIdIdentifierScheme = stringAttr ("thematicIdIdentifierScheme");
	
	private final AttributeExpression<Message, Context, String> legalBasisName = stringAttr ("legalBasisName");
	
	private final AttributeExpression<Message, Context, String> legalBasisLink = stringAttr ("legalBasisLink");
	
	private final CodeExpression<Message, Context> specialisedZoneTypeCode = code ("specialisedZoneTypeCode");

	private final AttributeExpression<Message, Context, Double> noiseLowValue = doubleAttr("noiseLowValue");
	
	private final AttributeExpression<Message, Context, Double> noiseHighValue = doubleAttr("noiseHighValue");

	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement");
	
	private final Constant<Message, Context, String> zoneTypeCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/ZoneTypeCode");
	
	private final Constant<Message, Context, String> environmentalDomainCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/EnvironmentalDomain");
		
	public AreaManagementValidator(final Map<Object, Object> validatorMessages) throws CompilerException {
		super(Context.class, AreaManagement.class, validatorMessages);
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
				validate (not (geometry.isNull ())).message (ATTRIBUTE_NULL, constant (geometry.name)),
				validate (not (geometry.isEmptyMultiGeometry ())).message (ATTRIBUTE_NULL, constant (geometry.name)),

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

	public Validator<Message, Context> getZoneTypeCodeValidator () {
		return validate (							
			and(
				validate (not (zoneTypeCode.isNull ())).message (ATTRIBUTE_NULL, constant(zoneTypeCode.name)),
				validate (zoneTypeCode.hasCodeSpace (zoneTypeCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, zoneTypeCode.codeSpace(), constant(zoneTypeCode.name), zoneTypeCodeSpace),
				validate (not (isBlank (zoneTypeCode.code()))).message (ATTRIBUTE_EMPTY, constant(zoneTypeCode.name)),				
				validate (zoneTypeCode.isValid ()).message (ATTRIBUTE_CODE_INVALID, zoneTypeCode.code(), constant(zoneTypeCode.name), zoneTypeCodeSpace)
			).shortCircuit()
		);
	}

	public Validator<Message, Context> getEnvironmentalDomainCodeValidator () {
		return validate (							
			and(
				validate (not (environmentalDomainCode.isNull ())).message (ATTRIBUTE_NULL, constant(environmentalDomainCode.name)),
				validate (environmentalDomainCode.hasCodeSpace (environmentalDomainCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, environmentalDomainCode.codeSpace(), constant(environmentalDomainCode.name), constant (environmentalDomainCodeSpace.value)),
				validate (not (isBlank (environmentalDomainCode.code()))).message (ATTRIBUTE_EMPTY, constant(environmentalDomainCode.name)),				
				validate (environmentalDomainCode.isValid ()).message (ATTRIBUTE_CODE_INVALID, environmentalDomainCode.code(), constant(environmentalDomainCode.name), environmentalDomainCodeSpace)
			).shortCircuit()
		);
	}

	public Validator<Message, Context> getThematicIdIdentifierValidator () {
		return validate (
			and(
			    ifExp (not (or (thematicIdIdentifier.isNull (), isBlank (thematicIdIdentifier))), 
			    	and (
			    	   validate (not (thematicIdIdentifierScheme.isNull())).message (ATTRIBUTE_GROUP_INCONSISTENT, constant (thematicIdIdentifierScheme.name), constant (thematicIdIdentifier.name)),
			    	   validate (not (isBlank (thematicIdIdentifierScheme))).message (ATTRIBUTE_GROUP_INCONSISTENT, constant (thematicIdIdentifierScheme.name), constant (thematicIdIdentifier.name))
			    	).shortCircuit(),
			    	constant(true)
			    ),
			    ifExp (not (or (thematicIdIdentifierScheme.isNull (), isBlank (thematicIdIdentifierScheme))),
			    	and (
			    	   validate (not (thematicIdIdentifier.isNull())).message (ATTRIBUTE_GROUP_INCONSISTENT, constant (thematicIdIdentifier.name), constant (thematicIdIdentifierScheme.name)),
			    	   validate (not (isBlank (thematicIdIdentifier))).message (ATTRIBUTE_GROUP_INCONSISTENT, constant (thematicIdIdentifier.name), constant (thematicIdIdentifierScheme.name))
			    	).shortCircuit(),
			    	constant(true)
			    )	        
		    )
		);
	}

	public Validator<Message, Context> getLegalBasisNameValidator () {
		return validate (		
			ifExp (
				legalBasisName.isNull(),
				constant(true),
				validate (not (isBlank (legalBasisName))).message (ATTRIBUTE_EMPTY, constant(legalBasisName.name))
			)
		);
	}	

	public Validator<Message, Context> getLegalBasisLinkValidator () {
		return validate (							
			or(
				legalBasisLink.isNull (),
				and (
					not (isBlank(legalBasisLink)),
				    isUrl(legalBasisLink)
				).shortCircuit()
			)
		).message(ATTRIBUTE_NOT_URL, legalBasisLink, constant (legalBasisLink.name));
	}	

	public Validator<Message, Context> getSpecialisedZoneTypeCodeValidator () {
		return validate (
			ifExp (
				specialisedZoneTypeCode.isNull(),
				constant(true),
			    validate (not (isBlank (specialisedZoneTypeCode.code()))).message (ATTRIBUTE_EMPTY, constant (specialisedZoneTypeCode.name))			
		    )
	    );
	}

	public Validator<Message, Context> getNoiseLowValueValidator () {
		return validate (
			ifExp (
				noiseLowValue.isNull(),
				constant(true),
				and (
			        validate (gte(noiseLowValue, constant(0.0))).message (ATTRIBUTE_VALUE_TOO_LOW, noiseLowValue, constant (noiseLowValue.name), constant(0.0)),
			        validate (lte(noiseLowValue, constant(150.0))).message (ATTRIBUTE_VALUE_TOO_HIGH, noiseLowValue, constant (noiseLowValue.name), constant(150.0))
			    )
		    )
	    );
	}

	public Validator<Message, Context> getNoiseHighValueValidator () {
		return validate (
			ifExp (
				noiseHighValue.isNull(),
				constant(true),
				and (
			        validate (gte(noiseHighValue, constant(0.0))).message (ATTRIBUTE_VALUE_TOO_LOW, noiseHighValue, constant (noiseHighValue.name), constant(0.0)),
			        validate (lte(noiseHighValue, constant(150.0))).message (ATTRIBUTE_VALUE_TOO_HIGH, noiseHighValue, constant (noiseHighValue.name), constant(150.0))
			    )
		    )
	    );
	}	
	
}
