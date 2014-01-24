/**
 * 
 */
package nl.ipo.cds.validator.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import nl.ipo.cds.validator.constraints.impl.ValidCollectionValidator;

/**
 * @author eshuism
 * 1 feb 2012
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=ValidCollectionValidator.class)
public @interface ValidCollection {

    Class<?>[] constraints() default {};

	String message() default "Collection not valid";
	String[] messages() default {};
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};


}
