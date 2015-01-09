package nl.ipo.cds.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * 
 * @author erik
 *
 */
@Entity
public final class GebruikerThemaAutorisatie implements Serializable {
	private static final long serialVersionUID = -7678955695408667947L;

	@NotNull
	@Id
	@ManyToOne
	private DbGebruiker gebruiker;
	
	@NotNull
	@Id
	@ManyToOne
	private Thema thema;
	
	@NotNull
	private TypeGebruik typeGebruik;
	
	public GebruikerThemaAutorisatie (final DbGebruiker gebruiker, final Thema thema, final TypeGebruik typeGebruik) {
		if (gebruiker == null) {
			throw new NullPointerException ("gebruiker cannot be null");
		}
		if (thema == null) {
			throw new NullPointerException ("thema cannot be null");
		}
		if (typeGebruik == null) {
			throw new NullPointerException ("typeGebruik cannot be null");
		}
		
		this.gebruiker = gebruiker;
		this.thema = thema;
		this.typeGebruik = typeGebruik;
	}

	public DbGebruiker getGebruiker() {
		return gebruiker;
	}

	public Thema getThema() {
		return thema;
	}

	public TypeGebruik getTypeGebruik() {
		return typeGebruik;
	}
}
