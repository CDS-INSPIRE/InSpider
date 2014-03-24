package nl.ipo.cds.admin.i18n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.context.annotation.Import;

@Import({FactoryConfiguration.class, MessageInterfaceRegistar.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableI18N {

	String basePackage();
}
