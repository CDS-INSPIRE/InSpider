package nl.ipo.cds.etl.theme.productioninstallationpart;

import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_CODE_CODESPACE_INVALID;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_CODE_INVALID;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_EMPTY;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_GROUP_INCONSISTENT;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.ATTRIBUTE_NULL;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.GEOMETRY_EMPTY_MULTIGEOMETRY;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.GEOMETRY_ONLY_POINT_OR_MULTIPOINT;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.GEOMETRY_SRS_NOT_RD;
import static nl.ipo.cds.etl.theme.productioninstallationpart.Message.GEOMETRY_SRS_NULL;

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

public class ProductionInstallationPartValidator extends AbstractValidator<ProductionInstallationPart, Message, Context> {

	private final CodeExpression<Message,Context> inspireIdDatasetCode = code ("inspireIdDatasetCode");

	private final AttributeExpression<Message, Context, String> inspireIdLocalId = stringAttr ("inspireIdLocalId");

	private final AttributeExpression<Message, Context, String> productionInstallationId = stringAttr ("productionInstallationId");

	private final GeometryExpression<Message, Context, Geometry> pointGeometry = geometry ("pointGeometry");

	private final AttributeExpression<Message, Context, String> name = stringAttr ("name");

	private final CodeExpression<Message,Context> statusType = code ("statusType");

	private final AttributeExpression<Message, Context, String> statusDescription = stringAttr ("statusDescription");

	private final CodeExpression<Message,Context> type = code ("type");

	private final CodeExpression<Message,Context> technique = code ("technique");

	private final Constant<Message, Context, String> inspireIdDatasetCodeSpace = constant ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallationPart");

	private final Constant<Message, Context, String> statusTypeCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue");

	private final Constant<Message, Context, String> techniqueCodeSpace = constant ("http://inspire.ec.europa.eu/codeList/PollutionAbatementTechniqueValue");

	public ProductionInstallationPartValidator(final Map<Object, Object> validatorMessages) throws CompilerException {
		super(Context.class, ProductionInstallationPart.class, validatorMessages);
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

	public Validator<Message, Context> getProductionInstallationIdValidator () {
		return validate (
			and(
				validate (not (productionInstallationId.isNull ())).message (ATTRIBUTE_NULL, constant(productionInstallationId.name)),
				validate (not (isBlank (productionInstallationId))).message (ATTRIBUTE_EMPTY, constant(productionInstallationId.name))
			).shortCircuit()
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

	public Validator<Message, Context> getNameValidator () {
		return validate (
			ifExp (
				name.isNull (),
	            constant(true),
				validate (not (isBlank (name))).message (ATTRIBUTE_EMPTY, constant(name.name))
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

	public Validator<Message, Context> getTechniqueValidator () {
		return validate (
			ifExp (
				technique.isNull (),
                constant(true),
				and(
					validate (technique.hasCodeSpace (techniqueCodeSpace)).message (ATTRIBUTE_CODE_CODESPACE_INVALID, technique.codeSpace(), constant(technique.name), techniqueCodeSpace),
					validate (not (isBlank (technique.code()))).message (ATTRIBUTE_EMPTY, constant(technique.name)),
					validate (technique.isValid ()).message (ATTRIBUTE_CODE_INVALID, technique.code(), constant(technique.name), techniqueCodeSpace)
				).shortCircuit()
			)
		);
	}
}
