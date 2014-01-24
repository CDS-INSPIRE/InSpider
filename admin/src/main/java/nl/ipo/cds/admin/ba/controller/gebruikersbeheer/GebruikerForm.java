/**
 * 
 */
package nl.ipo.cds.admin.ba.controller.gebruikersbeheer;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikersRol;

import org.apache.commons.lang.StringUtils;

/**
 * @author eshuism
 * 30 jan 2012
 */
public class GebruikerForm {

	@Valid
	private Gebruiker gebruiker;

	private boolean beheerder;
	
	private Bronhouder bronhouder;

	private GebruikersRol gebruikersRol;

	private boolean changePassword;
	
	private boolean passwordSet;

	@AssertTrue(message="Een beheerder kan geen bronhouder kiezen. Een beheerder is automatisch bronhouder voor alle provincies")
	public boolean isBronhouderMandatoryValid(){
		boolean valid = true;

		// If beheerder, then bronhouder is not permitted
		if(this.isBeheerder()){
			if(this.getBronhouder() != null){
				valid = false;
			}
		}

		return valid;
	}

	@AssertTrue(message="Een bronhouder is verplicht een provincie te kiezen")
	public boolean isBronhouderDisallowedValid(){
		boolean valid = true;

		// If bronhouder, then bronhouder is mandatory
		if(!this.isBeheerder()) {
			if(this.getBronhouder() == null){
				valid = false;
			}
		}

		return valid;
	}

	public Gebruiker getGebruiker() {
		return gebruiker;
	}

	public void setGebruiker(Gebruiker gebruiker) {
		this.gebruiker = gebruiker;
	}

	public boolean isBeheerder() {
		return beheerder;
	}

	public void setBeheerder(boolean beheerder) {
		this.beheerder = beheerder;
	}

	public Bronhouder getBronhouder() {
		return bronhouder;
	}

	public void setBronhouder(Bronhouder bronhouder) {
		this.bronhouder = bronhouder;
	}

	/**
	 * @param gebruikersRol
	 */
	public void setGebruikersRol(GebruikersRol gebruikersRol) {
		this.gebruikersRol = gebruikersRol;
		
	}

	public GebruikersRol getGebruikersRol() {
		return gebruikersRol;
	}

	// To satisfy Spring MVC. However, we won't display the password ever 
	public String getWachtwoord() {
		return "";
	}

	public void setWachtwoord(String wachtwoord) {
		// Mark that password is set
		this.passwordSet = StringUtils.isNotBlank(wachtwoord);
		// Store the password
		this.gebruiker.setWachtwoord(wachtwoord);
	}

	@AssertTrue(message="Verplicht bij toevoegen nieuwe gebruiker of als keuze \"Wachtwoord wijzigen\" aangevinkt is")
	public boolean isNotNullWachtwoord(){
		boolean result = true;
		if(this.isChangePassword()){
			result = this.isPasswordSet();
		}
		return result;
	}
	
	public boolean isChangePassword() {
		return changePassword;
	}

	public void setChangePassword(boolean changePassword) {
		this.changePassword = changePassword;
	}

	public boolean isPasswordSet() {
		return passwordSet;
	}

	public void setPasswordSet(boolean passwordSet) {
		this.passwordSet = passwordSet;
	}

}
