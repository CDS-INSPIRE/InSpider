package nl.ipo.cds.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * This entity authorizes a {@link DbGebruiker} to access a {@link Thema} on behalf of a
 * {@link Bronhouder}. A user is authorized on a {@link BronhouderThema}, which associates a bronhouder
 * with a theme. 
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
	private BronhouderThema bronhouderThema;
	
	@NotNull
	private TypeGebruik typeGebruik;
	
	GebruikerThemaAutorisatie () {
	}
	
	/**
	 * Constructs a new authorization by providing the user, the bronhouder and theme.
	 * 
	 * @param gebruiker The user for the relation.
	 * @param bronhouder Thema Provides the relation between bronhouder and thema
	 * @param typeGebruik The type of authorization.
	 */
	public GebruikerThemaAutorisatie (final DbGebruiker gebruiker, final BronhouderThema bronhouderThema, final TypeGebruik typeGebruik) {
		if (gebruiker == null) {
			throw new NullPointerException ("gebruiker cannot be null");
		}
		if (bronhouderThema == null) {
			throw new NullPointerException ("bronhouderThema cannot be null");
		}
		if (typeGebruik == null) {
			throw new NullPointerException ("typeGebruik cannot be null");
		}
		
		this.gebruiker = gebruiker;
		this.bronhouderThema = bronhouderThema;
		this.typeGebruik = typeGebruik;
	}

	/**
	 * @return The {@link DbGebruiker} associated with this authorization.
	 */
	public DbGebruiker getGebruiker () {
		return gebruiker;
	}

	/**
	 * @return The {@link BronhouderThema} associated with this authorization.
	 */
	public BronhouderThema getBronhouderThema () {
		return bronhouderThema;
	}

	/**
	 * @return The type of authorization.
	 */
	public TypeGebruik getTypeGebruik () {
		return typeGebruik;
	}
}
