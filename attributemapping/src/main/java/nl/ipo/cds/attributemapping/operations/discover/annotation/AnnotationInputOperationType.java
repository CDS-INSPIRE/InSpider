package nl.ipo.cds.attributemapping.operations.discover.annotation;

import nl.ipo.cds.attributemapping.operations.InputOperationType;

import org.springframework.context.MessageSource;

public final class AnnotationInputOperationType extends AnnotationOperationType
		implements InputOperationType {

	AnnotationInputOperationType(Object bean, String name, MessageSource messageSource) {
		super(bean, name, messageSource);
	}

}
