/**
 * 
 */
package nl.ipo.cds.admin.ba.controller.gebruikersbeheer;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import nl.ipo.cds.domain.Gebruiker;

import org.apache.commons.lang.StringUtils;

/**
 * @author eshuism
 * 30 jan 2012
 */
public class GebruikerForm {

	@Valid
	private Gebruiker gebruiker;

	private boolean changePassword;
	
	private boolean passwordSet;

	public Gebruiker getGebruiker() {
		return gebruiker;
	}

	public void setGebruiker(Gebruiker gebruiker) {
		this.gebruiker = gebruiker;
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
