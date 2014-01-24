package nl.ipo.cds.etl.xml.bind;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface GmlElement {
	String name() default "##default";
	String namespace() default "##default";
}
