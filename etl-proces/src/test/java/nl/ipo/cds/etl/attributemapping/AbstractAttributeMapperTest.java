package nl.ipo.cds.etl.attributemapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;

import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationOperationType;
import nl.ipo.cds.etl.attributemapping.AttributeMappingValidator.MessageKey;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

public class AbstractAttributeMapperTest {

	@Inject
	protected ThemeDiscoverer themeDiscoverer;

	@Inject
	protected OperationDiscoverer operationDiscoverer;
	
	protected OperationType getOperationType (final Class<?> operationClass) {
		for (final OperationType ot: operationDiscoverer.getOperationTypes ()) {
			if (ot instanceof AnnotationOperationType && ((AnnotationOperationType)ot).getBean ().getClass ().equals (operationClass)) {
				return ot;
			}
		}
		
		throw new IllegalArgumentException (String.format ("No operation type found for %s", operationClass));
	}
}
