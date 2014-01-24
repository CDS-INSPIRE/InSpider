package nl.ipo.cds.validation.execute;

public class ExecutionStep<C> {

	public final ExpressionExecutor<C> executor;
	public final int[] inputs;
	
	ExecutionStep (final ExpressionExecutor<C> executor, final int[] inputs) {
		this.executor = executor;
		this.inputs = inputs;
	}
	
	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		builder.append (executor.toString ());
		builder.append (" [");
		
		String separator = "";
		for (final int n: inputs) {
			builder.append (separator);
			builder.append (String.format ("%d", n));
			separator = ", ";
		}
		builder.append (']');
		
		return builder.toString ();
	}
}
