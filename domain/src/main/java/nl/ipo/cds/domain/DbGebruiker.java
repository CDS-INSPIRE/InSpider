package nl.ipo.cds.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table (name = "gebruiker")
public final class DbGebruiker {

	@Id
	@NotNull
	private String gebruikersnaam;

	private boolean superuser;
	
	DbGebruiker () {
		this.gebruikersnaam = null;
	}
	
	public DbGebruiker (final String gebruikersnaam) {
		this.gebruikersnaam = gebruikersnaam;
	}
	
	public String getGebruikersnaam () {
		return gebruikersnaam;
	}

	public void setGebruikersnaam (final String gebruikersnaam) {
		this.gebruikersnaam = gebruikersnaam;
	}

	public boolean isSuperuser () {
		return superuser;
	}

	public void setSuperuser (final boolean superuser) {
		this.superuser = superuser;
	}
}
