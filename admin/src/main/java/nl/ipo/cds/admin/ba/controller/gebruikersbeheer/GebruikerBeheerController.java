/**
 * 
 */
package nl.ipo.cds.admin.ba.controller.gebruikersbeheer;

import java.security.Principal;
import java.util.List;

import javax.validation.Valid;

import nl.ipo.cds.admin.ba.propertyeditor.IdentityPropertyEditor;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Gebruiker;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * 
 * @author eshuism
 * 26 jan 2012
 */
@Controller
@RequestMapping("/ba/gebruikersbeheer/gebruikers/{gebruikersNaam}")
public class GebruikerBeheerController {

	@Autowired
	private ManagerDao managerDao;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Bronhouder.class, "bronhouder", new IdentityPropertyEditor(Bronhouder.class, this.managerDao));
	}
	
	@ModelAttribute("roleFunction")
	String getRoleFunction(){
		return "beheerder";
	}

	@ModelAttribute("viewName")
	String overrideViewName(){
		/* Don't use the default viewName, but the viewName belonging to the GebruikerSbeheerController.
		 * Therefore it seems to stay on the same tab(-folder)
		 */
		return "/ba/gebruikersbeheer/gebruikers";
	}

	/**
	 * Combobox bronhouders
	 * @return
	 */
	@ModelAttribute("bronhouders")
	public List<Bronhouder> getBronhouders(){
	    return managerDao.getAllBronhouders();
	}

	/**
	 * The commmand object "gebruikerForm".
	 * Especially needed on method level because the binding will occur in the RequestMethod's
	 * after the command object is populated here.
	 */
	@ModelAttribute("gebruikerForm")
	public GebruikerForm getGebruikerForm ( @PathVariable(value="gebruikersNaam") String gebruikersNaam,
											@RequestParam(required=false, value="bronhouderId") Long bronhouderId,
											@RequestParam(required=false, value="submit") String submit,
											Model model) {
		GebruikerForm gebruikerForm = new GebruikerForm();

		if(StringUtils.isBlank(gebruikersNaam)){
			// gebruikersNaam is not allowed to be blank
			return null;
		} else if("_new".equalsIgnoreCase(gebruikersNaam)){
			// New Gebruiker GET or POST
			Gebruiker gebruiker = new Gebruiker();
			gebruikerForm.setGebruiker(gebruiker);
			return gebruikerForm;
		} else {
			// Existing Gebruiker GET or POST
			Gebruiker gebruiker = this.managerDao.getGebruiker(gebruikersNaam);
			gebruikerForm.setGebruiker(gebruiker);
		}
		
		return gebruikerForm;
	}

	@ModelAttribute("gebruikersNaam")
	public String getGebruikersNaam (@PathVariable(value="gebruikersNaam") String gebruikersNaam, Model model) {
		return gebruikersNaam;
	}

	/**
	 * Get a Gebruiker
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String createOrUpdate () {
		
		return "/ba/gebruikersbeheer/gebruiker";
	}

	/**
	 * Save a Gebruiker
	 * @param gebruikerForm
	 * @param bindingResult
	 * @param status
	 * @param gebruikersNaam
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/save", method = RequestMethod.POST)
	@Transactional
	public String submit (@Valid @ModelAttribute(value="gebruikerForm") GebruikerForm gebruikerForm, BindingResult bindingResult,
			SessionStatus status, @PathVariable(value="gebruikersNaam") String gebruikersNaam, Model model,
			final Principal principal,
			final WebRequest request) {
		
		final boolean isSuperuser = request.getParameter ("gebruiker.superuser") != null;
		gebruikerForm.getGebruiker ().setSuperuser (isSuperuser);
		
		// Check whether the username is unique when creating a new user.
		if ("_new".equalsIgnoreCase (gebruikersNaam) && !bindingResult.hasFieldErrors ("gebruiker.gebruikersnaam")) {
			final String naam = gebruikerForm.getGebruiker ().getGebruikersnaam ();
			if (managerDao.getGebruiker (naam) != null) {
				bindingResult.rejectValue ("gebruiker.gebruikersnaam", "nl.ipo.cds.admin.ba.controller.gebruikersbeheer.GebruikerBeheerController.duplicateUsername", new Object[] { }, "Er bestaat al een gebruiker met deze naam");
			}
		}
		
		// Raise an error if the user being modified is currently logged in and the superuser flag is set to false:
		if (!"_new".equalsIgnoreCase (gebruikersNaam) && !isSuperuser) {
			if (principal.getName ().equals (gebruikersNaam)) {
				bindingResult.rejectValue ("gebruiker.superuser", "nl.ipo.cds.admin.ba.controller.gebruikersbeheer.GebruikerBeheerController.removeSuperuserFromCurrentUser", new Object[] { }, "Kan de eigenschap beheerder niet uitzetten voor de ingelogde gebruiker");
			}
		}
		
		// Check for validation-errors
		if(bindingResult.hasErrors()){
			return "/ba/gebruikersbeheer/gebruiker";
		} else {
			status.setComplete();
		}

		// If new, act different then update
		if("_new".equalsIgnoreCase(gebruikersNaam)){
			this.managerDao.create(gebruikerForm.getGebruiker());
		} else {
			this.managerDao.update(gebruikerForm.getGebruiker());
		}
		
		// Redirect after POST pattern
		return "redirect:/ba/gebruikersbeheer/gebruikers";
	}

	@RequestMapping(value="/delete", method = RequestMethod.GET)
	@Transactional
	public String delete (@ModelAttribute(value="gebruikerForm") GebruikerForm gebruikerForm,
			BindingResult bindingResult, SessionStatus status, Model model,
			Principal principal) {

		if(principal.getName().equalsIgnoreCase(gebruikerForm.getGebruiker().getGebruikersnaam())){
			model.addAttribute("userMessage", "Een beheerder mag zichzelf niet verwijderen!");
			return "/ba/gebruikersbeheer/gebruikers";
		} else {
			status.setComplete();
		}

		this.managerDao.delete(gebruikerForm.getGebruiker());

		return "redirect:/ba/gebruikersbeheer/gebruikers";
	}
}
