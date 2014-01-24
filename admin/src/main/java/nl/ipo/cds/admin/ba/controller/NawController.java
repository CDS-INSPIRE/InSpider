/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.security.Principal;

import java.util.ArrayList;
import java.util.List;

import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.admin.security.AuthzImpl;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
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
		Bronhouder bronhouder = this.managerDao.getBronhouder(bronhouderId);
		Assert.notNull(bronhouder, "Bronhouder with id \"" + bronhouderId + "\", could not be found.");
		Boolean authorized = this.managerDao.isUserAuthorizedForBronhouder(bronhouder, principal.getName());
		if(!authorized){
			//TODO:MES: If this happens the views will break, because they expect a bronhouder to be available in the model
//			throw new IllegalStateException("User is not a bronhouder");
		}
		return bronhouder;
	}

	@RequestMapping(value ="/ba/naw/{bronhouderId}", method = RequestMethod.GET)
	public String updateNAWForm(@ModelAttribute Bronhouder bronhouder, Model model) {
        model.addAttribute("updateBronhouderForm", new BronhouderNAW (bronhouder));
		/** check the authorization of the current user
		 *  fill the bronhouders list depending on the role
		 *  i.e. only the current bronhouder for role bronhouder 
		 *  or all bronhouders for role beheerder 
		 */
		AuthzImpl authz = new AuthzImpl();
		final List<Bronhouder> bronhouderList ;
		if (authz.anyGranted("ROLE_BEHEERDER")){
			bronhouderList = managerDao.getAllBronhouders();
		}else{
			bronhouderList = new ArrayList<Bronhouder>();
			bronhouderList.add(bronhouder);
		}
		model.addAttribute("bronhouders", bronhouderList);
        return "/ba/naw";
	}
	
	@RequestMapping(value ="/ba/naw/{bronhouderId}", method = RequestMethod.POST)
	public String updateNAW(@ModelAttribute Bronhouder bronhouder, Model model) {

		this.managerDao.update(bronhouder);
        model.addAttribute("updateBronhouderForm", new BronhouderNAW (bronhouder));
        final List<Bronhouder> bronhouderList = managerDao.getAllBronhouders();
        model.addAttribute ("bronhouders", bronhouderList);
		model.addAttribute ("pgrBaseUrl", reportConfiguration.getPgrBaseUrl ());
        // Redirect after POST pattern
        return "redirect:/ba/naw/" + bronhouder.getId();
	}
	
}
