package nl.ipo.cds.attributemapping.operations.discover.annotation;

import java.lang.reflect.Type;
import java.util.Locale;

import org.springframework.context.NoSuchMessageException;

import nl.ipo.cds.attributemapping.operations.OperationInputType;

public class AnnotationOperationInputType implements OperationInputType {

	private final AnnotationOperationType operationType;
	private final String name;
	private final int parameterIndex;
	private Type inputType;
	private final boolean variableInputCount;
	
	AnnotationOperationInputType (final AnnotationOperationType operationType, final String name, final int parameterIndex, final Type parameterType, final boolean variableInputCount) {
		this.operationType = operationType;
		this.name = name;
		this.parameterIndex = parameterIndex;
		this.inputType = parameterType;
		this.variableInputCount = variableInputCount;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getInputType() {
		return inputType;
	}

	@Override
	public boolean isVariableInputCount () {
		return variableInputCount;
	}
	
	@Override
	public String getDescription(Locale locale) {
		try {
			return operationType.getMessageSource ().getMessage (
					getMessageKey (),
					null,
					locale
				);
		} catch (NoSuchMessageException e) {
			return name;
		}
	}
	
	public String getMessageKey () {
		return String.format ("%s.%s.description", operationType.getMessageKey (), name); 
	}
	
	public int getParameterIndex () {
		return parameterIndex;
	}
}
