package nl.ipo.cds.validation.flow;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.Executor;
import nl.ipo.cds.validation.execute.ExecutorException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public class IfExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType> extends AbstractExpression<K, C, ResultType> {

	
	public final Expression<K, C, Boolean> condition;
	public final Expression<K, C, ResultType> a;
	public final Expression<K, C, ResultType> b;
	
	public IfExpression (final Expression<K, C, Boolean> condition, final Expression<K, C, ResultType> a, final Expression<K, C, ResultType> b) {
		if (condition == null) {
			throw new NullPointerException ("condition cannot be null");
		}
		if (a == null) {
			throw new NullPointerException ("a cannot be null");
		}
		if (b == null) {
			throw new NullPointerException ("b cannot be null");
		}
		
		this.condition = condition;
		this.a = a;
		this.b = b;
	}

	@Override
	public Class<ResultType> getResultType() {
		return a.getResultType ();
	}

	@Override
	public String toString () {
		if (b == null) {
			return String.format ("(if %s then %s)", condition.toString (), a.toString ());
		} else {
			return String.format ("(if %s then %s else %s)", condition.toString (), a.toString (), b.toString ());
		}
	}

	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
		// Compile both branches:
		final Executor<C> aExecutor = compiler.compile (a);
		final Executor<C> bExecutor = compiler.compile (b);
		
        final MethodHandle handle =
                MethodHandles.insertArguments (
                        executeHandle,
                        4,
                        aExecutor,
                        bExecutor
                ).bindTo (this);

        return ExpressionExecutor.create (
				this, 
				condition, 
				false, 
				true, 
				//executeHandle.bindTo (executor),
                handle,
				false
			);
	}
	
	private final static MethodHandle executeHandle = Compiler
			.findMethod(
                    IfExpression.class,
                    "execute",
                    MethodType.methodType(Object.class, Object[].class, ValidatorContext.class, Boolean.class, Executor.class, Executor.class)
            );

    @SuppressWarnings("unchecked")
	public ResultType execute (final Object[] objects, final ValidatorContext<K, C> context, final Boolean conditionValue, final Executor<ValidatorContext<K, C>> aExecutor, final Executor<ValidatorContext<K, C>> bExecutor) throws ExecutorException {
        if (conditionValue != null && conditionValue) {
            return (ResultType)aExecutor.execute (context, objects);
        } else {
            return (ResultType)bExecutor.execute (context, objects);
        }
    }
}
