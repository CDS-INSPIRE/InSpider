package nl.ipo.cds.admin.ba.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import nl.idgis.commons.jobexecutor.JobCreator;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.dao.DatasetCriteria;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.TagDTO;
import nl.ipo.cds.domain.TagJob;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/ba/vaststellen")
public class TagDatasetController {
	
	@Autowired
	private ManagerDao managerDao;

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
		 boolean taggable = themeDiscoverer.getThemeConfiguration(dto.getThema()).isTaggable();
		 if(!taggable){
			 throw new IllegalArgumentException("The theme you try to tag is not taggable!" );
		 } else{
			// check if there is a job with the same tag already 
			 //managerDao.findDataset(DatasetCriteria)
			 
			/*//Check whether to create a transform Job, by checking if there is already a TRANSFORM job that hasn't started yet 
			 if(this.managerDao.getLastTransformJob(Job.Status.CREATED) == null){ final TransformJob transformJob = new TransformJob ();
			 managerDao.create (transformJob); }*/
			 
			 final TagJob tagJob = new TagJob();
			 tagJob.setTag(dto.getTagId());
			 jobCreator.putJob(tagJob);
		 }
		 return "redirect:/ba/vaststellen/";
	}

}
