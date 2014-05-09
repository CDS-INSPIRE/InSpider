/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobCreator;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.admin.security.AuthzImpl;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.RemoveJob;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TransformJob;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller voor de dataset configuratie pagina.<br/>
 * @author Rob
 *
 */
@Controller
public class DatasetController{

	@Autowired
	private ManagerDao managerDao;
	
	@Autowired
	private JobCreator jobCreator;
	
	@Autowired
	private ReportConfiguration reportConfiguration;

	@Inject
	private ThemeDiscoverer themeDiscoverer;
	
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
	
	@RequestMapping(value ="/ba/datasetconfig/{bronhouderId}", method = RequestMethod.GET)
	public String updateDatasetForm(@ModelAttribute Bronhouder bronhouder, @RequestParam(value="thema", required=false) String themaName, Model model) throws ThemeNotFoundException {
		// get all datasets from a bronhouder and put them in a map
		List <Dataset> datasetList = this.managerDao.getDatasetsByBronhouder(bronhouder);
		
		// get the current thema
		Thema thema = null ;
		List<Thema> themaList = this.managerDao.getAllThemas(bronhouder);
		if (themaName != null && !themaName.isEmpty()){
			thema = this.managerDao.getThemaByName(themaName);
		}else{
			thema = themaList.get(0);
		}
		
		// List the attributes that are available in the theme:
		final ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration (thema.getNaam ());
		if (themeConfig == null) {
			throw new ThemeNotFoundException (thema.getNaam ());
		}
		final int totalAttributeCount = themeConfig.getAttributeDescriptors ().size ();
		
		// a list of datasets for the current bronhouder and the current thema
		List<BronhouderDataset> bronhouderDatasetList = new ArrayList<BronhouderDataset>();		
		for (Dataset dataset : datasetList) {
			if (dataset.getDatasetType().getThema().equals(thema)){
				bronhouderDatasetList.add(new BronhouderDataset(
					dataset,
					managerDao.getValidAttributeMappings (dataset).size (),
					totalAttributeCount
				));
			}
		}
		
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

		model.addAttribute("bronhouder", bronhouder);
		model.addAttribute("themaList", themaList);
		model.addAttribute("currentThema", thema);
		model.addAttribute("bronhouderDatasetList", bronhouderDatasetList);
		model.addAttribute("pgrBaseUrl", reportConfiguration.getPgrBaseUrl ());
        return "/ba/datasetconfig";
	}
	
	/**
	 * Performs actions of the Dataset configuration view.<br/>
	 * A dataset can be removed, added, updated (uuid) and made (in)active.<br/>
	 * The proper working depends on the lists having the order in which items appear on the html form.
	 * @return view url
	 */
	@RequestMapping(value ="/ba/datasetconfig/{bronhouderId}", method = RequestMethod.POST)
	public String updateDataset(@PathVariable Long bronhouderId, 
			@RequestParam(value="thema", required=true) String themaName,  
			@ModelAttribute BronhouderDatasetForm  bronhouderDatasetForm,
			Model model,
			RedirectAttributes redirectAttributes) {

		for (BronhouderDataset bronhouderDataset : bronhouderDatasetForm.getBronhouderDatasets()) {			
			Dataset dataset = this.managerDao.getDataSet(bronhouderDataset.getId());
			
			if (dataset.getBronhouder().getId().equals(bronhouderId)) {
				if (dataset.getDatasetType().getNaam().equals(bronhouderDataset.getType())) {
					dataset.setNaam(bronhouderDataset.getNaam());
					dataset.setActief(bronhouderDataset.isActief());
					dataset.setUuid(bronhouderDataset.getUuid());
					this.managerDao.update(dataset);
				}
			}
		}
	
        // Redirect after POST pattern
		redirectAttributes.addAttribute ("thema", themaName);
        return "redirect:/ba/datasetconfig/" + bronhouderId;
	}
	
	@Transactional
	@RequestMapping(value ="/ba/remove_datasetconfig/{bronhouderId}", method = RequestMethod.GET)
	public String removeDataset(@ModelAttribute Bronhouder bronhouder,
			@RequestParam(value="datasetId", required=false)Long datasetId,
			Model model,
			final RedirectAttributes redirectAttributes) {

		Dataset dataset = this.managerDao.getDataSet(datasetId);
		
		final String themaNaam = dataset.getDatasetType ().getThema ().getNaam ();
		
		// NB deleten van dataset gebeurt in ETL-proces dmv een delete job
		final RemoveJob deleteJob = new RemoveJob ();
		deleteJob.setBronhouder(bronhouder);
		deleteJob.setDatasetType(dataset.getDatasetType());
		deleteJob.setUuid(dataset.getUuid());
		jobCreator.putJob (deleteJob);

		/* Check whether to create a transform Job, by checking if there is already a TRANSFORM job that
		 * hasn't started yet
		 */
		if(this.managerDao.getLastTransformJob(Job.Status.CREATED) == null){
			final TransformJob transformJob = new TransformJob ();
			managerDao.create (transformJob);
		}
		
//		this.managerDao.delete(dataset);// do this in RemoveJob
		

		redirectAttributes.addAttribute ("thema", themaNaam);
		return "redirect:/ba/datasetconfig/" + bronhouder.getId();
	}
	
	@RequestMapping(value ="/ba/add_datasetconfig/{bronhouderId}", method = RequestMethod.GET)
	public String addDatasetForm(@ModelAttribute Bronhouder bronhouder,
			@RequestParam(value="thema", required=true) String themaName,
			Model model) {		
 
		Thema thema = managerDao.getThemaByName(themaName);
		// check thema
		List<Thema> themaList = this.managerDao.getAllThemas(bronhouder);
		if (!themaList.contains(thema)){
			// if the bronhouder does not have the current thema then get the first thema from bronhouder
			thema = themaList.get(0);
		}
		
		List<DatasetType> datasetTypes = this.managerDao.getDatasetTypesByThema(thema);
		
		model.addAttribute("bronhouder", bronhouder);
		model.addAttribute("thema", thema);
		model.addAttribute("datasetTypes", datasetTypes);
		model.addAttribute("viewName", "/ba/datasetconfig");
		
		if(!model.containsAttribute("datasetForm")) {
			model.addAttribute("datasetForm", new DatasetForm());
		}
		
		return "ba/add_datasetconfig";
	}
	
	@RequestMapping(value ="/ba/add_datasetconfig/{bronhouderId}", method = RequestMethod.POST)
	public String addDataset(@ModelAttribute Bronhouder bronhouder, 
			@ModelAttribute("datasetForm") @Valid DatasetForm datasetForm,			
			BindingResult bindingResult,
			Model model,
			final RedirectAttributes redirectAttributes) {
		
		if(bindingResult.hasErrors()) {
			return addDatasetForm(bronhouder, datasetForm.getThema(), model);
		}
		
		Dataset dataset = new Dataset();
		dataset.setActief(true);
		dataset.setBronhouder(bronhouder);
		dataset.setUuid(datasetForm.getUuid());
		dataset.setNaam(datasetForm.getNaam());
		dataset.setDatasetType(this.managerDao.getDatasetType(datasetForm.getDatasettypeId()));
		this.managerDao.create(dataset);		

		redirectAttributes.addAttribute ("thema", datasetForm.getThema ());
		return "redirect:/ba/datasetconfig/" + bronhouder.getId();
	}
	
}
