package nl.ipo.cds.etl.theme.hydronode;

import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.hydronode.Message.ATTRIBUTE_NULL;
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

	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/HydroNode");

	private final AttributeExpression<Message, Context, String> name = stringAttr ("name");
	
	private final CodeExpression<Message,Context> category = code ("category");
	
	private final Constant<Message, Context, String> categoryCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/HydroNodeCategoryValue");	
	
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

	public Validator<Message, Context> getInspireIdDatasetCodeValidator () {
		return validate (
			and(
				validate (not (inspireIdDatasetCode.isNull ())).message (ATTRIBUTE_NULL, constant (inspireIdDatasetCode.name)),
				validate (not (isBlank (inspireIdDatasetCode.code()))).message (ATTRIBUTE_EMPTY, constant (inspireIdDatasetCode.name)),
				validate (inspireIdDatasetCode.hasCodeSpace (inspireIdDatasetCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, inspireIdDatasetCode.codeSpace(), constant(inspireIdDatasetCode.name), inspireIdDatasetCodeSpace)
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
				// SRS validations:
				and (
				    validate (geometry.hasSrs ()).message (GEOMETRY_SRS_NULL),
				    validate (geometry.isSrs (constant ("28992"))).message (GEOMETRY_SRS_NOT_RD, geometry.srsName ())
				).shortCircuit()
			).shortCircuit ()
		);
	}

	public Validator<Message, Context> getNameValidator () {
		return validate (
			and(
				validate (not (name.isNull ())).message (ATTRIBUTE_NULL, constant(name.name)),
				validate (not (isBlank (name))).message (ATTRIBUTE_EMPTY, constant(name.name))
			).shortCircuit()
		);
	}
	
	public Validator<Message, Context> getCategoryValidator () {
		return validate (
			ifExp (
                category.isNull (),
                constant(true),
				and(
					validate (category.hasCodeSpace (categoryCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, category.codeSpace(), constant(category.name), categoryCodeSpace),
					validate (not (isBlank (category.code()))).message (ATTRIBUTE_EMPTY, constant(category.name))
					// must be deactivated as long as codelist http://inspire.ec.europa.eu/codeList/ProductCPAValue is empty					
//					validate (functionInput.isValid ()).message (ATTRIBUTE_CODE_INVALID, functionInput.code(), constant(functionInput.name), functionInputCodeSpace)
				).shortCircuit()
		    )
		);
	}
	
}
