/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import nl.ipo.cds.admin.ba.util.GebruikerAuthorization;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.TypeGebruik;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller voor de NAW pagina <br/>
 * @author Rob
 *
 */
@Controller
public class NawController{

	@Autowired
	private ManagerDao managerDao;
	
	@Autowired
	private ReportConfiguration reportConfiguration;

	@ModelAttribute("roleFunction")
	String getRoleFunction(){
		return "bronhouder";
	}

	@ModelAttribute("bronhouder")
	public Bronhouder populateBronhouder (@PathVariable long bronhouderId, Principal principal) {
		final Bronhouder bronhouder = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao)
			.getAuthorizedBronhouder (bronhouderId);

		return bronhouder;
	}

	@RequestMapping(value ="/ba/naw/{bronhouderId}", method = RequestMethod.GET)
	public String updateNAWForm(@ModelAttribute Bronhouder bronhouder, Model model, final Principal principal) {
		if (bronhouder == null) {
			return "redirect:/ba";
		}
		
        model.addAttribute("updateBronhouderForm", new BronhouderNAW (bronhouder));
        
		/** check the authorization of the current user
		 *  fill the bronhouders list depending on the role
		 *  i.e. only the current bronhouder for role bronhouder 
		 *  or all bronhouders for role beheerder 
		 */
        final Collection<Bronhouder> bronhouderList = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao)
        	.getAuthorizedBronhouders ();
        
		model.addAttribute("bronhouders", bronhouderList);
		
        return "/ba/naw";
	}
	
	@RequestMapping(value ="/ba/naw/{bronhouderId}", method = RequestMethod.POST)
	public String updateNAW(@ModelAttribute Bronhouder bronhouder, Model model) {
		if (bronhouder == null) {
			return "redirect:/ba";
		}
		
		this.managerDao.update(bronhouder);
        model.addAttribute("updateBronhouderForm", new BronhouderNAW (bronhouder));
        final List<Bronhouder> bronhouderList = managerDao.getAllBronhouders();
        model.addAttribute ("bronhouders", bronhouderList);
		model.addAttribute ("pgrBaseUrl", reportConfiguration.getPgrBaseUrl ());
        // Redirect after POST pattern
        return "redirect:/ba/naw/" + bronhouder.getId();
	}
	
}
