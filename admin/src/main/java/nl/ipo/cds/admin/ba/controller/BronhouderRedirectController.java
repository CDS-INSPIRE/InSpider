/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.security.Principal;
import java.util.List;

import nl.ipo.cds.admin.ba.util.GebruikerAuthorization;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TypeGebruik;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Purpose of this class is to redirect requests without a bronhouderId, to
 * the controllers that handle requests with a bronhouderID.
 * Determine the bronhouder by using the principal
 * 
 * @author Marcel Eshuis
 * 
 */
@Controller
public class BronhouderRedirectController {

	@Autowired
	private ManagerDao managerDao;

	@RequestMapping(value ="/ba/naw", method = RequestMethod.GET)
	public String determineNAWBronhouder(Principal principal) {
		final Bronhouder bronhouder = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao)
			.getAuthorizedBronhouder (null);

		// User is not bronhouder of any 'provincie'
		//TODO:MES: If this happens the views will break, because they expect a bronhouder to be available in the model
		if (bronhouder == null){
			throw new IllegalStateException("No bronhouder");
		}

    	return "redirect:/ba/naw/" + bronhouder.getId();
	}

	@RequestMapping(value ="/ba/datasetconfig", method = RequestMethod.GET)
	public String determineDatasetBronhouder(Principal principal) {
		final Bronhouder bronhouder = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao)
			.getAuthorizedBronhouder (null);

		// User is not bronhouder of any 'provincie'
		//TODO:MES: If this happens the views will break, because they expect a bronhouder to be available in the model
		if (bronhouder == null){
			throw new IllegalStateException("No bronhouder");
		}

		return "redirect:/ba/datasetconfig/" + bronhouder.getId();
	}

	@RequestMapping(value ="/ba/validation", method = RequestMethod.GET)
	public String determineValidationBronhouder(Principal principal) {
		final Bronhouder bronhouder = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao)
			.getAuthorizedBronhouder (null);

		// User is not bronhouder of any 'provincie'
		//TODO:MES: If this happens the views will break, because they expect a bronhouder to be available in the model
		if (bronhouder == null){
			throw new IllegalStateException("No bronhouder");
		}

		return "redirect:/ba/validation/" + bronhouder.getId();
	}

	@RequestMapping(value ="/ba/emailteksten", method = RequestMethod.GET)
	public String determineThemas(Principal principal) {
		List<Thema> themas = managerDao.getAllThemas ();

		if (themas.size() == 0){
			throw new IllegalStateException("No Themas");
		}
    	return "redirect:/ba/emailteksten/" + themas.get(0).getId();
	}
}
