/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Rob
 *
 */
@Controller
@RequestMapping("/ba/jobdetails")
public class JobDetailsController {
	@RequestMapping(method = RequestMethod.GET)
	public String index () {
		return "/ba/jobdetails";
	}


}
