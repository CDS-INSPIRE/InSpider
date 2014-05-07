/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller voor de Downloadservice pagina <br/>
 * 
 * @author Rob
 * 
 */
@Controller
public class DownloadserviceController {

	@Autowired
	private DownloadServiceLinkList downloadLinkList;
	
	
	@ModelAttribute("roleFunction")
	String getRoleFunction() {
		return "beheerder";
	}

	@RequestMapping(value = "/ba/downloadservice", method = RequestMethod.GET)
	public String Index(Model model) {
		if (downloadLinkList==null){
			downloadLinkList = new DownloadServiceLinkList();
		}
		model.addAttribute("dlLinkList", downloadLinkList);
		return "/ba/downloadservice";
	}

}
