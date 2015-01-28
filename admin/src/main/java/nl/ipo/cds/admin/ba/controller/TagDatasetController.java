package nl.ipo.cds.admin.ba.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import nl.idgis.commons.jobexecutor.Job.Status;
import nl.idgis.commons.jobexecutor.JobCreator;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.TagDao;
import nl.ipo.cds.domain.TagDTO;
import nl.ipo.cds.domain.TagJob;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TransformJob;
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
		// check if thema is taggable
		ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration(dto.getThema());
		Assert.notNull(themeConfig, "Theme with name " + dto.getThema() + " does not exist.");
		Assert.isTrue(themeConfig.isTaggable(), "Theme " + themeConfig.getThemeName() + " is not taggable!");

		// FIXME TODO check that user is authorized to tag this thema

		// check if there is a job with the same tag already
		Table table = themeConfig.getFeatureTypeClass().getAnnotation(Table.class);
		Assert.notNull(table, "table Annotation could not be determined for thema " + themeConfig.getFeatureTypeClass());
		Assert.isTrue(!tagDao.doesTagExist(dto.tagId, table.schema(), table.name()), "the tag " + dto.getTagId()
		+ " already exists!");

		// Check whether to create a transform Job, by checking if there is already a TRANSFORM job that hasn't started yet
		if (this.managerDao.getLastTransformJob(Status.CREATED) == null) {
			final TransformJob transformJob = new TransformJob();
			managerDao.create(transformJob);
		}
		final TagJob tagJob = new TagJob();
		tagJob.setTag(dto.getTagId());
		jobCreator.putJob(tagJob);
		return "redirect:/ba/vaststellen/";
	}

}
