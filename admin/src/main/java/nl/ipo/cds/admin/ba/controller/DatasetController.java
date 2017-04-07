/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import static nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils.getAttributeDescriptors;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.validation.Valid;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobCreator;
import nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils;
import nl.ipo.cds.admin.ba.attributemapping.FeatureTypeCache;
import nl.ipo.cds.admin.ba.attributemapping.OperationFactory;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Mapping;
import nl.ipo.cds.admin.ba.util.GebruikerAuthorization;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.attributemapping.AttributeMappingDao;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.BronhouderThema;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.domain.RefreshPolicy;
import nl.ipo.cds.domain.RemoveJob;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TransformJob;
import nl.ipo.cds.domain.TypeGebruik;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
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

	/**
	 * Returns all datasets for the given bronhouder and theme.
	 * 
	 * @param bronhouder		The bronhouder to use as a filter.
	 * @param thema				The theme to use as a filter.
	 * @return					All datasets for the given bronhouder and theme.
	 */
	private List<Dataset> getDatasetsByBronhouderAndThema (final Bronhouder bronhouder, final Thema thema) {
		final List<Dataset> result = new ArrayList<Dataset> ();
		
		for (final Dataset dataset: managerDao.getDatasetsByBronhouder (bronhouder)) {
			if (dataset.getDatasetType ().getThema ().equals (thema)) {
				result.add (dataset);
			}
		}
		
		return Collections.unmodifiableList (result);
	}
	
	/**
	 * Returns a list of bronhouders that are authorized to use themes, or that have
	 * existing datasets.
	 * 
	 * @return
	 */
	private Collection<Bronhouder> getBronhoudersWithThemesOrDatasets () {
		final SortedSet<Bronhouder> bronhouders = new TreeSet<Bronhouder> (new Comparator<Bronhouder> () {
			@Override
			public int compare (final Bronhouder o1, final Bronhouder o2) {
				return o1.getNaam ().compareTo (o2.getNaam ());
			}
		});
		
		// Add bronhouders that are associated with a theme:
		for (final BronhouderThema bronhouderThema: managerDao.getBronhouderThemas ()) {
			bronhouders.add (bronhouderThema.getBronhouder ());
		}
		
		// Add bronhouders that are associated with a dataset:
		for (final Dataset dataset: managerDao.getAllDatasets ()) {
			bronhouders.add (dataset.getBronhouder ());
		}
		
		return Collections.unmodifiableCollection (bronhouders);
	}
	
	/**
	 * Returns a list of themes for which the given bronhouder either has permissions to use
	 * it or existing datasets for that bronhouder exist which are related to the theme.
	 * 
	 * @param bronhouder	The bronhouder to use as a filter.
	 * @return				A collection of themes.
	 */
	private Collection<Thema> getThemesWithBronhouderOrDatasets (final Bronhouder bronhouder, final Collection<Thema> unauthorizedThemes) {
		final SortedSet<Thema> themas = new TreeSet<Thema> (new Comparator<Thema> () {
			@Override
			public int compare (final Thema o1, final Thema o2) {
				return o1.getNaam ().compareTo (o2.getNaam ());
			}
		});
		
		// Add themes that are directly associated with the given bronhouder:
		themas.addAll (managerDao.getAllThemas (bronhouder));
		
		// Add themes that are associated with a dataset for this bronhouder:
		for (final Dataset dataset: managerDao.getDatasetsByBronhouder (bronhouder)) {
			final Thema thema = dataset.getDatasetType ().getThema ();
			if (!themas.contains (thema)) {
				unauthorizedThemes.add (dataset.getDatasetType ().getThema ());
				themas.add (dataset.getDatasetType ().getThema ());
			}
		}
		
		return Collections.unmodifiableCollection (themas);
	}
	
	@RequestMapping(value ="/ba/datasetconfig/{bronhouderId}", method = RequestMethod.GET)
	public String updateDatasetForm(final @PathVariable("bronhouderId") long bronhouderId, @RequestParam(value="thema", required=false) String themaName, Model model, final Principal principal) throws ThemeNotFoundException {
		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao);
		
		// Get bronhouder:
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);
		if (bronhouder == null) {
			return "redirect:/ba";
		}
		
		// Get the current thema:
		final Thema thema = gebruikerAuthorization.getAuthorizedThemaByName (themaName, bronhouder);

		// get all datasets from a bronhouder and put them in a map
		final List<Dataset> datasetList = getDatasetsByBronhouderAndThema (bronhouder, thema);
		
		// List themes:
		final Collection<Thema> themaList;
		final Collection<String> notAuthorizedThemaList = new ArrayList<String> ();
		if (gebruikerAuthorization.getGebruiker ().isSuperuser ()) {
			final List<Thema> unauthorizedThemas = new ArrayList<Thema> ();
			
			themaList = getThemesWithBronhouderOrDatasets (bronhouder, unauthorizedThemas);
			
			// add themas that are implicit in the datasetlist to the themalist
			// this happens when the datasetlist contains more datasets than the themaAuthorization allows
			for (final Thema t: unauthorizedThemas) {
				notAuthorizedThemaList.add (t.getNaam ());
			}
		} else {
			themaList = gebruikerAuthorization.getAuthorizedThemas (bronhouder);
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
			bronhouderDatasetList.add(new BronhouderDataset(
				dataset,
				managerDao.getValidAttributeMappings (dataset).size (),
				totalAttributeCount
			));
		}
		
		/** check the authorization of the current user
		 *  fill the bronhouders list depending on the role
		 *  i.e. only the current bronhouder for role bronhouder 
		 *  or all bronhouders for role beheerder 
		 */
		
		// Use BronhouderNAW instead of Bronhouder because of the need to flag when a bronhouder has more datasets than it is authorized for
		final List<BronhouderNAW> bronhouderNAWList = new ArrayList<BronhouderNAW>();
		
		if (gebruikerAuthorization.getGebruiker ().isSuperuser ()) {
			for (Bronhouder bronhouder2 : getBronhoudersWithThemesOrDatasets ()) {
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
		} else {
			for (final Bronhouder b: gebruikerAuthorization.getAuthorizedBronhouders ()) {
				bronhouderNAWList.add (new BronhouderNAW (b, false));
			}
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
	@Transactional
	@RequestMapping(value ="/ba/datasetconfig/{bronhouderId}", method = RequestMethod.POST)
	public String updateDataset(@PathVariable Long bronhouderId, 
			@RequestParam(value="thema", required=true) String themaName,  
			@ModelAttribute BronhouderDatasetForm  bronhouderDatasetForm,
			Model model,
			RedirectAttributes redirectAttributes,
			RefreshPolicy refreshPolicy) { //w1502 019

		for (BronhouderDataset bronhouderDataset : bronhouderDatasetForm.getBronhouderDatasets()) {			
			Dataset dataset = this.managerDao.getDataSet(bronhouderDataset.getId());
			
			if (dataset.getBronhouder().getId().equals(bronhouderId)) {
				if (dataset.getDatasetType().getNaam().equals(bronhouderDataset.getType())) {
					dataset.setNaam(bronhouderDataset.getNaam());
					dataset.setActief(bronhouderDataset.isActief());
					// W1502 019
					dataset.setRefreshPolicy(bronhouderDataset.getRefreshPolicy());
					final String oldUuid = dataset.getUuid();
					final String newUuid = bronhouderDataset.getUuid();
					if (!oldUuid.equals(newUuid)) {
						dataset.setUuid(newUuid);
						removeAndReimportDataAfterUuidChange(dataset, oldUuid, newUuid);
					}
					this.managerDao.update(dataset);
				}
			}
		}
	
        // Redirect after POST pattern
		redirectAttributes.addAttribute ("thema", themaName);
        return "redirect:/ba/datasetconfig/" + bronhouderId;
	}

	private void removeAndReimportDataAfterUuidChange(final Dataset dataset, final String oldUuid, final String newUuid ) {

		final RemoveJob deleteJob = new RemoveJob ();
		deleteJob.setBronhouder(dataset.getBronhouder());
		deleteJob.setDatasetType(dataset.getDatasetType());
		deleteJob.setUuid(oldUuid);
		jobCreator.putJob (deleteJob);

		final ImportJob job = new ImportJob ();
		job.setBronhouder(dataset.getBronhouder());
		job.setDatasetType(dataset.getDatasetType());
		job.setUuid(newUuid);
		job.setForceExecution(true);
		jobCreator.putJob (job);

		if(this.managerDao.getLastTransformJob(Job.Status.CREATED) == null){
			final TransformJob transformJob = new TransformJob ();
			managerDao.create (transformJob);
		}
	}
	
	@Transactional
	@RequestMapping(value ="/ba/remove_datasetconfig/{bronhouderId}", method = RequestMethod.GET)
	public String removeDataset(final @PathVariable("bronhouderId") long bronhouderId,
			@RequestParam(value="datasetId", required=false)Long datasetId,
			Model model,
			final RedirectAttributes redirectAttributes,
			final Principal principal) {

		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao);
		
		// Get bronhouder:
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);
		if (bronhouder == null) {
			return "redirect:/ba";
		}
		
		Dataset dataset = this.managerDao.getDataSet(datasetId);
		
		final String themaNaam = dataset.getDatasetType ().getThema ().getNaam ();
		
		// NB deleten van dataset gebeurt in ETL-proces dmv een delete job
		final RemoveJob deleteJob = new RemoveJob ();
		deleteJob.setBronhouder(bronhouder);
		deleteJob.setDatasetType(dataset.getDatasetType());
		deleteJob.setUuid(dataset.getUuid());
		jobCreator.putJob (deleteJob);
		
		managerDao.delete (dataset);

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

	@RequestMapping(value ="/ba/add_datasetconfig/{bronhouderId}", method = RequestMethod.GET)
	public String addDatasetForm(final @PathVariable("bronhouderId") long bronhouderId,
			@RequestParam(value="thema", required=true) String themaName,
			Model model,
			final Principal principal) {
		
		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao);
		
		// Get bronhouder:
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);
		if (bronhouder == null) {
			return "redirect:/ba";
		}
 
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
	public String addDataset(final @PathVariable("bronhouderId") long bronhouderId, 
			@ModelAttribute("datasetForm") @Valid DatasetForm datasetForm,			
			BindingResult bindingResult,
			Model model,
			final RedirectAttributes redirectAttributes,
			final Principal principal) throws ThemeNotFoundException, HarvesterException, MappingParserException {
		
		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao);
		
		// Get bronhouder:
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);

		if (bronhouder == null) {
			return "redirect:/ba";
		}
		
		if(bindingResult.hasErrors()) {
			return addDatasetForm(bronhouderId, datasetForm.getThema(), model, principal);
		}
		
		// default refresh policy value mast be "MANUAL" W1502 019
		Dataset dataset = new Dataset();
	
		dataset.setActief(true);
		dataset.setBronhouder(bronhouder);
		dataset.setUuid(datasetForm.getUuid());
		dataset.setNaam(datasetForm.getNaam());
		//W1502 019
		dataset.setRefreshPolicy(RefreshPolicy.MANUAL);
				
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
