/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import static nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils.getAttributeDescriptors;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.Valid;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobCreator;
import nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils;
import nl.ipo.cds.admin.ba.attributemapping.FeatureTypeCache;
import nl.ipo.cds.admin.ba.attributemapping.OperationFactory;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Mapping;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.admin.security.AuthzImpl;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.attributemapping.AttributeMappingDao;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.domain.*;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
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
	
	@Inject
	private FeatureTypeCache featureTypeCache;
	
	@Inject
	private OperationDiscoverer operationDiscoverer;
	
	@Inject
	private ConversionService conversionService;
	
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
		List<String> notAuthorizedThemaList = new ArrayList<String>();
		AuthzImpl authz = new AuthzImpl();
		if (authz.anyGranted("ROLE_BEHEERDER")){
			// add themas that are implicit in the datasetlist to the themalist
			// this happens when the datasetlist contains more datasets than the themaAuthorization allows
			for (Dataset dataset : datasetList) {
				Thema datasetThema = dataset.getDatasetType().getThema();
				if (!themaList.contains(datasetThema)){
					themaList.add(datasetThema);
					notAuthorizedThemaList.add(datasetThema.getNaam());
				}
			}
		}
		if (themaName != null && !themaName.isEmpty()){
			thema = this.managerDao.getThemaByName(themaName);
			if (!themaList.contains(thema)){
				// sometimes when switching to another bronhouder the themaName does not match a thema of the new bronhouder
				thema = themaList.get(0);
			}
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
		
		// Use BronhouderNAW instead of Bronhouder because of the need to flag when a bronhouder has more datasets than it is authorized for
		final List<BronhouderNAW> bronhouderNAWList ;
		bronhouderNAWList = new ArrayList<BronhouderNAW>();
		if (authz.anyGranted("ROLE_BEHEERDER")){
			// beheerder role sees all bronhouders
			final List<Bronhouder> bronhouderList = managerDao.getAllBronhouders();
			for (Bronhouder bronhouder2 : bronhouderList) {
				List<Thema> themaList1 = this.managerDao.getAllThemas(bronhouder2);
				List<Thema> themaList2 = new ArrayList<Thema>();
				List <Dataset> datasetList2 = this.managerDao.getDatasetsByBronhouder(bronhouder2);
				for (Dataset dataset2 : datasetList2) {
					Thema datasetThema = dataset2.getDatasetType().getThema();
					if (!themaList1.contains(datasetThema)){
						themaList2.add(datasetThema);
					}
				}
				if (themaList2.isEmpty()){
					bronhouderNAWList.add(new BronhouderNAW (bronhouder2, false));
				}else{
					//flag this bronhouder as having datasets it is not authorized to have
					bronhouderNAWList.add(new BronhouderNAW (bronhouder2, true));
				}
			}
		}else{
			// bronhouder role only sees one bronhouder
			bronhouderNAWList.add(new BronhouderNAW (bronhouder, false));
		}
		model.addAttribute("bronhouders", bronhouderNAWList);

		model.addAttribute("bronhouder", bronhouder);
		model.addAttribute("themaList", themaList);
		model.addAttribute("notAuthorizedThemaList", notAuthorizedThemaList);
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
		
		redirectAttributes.addAttribute ("thema", themaNaam);
		return "redirect:/ba/datasetconfig/" + bronhouder.getId();
	}

	@Transactional
	@RequestMapping(value ="/ba/tag_datasetconfig/{bronhouderId}", method = RequestMethod.GET)
	public String tagDataset(@ModelAttribute Bronhouder bronhouder,
								@RequestParam(value="datasetId")Long datasetId,
								@RequestParam(value="tag")String tag,
								Model model,
								final RedirectAttributes redirectAttributes) {

		Dataset dataset = managerDao.getDataSet(datasetId);

		final String themaNaam = dataset.getDatasetType().getThema().getNaam();
		boolean taggable = themeDiscoverer.getThemeConfiguration(themaNaam).isTaggable();
		redirectAttributes.addAttribute ("thema", themaNaam);

		// We only continue with tagging if the corresponding theme is taggable.
		if (!taggable) {
			// We probably never get here through the interface, because the tag button is not visible for themes
			// that are not taggable.
			return "redirect:/ba/datasetconfig/" + bronhouder.getId();
		}

		// Tagging of a dataset is done using an ETL job "TagJob" and is not immediate.
		final TagJob tagJob = new TagJob();
		tagJob.setBronhouder(bronhouder);
		tagJob.setDatasetType(dataset.getDatasetType());
		tagJob.setUuid(dataset.getUuid());
		tagJob.setTag(tag);

		jobCreator.putJob (tagJob);

		// TODO: Is this required for tag jobs?
		/* Check whether to create a transform Job, by checking if there is already a TRANSFORM job that
		 * hasn't started yet
		 */
		/*if(this.managerDao.getLastTransformJob(Job.Status.CREATED) == null){
			final TransformJob transformJob = new TransformJob ();
			managerDao.create (transformJob);
		}*/

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
	@Transactional
	public String addDataset(@ModelAttribute Bronhouder bronhouder, 
			@ModelAttribute("datasetForm") @Valid DatasetForm datasetForm,			
			BindingResult bindingResult,
			Model model,
			final RedirectAttributes redirectAttributes) throws ThemeNotFoundException, HarvesterException, MappingParserException {
		
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
		// configure any dataset mapping templates
		configureMappings(dataset);
				
		redirectAttributes.addAttribute ("thema", datasetForm.getThema ());
		return "redirect:/ba/datasetconfig/" + bronhouder.getId();
	}

	/**
	 * Configure and persist any template mappings associated with the dataset.
	 * 
	 * @param dataset
	 * @throws ThemeNotFoundException 
	 * @throws HarvesterException 
	 * @throws MappingParserException 
	 */
	private void configureMappings(Dataset dataset) throws ThemeNotFoundException, HarvesterException, MappingParserException {
		final Set<AttributeDescriptor<?>> attributeDescriptors = getAttributeDescriptors(themeDiscoverer, dataset);
		final FeatureType featureType = featureTypeCache.getFeatureType(dataset);
		final AttributeMappingDao dao = new AttributeMappingDao(managerDao);
		final ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration(dataset.getDatasetType().getThema()
				.getNaam());

		for (AttributeDescriptor<?> attributeDescriptor : attributeDescriptors) {
			 final Mapping mapping = themeConfig.getDefaultMappingForAttributeType(attributeDescriptor);
			if (mapping==null) {
				// skip mapping if no template is available
				continue;
			}
			// Convert mapping to an operation tree used in the dao:
			final OperationFactory factory = new OperationFactory (attributeDescriptor, operationDiscoverer.getOperationTypes(), featureType, conversionService);
			
			 final OperationDTO operationTree = factory.buildOperationCommand (mapping);
			// Determine whether the mapping is valid:
			final OperationDTO rootOperation = (OperationDTO) operationTree.getInputs().get(0).getOperation();
			final boolean isValid = AttributeMappingUtils.isMappingValid(rootOperation, attributeDescriptor,
					featureType);

			// Save the mapping:
			dao.putAttributeMapping(dataset, attributeDescriptor, rootOperation, isValid);
		}
	}
}
