package nl.ipo.cds.validation.execute;

public interface ExecutableExpression<Context, Type> {
	
	Class<Type> getResultType ();
	ExpressionExecutor<Context> getExecutor (Compiler<Context> compiler) throws CompilerException;
	Class<Context> getContextType ();
}
