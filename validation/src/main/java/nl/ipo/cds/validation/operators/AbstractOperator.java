package nl.ipo.cds.validation.operators;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.BinaryExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ExpressionEvaluationException;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public abstract class AbstractOperator<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType, InputType> extends AbstractExpression<K, C, ResultType> implements BinaryExpression<K, C, ResultType, InputType, InputType> {

	public final Expression<K, C, InputType> a;
	public final Expression<K, C, InputType> b;
	
	public AbstractOperator (final Expression<K, C, InputType> a, final Expression<K, C, InputType> b) {
		if (a == null) {
			throw new NullPointerException ("a cannot be null");
		}
		if (b == null) {
			throw new NullPointerException ("b cannot be null");
		}
		
		this.a = a;
		this.b = b;
	}

	public ResultType evaluate (final C context, final InputType a, final InputType b) {
		final InputType aValue = a;
		final InputType bValue = b;
		
		if (aValue == null || bValue == null) {
			throw new ExpressionEvaluationException (String.format ("Null values not accepted as input for %s", getClass ().getCanonicalName ()));
		}
		
		return evaluate (aValue, bValue, context);
	}
	
	public abstract ResultType evaluate (final InputType a, final InputType b, final C context);
	public abstract String getOperatorName ();
	
	@Override
	public Class<InputType> getTypeA () {
		return a.getResultType ();
	}
	
	@Override
	public Class<InputType> getTypeB () {
		return b.getResultType ();
	}
	
	@Override
	public String toString () {
		return String.format ("(%s %s %s)", a.toString (), getOperatorName (), b.toString ());
	}
	
	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					AbstractOperator.class, 
					"evaluate", 
					MethodType.methodType (Object.class, ValidatorContext.class, Object.class, Object.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
		return ExpressionExecutor.create (
				this, 
				a, 
				b, 
				false, 
				true, 
				evaluateHandle.bindTo (this),
				false
			);
	}
}
