package nl.ipo.cds.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class ThemaBronhouderAuthorization implements Serializable {

	private static final long serialVersionUID = 7722858810254006319L;

	@Id
	@NotNull
	@ManyToOne
	private Thema thema;
	
	@Id
	@NotNull
	@ManyToOne
	private Bronhouder bronhouder;

	ThemaBronhouderAuthorization () {
	}
	
	public ThemaBronhouderAuthorization (final Thema thema, final Bronhouder bronhouder) {
		assert (thema != null);
		assert (bronhouder != null);
		
		this.thema = thema;
		this.bronhouder = bronhouder;
	}
	
	public Thema getThema() {
		return thema;
	}
	
	public Bronhouder getBronhouder() {
		return bronhouder;
	}
}
