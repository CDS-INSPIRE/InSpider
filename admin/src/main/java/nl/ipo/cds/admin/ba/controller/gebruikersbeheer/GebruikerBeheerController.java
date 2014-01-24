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
import nl.ipo.cds.domain.GebruikersRol;
import nl.ipo.cds.domain.Rol;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
	
			List<GebruikersRol> gebruikersRollen = this.managerDao.getGebruikersRollenByGebruiker(gebruiker);
			// For now (CDS-INSPIRE) there can only be one role
			if(CollectionUtils.size(gebruikersRollen) != 1){
				model.addAttribute("userMessage", "LDAP fout geconfigureerd. Gebruiker \"" + gebruikersNaam + "\" heeft meer dan 1 rol.");
			} else {
				GebruikersRol gebruikersRol = gebruikersRollen.get(0);
				/* Explicitly don't preset "gebruikerForm.beheerder" and "gebruikerForm.bronhouder" on submit,
				 * because when the "beheerder"-checkbox is not checked, there won't be
				 * a "beheerder"-request-parameter (which will set gebruikerForm.beheerder to false).
				 * Same goes for bronhouder-combobox. When it's disabled it won't send a bronhouder-request-parameter with a null value.
				 */
				if(StringUtils.isBlank(submit)){
					gebruikerForm.setBeheerder(gebruikersRol.getRol().equals(Rol.BEHEERDER)? true : false);
					gebruikerForm.setBronhouder(gebruikersRollen.get(0).getBronhouder());
				}
	
				gebruikerForm.setGebruikersRol(gebruikersRol);
			}
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
	public String submit (@Valid @ModelAttribute(value="gebruikerForm") GebruikerForm gebruikerForm, BindingResult bindingResult,
			SessionStatus status, @PathVariable(value="gebruikersNaam") String gebruikersNaam, Model model) {
		
		// Check whether the username is unique when creating a new user.
		if ("_new".equalsIgnoreCase (gebruikersNaam) && !bindingResult.hasFieldErrors ("gebruiker.gebruikersnaam")) {
			final String naam = gebruikerForm.getGebruiker ().getGebruikersnaam ();
			if (managerDao.getGebruiker (naam) != null) {
				bindingResult.rejectValue ("gebruiker.gebruikersnaam", "nl.ipo.cds.admin.ba.controller.gebruikersbeheer.GebruikerBeheerController.duplicateUsername", new Object[] { }, "Er bestaat al een gebruiker met deze naam");
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
			// Update the GebruikerRol if necessary
			this.managerDao.delete(gebruikerForm.getGebruikersRol());
		}
		
		this.managerDao.createGebruikersRol(gebruikerForm.getGebruiker(), 
				gebruikerForm.isBeheerder() ? Rol.BEHEERDER : Rol.BRONHOUDER, 
				gebruikerForm.getBronhouder()
				);
		
		// Redirect after POST pattern
		return "redirect:/ba/gebruikersbeheer/gebruikers";
	}

	@RequestMapping(value="/delete", method = RequestMethod.GET)
	public String delete (@ModelAttribute(value="gebruikerForm") GebruikerForm gebruikerForm,
			BindingResult bindingResult, SessionStatus status, Model model,
			Principal principal) {

		if(principal.getName().equalsIgnoreCase(gebruikerForm.getGebruiker().getGebruikersnaam())){
			model.addAttribute("userMessage", "Een beheerder mag zichzelf niet verwijderen!");
			return "/ba/gebruikersbeheer/gebruikers";
		} else {
			status.setComplete();
		}

		this.managerDao.delete(gebruikerForm.getGebruikersRol());
		this.managerDao.delete(gebruikerForm.getGebruiker());

		return "redirect:/ba/gebruikersbeheer/gebruikers";
	}
}
