/**
 * 
 */
package nl.ipo.cds.admin.ba.controller.gebruikersbeheer;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Gebruiker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 * @author eshuism
 * 26 jan 2012
 */
@Controller
@RequestMapping("/ba/gebruikersbeheer")
public class GebruikersBeheerController {

	@Autowired
	ManagerDao managerDao;
	
	@ModelAttribute("roleFunction")
	String getRoleFunction(){
		return "beheerder";
	}

	/**
	 * Combobox bronhouders Filter
	 * @return
	 */
	@ModelAttribute("bronhouders")
	public List<Bronhouder> getBronhouders(){
	    return managerDao.getAllBronhouders();
	}	   

	@ModelAttribute("principalName")
	public String principalName (Principal principal) {
		String principalName = null;
		if(principal != null){
			principalName = principal.getName();
		}
		return principalName;
	}
	
	/**
	 * Get all gebruikers
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/gebruikers", method = RequestMethod.GET)
	public String index (Model model) {
		return "/ba/gebruikersbeheer/gebruikers";
	}

	/**
	 * JSON user response of all users
	 * @param model
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public List<GebruikerWrapper> getUsers (	Model model,
										Principal principal) {
		List<Gebruiker> users = this.managerDao.getAllGebruikers();
		List<GebruikerWrapper> wrappedUsers = new ArrayList<GebruikerWrapper>(users.size());
		for (Iterator<Gebruiker> iterator = users.iterator(); iterator.hasNext();) {
			Gebruiker gebruiker = (Gebruiker) iterator.next();
			boolean beheerder = gebruiker.isSuperuser ();

			GebruikerWrapper gebruikerWrapper = new GebruikerWrapper(gebruiker, beheerder);
			wrappedUsers.add(gebruikerWrapper);
		}
		return wrappedUsers;
	}

	class GebruikerWrapper {

		private Gebruiker gebruiker;

		private boolean beheerder;
		
		public GebruikerWrapper(Gebruiker gebruiker,
				boolean beheerder) {
			super();
			this.gebruiker = gebruiker;
			this.beheerder = beheerder;
		}

		public String getEmail() {
			return this.gebruiker.getEmail();
		}

		public String getGebruikersnaam() {
			return this.gebruiker.getGebruikersnaam();
		}

		public String getMobile() {
			return this.gebruiker.getMobile();
		}

		public void setEmail(String email) {
			this.gebruiker.setEmail(email);
		}

		public void setGebruikersnaam(String gebruikersnaam) {
			this.gebruiker.setGebruikersnaam(gebruikersnaam);
		}

		public void setMobile(String mobile) {
			this.gebruiker.setMobile(mobile);
		}

		public boolean isBeheerder() {
			return beheerder;
		}
	}
}
