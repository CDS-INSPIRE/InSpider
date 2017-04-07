package nl.ipo.cds.admin.ba.controller;

import nl.idgis.commons.jobexecutor.JobCreator;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.TagDao;
import nl.ipo.cds.domain.*;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ba/vaststellen")
public class TagDatasetController {

	@Autowired
	private ManagerDao managerDao;

	@Autowired
	private TagDao tagDao;

	@Autowired
	private ReportConfiguration reportConfiguration;

	@Inject
	private ThemeDiscoverer themeDiscoverer;

	@Autowired
	private JobCreator jobCreator;

	@RequestMapping(method = RequestMethod.GET)
	public String Index(Model model) {
		model.addAttribute("themas", getVaststelThemas());
		return "/ba/vaststellen";
	}

	private List<Thema> getVaststelThemas() {
		ArrayList<Thema> taggableThemes = new ArrayList<Thema>();
		for (Thema thema : this.managerDao.getAllThemas()) {
			if (this.themeDiscoverer.getThemeConfiguration(thema.getNaam()).isTaggable()) {
				taggableThemes.add(thema);
			}
		}
		return taggableThemes;

	}

	@Transactional
	@RequestMapping(method = RequestMethod.POST)
	public String tagTheme(@Valid @ModelAttribute("dto") TagDTO dto, Model model, final Principal principal) {
		model.addAttribute("themas", getVaststelThemas());
		
		// check if thema is taggable
		ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration(dto.getThema());
		Assert.notNull(themeConfig, "Theme with name " + dto.getThema() + " does not exist.");
		if(!themeConfig.isTaggable()){
			model.addAttribute("themaError", "Thema " + themeConfig.getThemeName() + " kan niet worden vastgesteld!");
			return "/ba/vaststellen";
		}
		
		// check that user is authorized to tag this thema (check typeGebruik and Thema authorisatie)
		Gebruiker gebruiker = managerDao.getGebruiker(principal.getName());
		if (!gebruiker.isSuperuser()) {
			boolean authorized = false;
			Thema thema = managerDao.getThemaByName(dto.getThema());
			List<GebruikerThemaAutorisatie> listAuthorisatie = managerDao.getGebruikerThemaAutorisatie(gebruiker);
			for (GebruikerThemaAutorisatie gebruikerThemaAutorisatie : listAuthorisatie) {
				if (gebruikerThemaAutorisatie.getBronhouderThema().getThema().equals(thema) &&
						gebruikerThemaAutorisatie.getTypeGebruik().isAllowed(TypeGebruik.VASTSTELLER)) {
					authorized = true;
					break; // Short-circuit.
				}
			}
			if (!authorized) {
				model.addAttribute("authError", "Deze gebruiker heeft niet de rechten om dit thema vast te stellen");
				return "/ba/vaststellen";
			}
		}
		
		// check if there is a job with the same tag already
		// Also check in manager.job (joined with manager.etljob) table for a job that has the chosen tag in its parameters 
		//and does have either one of the following status: CREATED, PREPARED, STARTED. (FINISHED and ABORTED jobs can be ignored).
		Table table = themeConfig.getFeatureTypeClass().getAnnotation(Table.class);
		Assert.notNull(table, "table Annotation could not be determined for thema " + themeConfig.getFeatureTypeClass());
		if(tagDao.doesTagExist(dto.tagId, table.schema(), table.name())|| tagDao.doesTagJobWithIdExist(dto.getTagId()
				, dto.getThema())){
			model.addAttribute("tagIdError", "Het vaststel id " + dto.getTagId()+ " bestaat al!");
			return "/ba/vaststellen";
		}

		final TagJob tagJob = new TagJob();
		tagJob.setTag(dto.getTagId());
		tagJob.setThema(dto.getThema());
		jobCreator.putJob(tagJob);
		model.addAttribute("success", "Het thema " + dto.getThema() + " zal worden vastgesteld met id " + dto.getTagId());
		return "/ba/vaststellen";
		
	}

}
