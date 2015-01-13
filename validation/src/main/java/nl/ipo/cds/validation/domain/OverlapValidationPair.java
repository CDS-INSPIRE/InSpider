/**
 * 
 */
package nl.ipo.cds.validation.domain;

/**
 * @author marnix
 *
 */
public class OverlapValidationPair<F1, F2> {
	public final F1 f1;
	public final F2 f2;

	public OverlapValidationPair(F1 f1, F2 f2) {
		this.f1 = f1;
		this.f2 = f2;
	}
}
