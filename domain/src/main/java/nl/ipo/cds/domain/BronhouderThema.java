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

	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bronhouder == null) ? 0 : bronhouder.hashCode());
		result = prime * result + ((thema == null) ? 0 : thema.hashCode());
		return result;
	}

	@Override
	public boolean equals (final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BronhouderThema other = (BronhouderThema) obj;
		if (bronhouder == null) {
			if (other.bronhouder != null)
				return false;
		} else if (!bronhouder.equals(other.bronhouder))
			return false;
		if (thema == null) {
			if (other.thema != null)
				return false;
		} else if (!thema.equals(other.thema))
			return false;
		return true;
	}
}
