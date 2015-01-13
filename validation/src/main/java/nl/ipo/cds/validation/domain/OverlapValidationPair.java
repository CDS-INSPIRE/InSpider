/**
 * 
 */
package nl.ipo.cds.validation.domain;

/**
 * @author marnix
 *
 */
public class OverlapValidationPair<T> {
	public final T f1;
	public final T f2;

	public OverlapValidationPair(T f1, T f2) {
		this.f1 = f1;
		this.f2 = f2;
	}
}
