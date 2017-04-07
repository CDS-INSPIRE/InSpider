/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.security.Principal;
import java.util.Collections;

import javax.validation.Valid;

import nl.ipo.cds.admin.security.AuthzImpl;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Gebruiker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

/**
 * @author Rob
 *
 */
@Controller
@RequestMapping("/")
public class BAIndexController {

	@Autowired
	private ManagerDao managerDao;

	@RequestMapping
	public String index () {
		AuthzImpl authz = new AuthzImpl();
		if (authz.anyGranted("ROLE_SUPERUSER")){
			return "redirect:/ba/monitoring";
		} else if (authz.anyGranted("ROLE_RAADPLEGER")) {
			return "redirect:/ba/etloverzicht";
		}else{
			return "redirect:/no-access";
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login (@RequestParam(required=false, defaultValue="false") boolean error, 
			final @RequestParam(required=false, defaultValue="false") boolean closeWindow, 
			Model model) {
		
		model.addAttribute ("error", error);
		model.addAttribute ("closeWindow", closeWindow);
		model.addAttribute("showTabs", false);

		return "login";
	}
	
	@RequestMapping (value = "/login-close", method = RequestMethod.GET)
	public String loginClose () {
		return "login-close";
	}

	@RequestMapping(value = "/changePassword", method = RequestMethod.GET)
	public String changePassword (@ModelAttribute ChangePasswordForm changePasswordForm, Model model) {
		/* Use Annotation @ModelAttribute for convenience, so spring creates an empty ChangePasswordForm-object,
		 * but more important: adds it to the model
		*/
		model.addAttribute("showTabs", false);

		return "changePassword";
	}

	@RequestMapping(value = "/changePassword", method = RequestMethod.POST)
	public String changePasswordSubmit (@Valid ChangePasswordForm changePasswordForm,
			BindingResult bindingResult, SessionStatus status, Model model,
			Principal principal) {

		model.addAttribute("showTabs", false);
		
		String gebruikersnaam = principal.getName();

		// Check Old password
		boolean authenticated = this.managerDao.authenticate(gebruikersnaam, changePasswordForm.getPasswordOld());
		if(!authenticated){
			bindingResult.rejectValue("passwordOld", "oldPasswordInvalid", "Oude wachtwoord onjuist");
		}
		// Check for validation-errors
		if(bindingResult.hasErrors()){
			return "changePassword";
		} else {
			status.setComplete();
		}
		
		// Do the actual change of the password
		Gebruiker changedGebruiker = managerDao.getGebruiker(gebruikersnaam); 
		changedGebruiker.setWachtwoord(changePasswordForm.getPasswordNew());
		managerDao.update(changedGebruiker);
		
		return "redirect:/";
	}
	
	/**
	 * Displays a view for users that have insufficient permissions to manage any data
	 * in the admin.
	 * 
	 * @return The view name.
	 */
	@RequestMapping ("/no-access")
	public String raadpleger (final Principal principal, final Model model) {
		final Gebruiker gebruiker = managerDao.getGebruiker (principal.getName ());
		
		if (gebruiker != null) {
			model.addAttribute ("gebruikerThemas", managerDao.getGebruikerThemaAutorisatie (gebruiker));
		} else {
			model.addAttribute ("gebruikerThemas", Collections.emptyList ());
		}
		
		return "no-access";
	}
}




