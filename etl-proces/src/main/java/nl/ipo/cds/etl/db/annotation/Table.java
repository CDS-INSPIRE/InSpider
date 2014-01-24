package nl.ipo.cds.etl.db.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Table {
	String name() default "##default";
	String schema() default "##default";
}
