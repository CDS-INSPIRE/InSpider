package nl.ipo.cds.admin.ba.controller;

import java.security.Principal;
import java.util.List;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Thema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class EmailTekstenController {

	@Autowired
	private ManagerDao managerDao;
	
	@ModelAttribute("roleFunction")
	String getRoleFunction(){
		return "beheerder";
	}
	
	@ModelAttribute("thema")
	public Thema populateThema (@PathVariable long themaId, Principal principal) {
		final Thema thema = managerDao.getThema(themaId);
		return thema;
	}
	
	@RequestMapping(value ="/ba/emailteksten/{themaId}", method = RequestMethod.GET)
	public String updateThemaForm(@ModelAttribute Thema thema,  Model model, final Principal principal) {
		if (thema == null) {
			return "redirect:/ba";
		}
		
        model.addAttribute("updateThemaForm", new ThemaForm (thema));        
        final List<Thema> themas = managerDao.getAllThemas ();       
		model.addAttribute("themas", themas);
		model.addAttribute("thema", thema);
		
        return "/ba/emailteksten";
	}
	
	@RequestMapping(value ="/ba/emailteksten/{themaId}", method = RequestMethod.POST)
	public String updateThema(@ModelAttribute Thema thema, Model model) {
		if (thema == null) {
			return "redirect:/ba";
		}
		
		this.managerDao.update(thema);
        model.addAttribute("updateThemaForm", new ThemaForm (thema));
        final List<Thema> themas = managerDao.getAllThemas ();
        model.addAttribute ("themas", themas);
        return "redirect:/ba/emailteksten/" + thema.getId();
	}
}
