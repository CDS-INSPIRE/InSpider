package nl.ipo.cds.validation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

import org.deegree.geometry.primitive.Point;

public class LastLocationExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractExpression<K, C, String> {

	private final static String separator = ", ";
	
	@Override
	public Class<String> getResultType () {
		return String.class;
	}

	public String evaluate (final ValidatorContext<K, C> context) {
		final Point point = context.getLastLocation ();
		
		if(point == null) {
			return "?";
		}
		
		StringBuilder stringBuilder = new StringBuilder("(");
		stringBuilder.append(point.get0());
		stringBuilder.append(separator);
		stringBuilder.append(point.get1());
		double p2 = point.get2();
		if(!Double.isNaN(p2)) {
			stringBuilder.append(separator);
			stringBuilder.append(p2);
		}
		stringBuilder.append(')');
		return stringBuilder.toString ();
	}

	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					LastLocationExpression.class, 
					"evaluate", 
					MethodType.methodType (String.class, ValidatorContext.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
		return ExpressionExecutor.create (
				this, 
				false, 
				false, 
				evaluateHandle.bindTo (this),
				false
			);
	}
}
