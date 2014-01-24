package nl.ipo.cds.attributemapping.operations.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Documented
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MappingOperation {

	Class<?> propertiesClass () default Object.class;
	String[] messageSources () default { };
	boolean internal () default false;
}
