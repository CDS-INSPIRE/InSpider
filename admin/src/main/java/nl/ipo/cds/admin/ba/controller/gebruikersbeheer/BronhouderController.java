package nl.ipo.cds.admin.ba.controller.gebruikersbeheer;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import nl.ipo.cds.admin.ba.controller.gebruikersbeheer.beans.BronhouderForm;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller to support CRUD operations on "Bronhouders" in the admin. 
 */
@Controller
@RequestMapping ("/ba/gebruikersbeheer/bronhouders")
public final class BronhouderController {

	@Inject
	private ManagerDao managerDao;
	
	/**
	 * Index page: lists bronhouders.
	 */
	@RequestMapping (method = RequestMethod.GET)
	public String listBronhouders (final Model model) {
		final List<Bronhouder> bronhouders = managerDao.getAllBronhouders ();
		
		model.addAttribute ("bronhouders", bronhouders);
	
		return "/ba/gebruikersbeheer/bronhouders";
	}

	/**
	 * Displays the form for creating a new bronhouder.
	 */
	@RequestMapping (value = "/-/create", method = RequestMethod.GET)
	public String createBronhouderForm (final Model model) {
		model.addAttribute ("bronhouderForm", new BronhouderForm ());
		return "/ba/gebruikersbeheer/edit-bronhouder";
	}
	
	/**
	 * Processes a POST of the create bronhouder form. 
	 */
	@RequestMapping (value = "/-/create", method = RequestMethod.POST)
	@Transactional
	public String processCreateBronhouderForm (
			final @Valid BronhouderForm bronhouderForm, 
			final BindingResult bindingResult, 
			final Model model) {
		
		// Run additional validations:
		validateBronhouderForm (bindingResult, null);
		
		// Show the form again if it has errors:
		if (bindingResult.hasErrors ()) {
			return "/ba/gebruikersbeheer/edit-bronhouder";
		}
		
		// Save the bronhouder:
		managerDao.create (copyBronhouderForm (bronhouderForm, new Bronhouder ()));
		
		return "redirect:/ba/gebruikersbeheer/bronhouders";
	}

	/**
	 * Displays the form for editing bronhouders.
	 * 
	 * @param bronhouderId	The ID of the bronhouder to edit.
	 */
	@RequestMapping (value = "/{bronhouderId}/edit", method = RequestMethod.GET)
	public String editBronhouderForm (final @PathVariable("bronhouderId") long bronhouderId, final Model model) {
		final Bronhouder bronhouder = managerDao.getBronhouder (bronhouderId);
		if (bronhouder == null) {
			return "redirect:/ba/gebruikersbeheer/bronhouders";
		}
		
		model.addAttribute ("bronhouderForm", new BronhouderForm (bronhouder));
		
		return "/ba/gebruikersbeheer/edit-bronhouder";
	}

	/**
	 * Processes a POST of the bronhouder edit form.
	 * 
	 * @param bronhouderId The ID of the bronhouder to save.
	 */
	@RequestMapping (value = "/{bronhouderId}/edit", method = RequestMethod.POST)
	@Transactional
	public String processEditBronhouderForm (
			final @PathVariable ("bronhouderId") long bronhouderId,
			final @Valid BronhouderForm bronhouderForm,
			final BindingResult bindingResult) {
		
		final Bronhouder bronhouder = managerDao.getBronhouder (bronhouderId);
		if (bronhouder == null) {
			return "redirect:/ba/gebruikersbeheer/bronhouders";
		}
		
		// Run additional validations on the bronhouder form:
		validateBronhouderForm (bindingResult, bronhouder);
		
		// Display the form again in case of errors:
		if (bindingResult.hasErrors ()) {
			return "/ba/gebruikersbeheer/edit-bronhouder";
		}
		
		// Save the bronhouder:
		managerDao.update (copyBronhouderForm (bronhouderForm, bronhouder));
		
		return "redirect:/ba/gebruikersbeheer/bronhouders";
	}

	/**
	 * Displays the form for deleting a bronhouder.
	 * 
	 * @param bronhouderId	The ID of the bronhouder to delete.
	 */
	@RequestMapping (value = "/{bronhouderId}/delete", method = RequestMethod.GET)
	public String deleteBronhouderForm (final @PathVariable("bronhouderId") long bronhouderId, final Model model) {
		final Bronhouder bronhouder = managerDao.getBronhouder (bronhouderId);
		if (bronhouder == null) {
			return "redirect:/ba/gebruikersbeheer/bronhouders";
		}

		// Test whether the bronhouder can be deleted:
		model.addAttribute ("bronhouderDatasets", managerDao.getDatasetsByBronhouder (bronhouder));
		model.addAttribute ("bronhouder", bronhouder);
		
		return "/ba/gebruikersbeheer/delete-bronhouder";
	}

	/**
	 * Processes a POST of the bronhouder delete form.
	 * 
	 * @param bronhouderId
	 */
	@RequestMapping (value = "/{bronhouderId}/delete", method = RequestMethod.POST)
	@Transactional
	public String processDeleteBronhouderForm (final @PathVariable("bronhouderId") long bronhouderId, final Model model) {
		final Bronhouder bronhouder = managerDao.getBronhouder (bronhouderId);
		if (bronhouder == null) {
			return "redirect:/ba/gebruikersbeheer/bronhouders";
		}
		
		final List<Dataset> bronhouderDatasets = managerDao.getDatasetsByBronhouder (bronhouder);
		if (!bronhouderDatasets.isEmpty ()) {
			model.addAttribute ("bronhouderDatasets", bronhouderDatasets);
			model.addAttribute ("bronhouder", bronhouder);
			return "/ba/gebruikersbeheer/delete-bronhouder";
		}
		
		// Delete the bronhouder and related jobs:
		managerDao.delete (bronhouder);
		
		return "redirect:/ba/gebruikersbeheer/bronhouders";
	}
	
	/**
	 * Peforms additional validations on the given form that are not easily tested using validation
	 * annotations. Checks for uniqueness of the code, name and common name properties.
	 * 
	 * Properties that have errors are rejected on the binding result.
	 * 
	 * @param bindingResult	The binding result that contains the current (unvalidated) values of the form.
	 * @param bronhouder	The original bronhouder, or null if there is no original (in case of a create action).
	 */
	private void validateBronhouderForm (final BindingResult bindingResult, final Bronhouder bronhouder) {
		final String code = (String) bindingResult.getFieldValue ("code");
		final String naam = (String) bindingResult.getFieldValue ("naam");
		final String commonName = (String) bindingResult.getFieldValue ("commonName");
		
		// Check the code for uniqueness:
		if (code != null && !code.isEmpty ()) {
			final Bronhouder duplicateCode = managerDao.getBronhouderByCode (code);
			if (duplicateCode != null && !(bronhouder != null && bronhouder.getId ().equals (duplicateCode.getId ()))) {
				bindingResult.rejectValue ("code", "CODE_DUPLICATE", String.format ("De code %s is al in gebruik bij een andere bronhouder", code));
			}
		}
		
		// Check the name for uniqueness:
		if (naam != null && !naam.isEmpty ()) {
			final Bronhouder duplicateNaam = managerDao.getBronhouderByNaam (naam);
			if (duplicateNaam != null && !(bronhouder != null && bronhouder.getId ().equals (duplicateNaam.getId ()))) {
				bindingResult.rejectValue ("naam", "NAAM_DUPLICATE", String.format ("De naam %s is al in gebruik bij een andere bronhouder", naam));
			}
		}
		
		// Check the common name for uniqueness:
		if (commonName != null && !commonName.isEmpty ()) {
			final Bronhouder duplicateCommonName = managerDao.getBronhouderByCommonName (commonName);
			if (duplicateCommonName != null && !(bronhouder != null && bronhouder.getId ().equals (duplicateCommonName.getId ()))) {
				bindingResult.rejectValue ("commonName", "COMMON_NAME_DUPLICATE", String.format ("De identificatie %s is al in gebruik bij een andere bronhouder", commonName));
			}
		}
	}
	
	/**
	 * Copies the properties of the given form to the given bronhouder instance.
	 * 
	 * @param form			The source form.
	 * @param bronhouder	The destination bronhouder.
	 * @return				The destination bronhouder.
	 */
	private Bronhouder copyBronhouderForm (final BronhouderForm form, final Bronhouder bronhouder) {
		bronhouder.setCode (form.getCode ());
		bronhouder.setCommonName (form.getCommonName ());
		bronhouder.setContactAdres (form.getContactAdres ());
		bronhouder.setContactEmailadres (form.getContactEmailadres ());
		bronhouder.setContactNaam (form.getContactNaam ());
		bronhouder.setContactPlaats (form.getContactPlaats ());
		bronhouder.setContactPostcode (form.getContactPostcode ());
		bronhouder.setContactTelefoonnummer (form.getContactTelefoonnummer ());
		bronhouder.setNaam (form.getNaam ());
		
		return bronhouder;
	}
}