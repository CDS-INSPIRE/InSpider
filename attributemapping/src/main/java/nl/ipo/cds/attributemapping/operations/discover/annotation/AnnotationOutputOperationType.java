package nl.ipo.cds.attributemapping.operations.discover.annotation;

import nl.ipo.cds.attributemapping.operations.OutputOperationType;

import org.springframework.context.MessageSource;

public class AnnotationOutputOperationType extends AnnotationOperationType
		implements OutputOperationType {

	AnnotationOutputOperationType(Object bean, String name, MessageSource messageSource) {
		super(bean, name, messageSource);
	}
}
