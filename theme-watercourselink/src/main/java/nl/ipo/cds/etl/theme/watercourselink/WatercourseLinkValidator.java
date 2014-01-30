package nl.ipo.cds.etl.theme.watercourselink;

import static nl.ipo.cds.etl.theme.watercourselink.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.watercourselink.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.watercourselink.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.watercourselink.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.watercourselink.Message.GEOMETRY_SRS_NULL;

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

public class WatercourseLinkValidator extends AbstractValidator<WatercourseLink, Message, Context> {

	private final CodeExpression<Message,Context> inspireIdDatasetCode = code ("inspireIdDatasetCode");

	private final AttributeExpression<Message, Context, String> inspireIdLocalId = stringAttr ("inspireIdLocalId");

	private final GeometryExpression<Message, Context, Geometry> geometry = geometry ("geometry");

	private final AttributeExpression<Message, Context, String> name = stringAttr ("name");
	
	private final AttributeExpression<Message, Context, String> endNodeLocalId = stringAttr ("endNodeLocalId");
	
	private final AttributeExpression<Message, Context, String> startNodeLocalId = stringAttr ("startNodeLocalId");

	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/WatercourseLink");

	public WatercourseLinkValidator(final Map<Object, Object> validatorMessages) throws CompilerException {
		super(Context.class, WatercourseLink.class, validatorMessages);
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

	public Validator<Message, Context> getEndNodeLocalIdValidator () {
		return validate (
			and(
				validate (not (endNodeLocalId.isNull ())).message (ATTRIBUTE_NULL, constant(endNodeLocalId.name)),
				validate (not (isBlank (endNodeLocalId))).message (ATTRIBUTE_EMPTY, constant(endNodeLocalId.name))
			).shortCircuit()
		);
	}

	public Validator<Message, Context> getStartNodeLocalIdValidator () {
		return validate (
			and(
				validate (not (startNodeLocalId.isNull ())).message (ATTRIBUTE_NULL, constant(startNodeLocalId.name)),
				validate (not (isBlank (startNodeLocalId))).message (ATTRIBUTE_EMPTY, constant(startNodeLocalId.name))
			).shortCircuit()
		);
	}	
	
}
