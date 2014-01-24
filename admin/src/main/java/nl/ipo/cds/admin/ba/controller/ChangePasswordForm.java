/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import javax.validation.constraints.AssertTrue;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author eshuism
 * 2 mrt 2012
 */
public class ChangePasswordForm {

	@NotBlank(message="Verplicht")
	private String passwordOld;
	
	@NotBlank(message="Verplicht")
	private String passwordNew;
	
	@NotBlank(message="Verplicht")
	private String passwordConfirm;

	@AssertTrue(message="De wachtwoorden zijn niet gelijk")
	public boolean isPasswordsEqual(){

		return StringUtils.equals(passwordNew, passwordConfirm);
	}

	public String getPasswordOld() {
		return passwordOld;
	}

	public void setPasswordOld(String passwordOld) {
		this.passwordOld = passwordOld;
	}

	public String getPasswordNew() {
		return passwordNew;
	}

	public void setPasswordNew(String passwordNew) {
		this.passwordNew = passwordNew;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}
}
