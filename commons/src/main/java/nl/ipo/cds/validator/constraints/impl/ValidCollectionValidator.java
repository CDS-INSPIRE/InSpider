/**
 * 
 */
package nl.ipo.cds.validator.constraints.impl;

import java.util.List;

import javax.validation.Constraint;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import nl.ipo.cds.validator.constraints.ValidCollection;

import org.apache.commons.lang.ArrayUtils;
/**
 * @author eshuism
 * 6 feb 2012
 * 
 * NOTE: Known issue: Validator is not able to identify which entry in the Collection is invalid.
 * Cause of this that within the validator the error-path (property-path) is not accessible
 */
public class ValidCollectionValidator implements ConstraintValidator<ValidCollection , List<?>> {

	private Class<?>[] constraints;
	private String[] messages;

	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
	 */
	@Override
	public void initialize(ValidCollection constraintAnnotation) {
		constraints = constraintAnnotation.constraints();
		messages = constraintAnnotation.messages();
	}

	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(List<?> entries,
			ConstraintValidatorContext context) {
		boolean valid = true;

		if(entries == null){
			return valid;
		}
		
		if(ArrayUtils.getLength(constraints) != ArrayUtils.getLength(messages)){
			throw new ConstraintDeclarationException("Number of messages must be the same as number of constraints");
		}

		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		ConstraintValidatorFactory constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();

		for(Object element : entries) {
			for(Class<?> constraint : constraints) {
				Constraint constraintAnnotation = constraint.getAnnotation(Constraint.class);
				Class<? extends ConstraintValidator<?, ?>>[] constraintValidators = constraintAnnotation.validatedBy();
				for (int i = 0; i < constraintValidators.length; i++) {
					ConstraintValidator constraintValidator = constraintValidatorFactory.getInstance(constraintValidators[i]);
					if(!constraintValidator.isValid(element, context)){
						context.buildConstraintViolationWithTemplate(messages[i]).addConstraintViolation().disableDefaultConstraintViolation();
						valid = false;
					}
				}
			}

		}

		return valid;
	}

}