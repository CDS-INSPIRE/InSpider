package nl.ipo.cds.dao.impl;

import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikersRol;
import nl.ipo.cds.domain.Rol;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class GebruikersRolImpl implements GebruikersRol {

	private Gebruiker gebruiker;
	private Bronhouder bronhouder;
	private Rol rol;

	public GebruikersRolImpl (final Gebruiker gebruiker, final Rol rol, final Bronhouder bronhouder) {
		this.gebruiker = gebruiker;
		this.rol = rol;
		this.bronhouder = bronhouder;
	}
	
	@Override
	public Gebruiker getGebruiker() {
		return gebruiker;
	}

	@Override
	public Rol getRol() {
		return rol;
	}

	@Override
	public Bronhouder getBronhouder() {
		return bronhouder;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().
			append(this.getGebruiker()).
			append(this.getRol()).
			append(this.getBronhouder()).
			toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (! (obj instanceof GebruikersRol))
			return false;

		final GebruikersRol other = (GebruikersRol) obj;

		return new EqualsBuilder().
			append(this.getBronhouder(), other.getBronhouder()).
			append(this.getGebruiker(), other.getGebruiker()).
			append(this.getRol(), other.getRol()).
			isEquals();
	}
}
