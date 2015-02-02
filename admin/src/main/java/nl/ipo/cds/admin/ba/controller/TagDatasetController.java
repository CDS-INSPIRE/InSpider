package nl.ipo.cds.admin.ba.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import nl.idgis.commons.jobexecutor.JobCreator;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.TagDao;
import nl.ipo.cds.domain.TagDTO;
import nl.ipo.cds.domain.TagJob;
import nl.ipo.cds.domain.Thema;
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
	public String tagTheme(@Valid @ModelAttribute("dto") TagDTO dto, Model model) {
		model.addAttribute("themas", getVaststelThemas());
		
		// check if thema is taggable
		ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration(dto.getThema());
		Assert.notNull(themeConfig, "Theme with name " + dto.getThema() + " does not exist.");
		//Assert.isTrue(themeConfig.isTaggable(), "Theme " + themeConfig.getThemeName() + " is not taggable!");
		if(!themeConfig.isTaggable()){
			model.addAttribute("themaError", "Thema " + themeConfig.getThemeName() + " kan niet worden vastgesteld!");
			return "/ba/vaststellen";
		}
		// FIXME TODO check that user is authorized to tag this thema

		// check if there is a job with the same tag already
		// Also check in manager.job (joined with manager.etljob) table for a job that has the chosen tag in its parameters 
		//and does have either one of the following status: CREATED, PREPARED, STARTED. (FINISHED and ABORTED jobs can be ignored).
		Table table = themeConfig.getFeatureTypeClass().getAnnotation(Table.class);
		Assert.notNull(table, "table Annotation could not be determined for thema " + themeConfig.getFeatureTypeClass());
		if(tagDao.doesTagExist(dto.tagId, table.schema(), table.name())|| tagDao.doesTagJobWithIdExist(dto.getTagId())){
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
