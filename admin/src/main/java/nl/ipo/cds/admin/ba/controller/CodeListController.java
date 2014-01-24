package nl.ipo.cds.admin.ba.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.CodeListMapping;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping ("/ba/codelists")
public class CodeListController {

	@Inject
	private ManagerDao managerDao;
	
	@Inject
	private Set<ThemeConfig<?>> themeConfigurations;
	
	private List<String> codeSpaces;
	
	@PostConstruct
	public void discoverCodeSpaces () {
		SortedSet<String> codeSpaces = new TreeSet<String> ();
		
		for (final ThemeConfig<?> themeConfig: themeConfigurations) {
			for (final AttributeDescriptor<?> ad: themeConfig.getAttributeDescriptors ()) {
				final String codeSpace = ad.getCodeSpace ();
				if (codeSpace != null) {
					codeSpaces.add (codeSpace);
				}
			}
		}
		
		this.codeSpaces = new ArrayList<String>(codeSpaces);
	}

	@ModelAttribute ("roleFunction")
	public String getRoleFunction(){
		return "beheerder";
	}
	
	@ModelAttribute ("codeSpaces")
	public List<String> getCodeSpaces () {
		return codeSpaces;
	}
	
	@RequestMapping (method = RequestMethod.GET)
	public String codeListsForm (final Model model) {

		model.addAttribute ("mappings", getCurrentMappings ());
		
		return "/ba/codelists/form";
	}
	
	@RequestMapping (method = RequestMethod.POST)
	@Transactional
	public String postCodeListForm (final Form form, final Model model) {
		final Map<String, String> mappings = new HashMap<String, String> (form.getMappings ());
		final List<CodeListMapping> currentMappings = managerDao.getCodeListMappings ();
		
		// Update or delete existing mappings:
		for (final CodeListMapping mapping: currentMappings) {
			if (mappings.containsKey (mapping.getCodeSpace ())) {
				final String value = mappings.get (mapping.getCodeSpace ()).trim ();
				
				if (!value.isEmpty ()) {
					mapping.setUrl (value);
					managerDao.update (mapping);
				} else {
					managerDao.delete (mapping);
				}
				
				mappings.remove (mapping.getCodeSpace ());
			} else {
				managerDao.delete (mapping);
			}
		}
		
		// Insert new mappings:
		for (final Map.Entry<String, String> entry: mappings.entrySet ()) {
			managerDao.create (new CodeListMapping (entry.getKey (), entry.getValue ()));
		}
		
		model.asMap ().clear ();
		
		return "redirect:/ba/codelists";
	}
	
	private Map<String, String> getCurrentMappings () {
		final Map<String, String> mappings = new HashMap<String, String> ();
		
		for (final CodeListMapping mapping: managerDao.getCodeListMappings ()) {
			mappings.put (mapping.getCodeSpace (), mapping.getUrl ());
		}
		
		return mappings;
	}
	
	public static class Form {
		private Map<String, String> mappings;

		public Map<String, String> getMappings() {
			return mappings;
		}

		public void setMappings(Map<String, String> mappings) {
			this.mappings = mappings;
		}
	}
}
