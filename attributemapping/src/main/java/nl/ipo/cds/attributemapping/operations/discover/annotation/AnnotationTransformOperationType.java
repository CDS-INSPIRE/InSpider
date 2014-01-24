package nl.ipo.cds.attributemapping.operations.discover.annotation;

import nl.ipo.cds.attributemapping.operations.TransformOperationType;

import org.springframework.context.MessageSource;

public final class AnnotationTransformOperationType extends AnnotationOperationType
		implements TransformOperationType {

	public AnnotationTransformOperationType(Object bean, String name, MessageSource messageSource) {
		super(bean, name, messageSource);
	}

}
