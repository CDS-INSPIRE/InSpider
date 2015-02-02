package nl.ipo.cds.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * BronhouderThema links a {@link Bronhouder} to a {@link Thema}.
 */
@Entity
public class BronhouderThema implements Serializable {

	private static final long serialVersionUID = 7722858810254006319L;

	@Id
	@NotNull
	@ManyToOne
	private Thema thema;
	
	@Id
	@NotNull
	@ManyToOne
	private Bronhouder bronhouder;

	BronhouderThema () {
	}
	
	public BronhouderThema (final Thema thema, final Bronhouder bronhouder) {
		assert (thema != null);
		assert (bronhouder != null);
		
		this.thema = thema;
		this.bronhouder = bronhouder;
	}
	
	/**
	 * @return The theme associated with this link.
	 */
	public Thema getThema() {
		return thema;
	}
	
	/**
	 * @return The bronhouder associated with this link.
	 */
	public Bronhouder getBronhouder() {
		return bronhouder;
	}
}
