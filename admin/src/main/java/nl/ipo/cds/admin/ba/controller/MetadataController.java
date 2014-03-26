package nl.ipo.cds.admin.ba.controller;

import java.io.IOException;
import java.util.List;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.MetadataDocument;
import nl.ipo.cds.domain.MetadataDocumentType;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.metadata.MetadataManager;
import nl.ipo.cds.metadata.ValidationResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MetadataController {
	
	private static final Log logger = LogFactory.getLog(MetadataController.class);
	
	@Autowired
	private MetadataManager metadataManager;
	
	@Autowired
	private ManagerDao managerDao;
	
	@Autowired
	private MessageSource messageSource;
	
	@RequestMapping("/ba/metadata/")
	public String metadata() {
		return "redirect:/ba/metadata";
	}

	@RequestMapping("/ba/metadata")
	public String metadata(final Model model) {
		List<MetadataDocument> documents = managerDao.getAllMetadataDocuments();
		model.addAttribute("documents", documents);
		
		logger.debug("Fetched " + documents.size() + " metadata document(s)");
		
		return "/ba/metadata/index";
	}
	
	private void validateDocument(final String documentContent, MetadataDocumentType documentType, final BindingResult bindingResult) throws Exception {
		byte[] documentBytes = documentContent.getBytes("utf-8");
		
		ValidationResult validationResult = metadataManager.validateDocument(documentBytes, documentType);
		if(validationResult != ValidationResult.VALID) {
			String invalidReason = messageSource.getMessage(validationResult.getCode(), null, validationResult.toString(), LocaleContextHolder.getLocale());			
			bindingResult.rejectValue("documentContent", "metadata.notValid", new Object[]{invalidReason}, "not valid: {0}");
		}
	}
	
	@RequestMapping(value="/ba/metadata/edit", method=RequestMethod.POST)
	public String edit(
			@ModelAttribute("metadataForm") @Validated(MetadataForm.Modify.class) final MetadataForm metadataForm,
			final BindingResult bindingResult,
			final Model model) throws Exception {
	
		logger.debug("Saving editted document " + metadataForm);
		
		validateDocument(metadataForm.getDocumentContent(), metadataForm.getDocumentType(), bindingResult);
		
		MetadataDocument metadataDocument = managerDao.getMetadataDocument(metadataForm.getId());
		
		if(bindingResult.hasErrors()) {
			logger.debug("hasErrors");
			
			model.addAttribute("editing", true);
			model.addAttribute("themes", managerDao.getAllThemas());			
			metadataForm.setDocumentName(metadataDocument.getDocumentName());
			return "/ba/metadata/edit";
		}
		
		Thema thema = managerDao.getThema(metadataForm.getThemeId());
		
		metadataDocument.setThema(thema);
		metadataDocument.setDocumentType(metadataForm.getDocumentType());
		managerDao.update(metadataDocument);
		
		byte[] documentBytes = null;
		String documentContent = metadataForm.getDocumentContent();
		if(documentContent != null) {
			documentBytes = documentContent.getBytes("utf-8");
		}
		
		metadataManager.storeDocument(metadataDocument.getDocumentName(), documentBytes);
		
		return "redirect:/ba/metadata";
	}
	
	@RequestMapping("/ba/metadata/edit/{documentId}")
	public String edit(@PathVariable("documentId") final long documentId, final Model model) throws IOException {
		MetadataDocument metadataDocument = managerDao.getMetadataDocument(documentId);
		String documentName = metadataDocument.getDocumentName();
		String documentContent = new String(metadataManager.retrieveDocument(documentName), "utf-8");
		
		logger.debug("Retrieved document '" + documentName + "', length: " + documentContent.length());
		
		MetadataForm metadataForm = new MetadataForm();
		metadataForm.setId(metadataDocument.getId());
		metadataForm.setThemeId(metadataDocument.getThema().getId());
		metadataForm.setDocumentType(metadataDocument.getDocumentType());
		metadataForm.setDocumentName(metadataDocument.getDocumentName());
		metadataForm.setDocumentContent(documentContent);
		
		model.addAttribute("themes", managerDao.getAllThemas());
		model.addAttribute("editing", true);
		model.addAttribute("metadataForm", metadataForm);
		
		return "/ba/metadata/edit";
	}
	
	@Transactional
	@RequestMapping("/ba/metadata/delete/{documentId}")
	public String delete(@PathVariable("documentId") final long documentId) {
		MetadataDocument metadataDocument = managerDao.getMetadataDocument(documentId);
		metadataManager.deleteDocument(metadataDocument.getDocumentName());
		managerDao.delete(metadataDocument);
		
		return "redirect:/ba/metadata";
	}
	
	@RequestMapping(value="/ba/metadata/add", method=RequestMethod.GET)
	public String add(final Model model) {
		model.addAttribute("themes", managerDao.getAllThemas());
		model.addAttribute("metadataForm", new MetadataForm());
		
		return "/ba/metadata/add";
	} 
	
	@RequestMapping(value="/ba/metadata/add", method=RequestMethod.POST)
	public String add(
			@ModelAttribute("metadataForm") @Validated(MetadataForm.Add.class) final MetadataForm metadataForm,
			final BindingResult bindingResult,
			final Model model) throws Exception {
		
		logger.debug("Saving new document " + metadataForm);
		
		validateDocument(metadataForm.getDocumentContent(), metadataForm.getDocumentType(), bindingResult);		
		
		if(!bindingResult.hasFieldErrors("documentName") && metadataManager.documentExists(metadataForm.getDocumentName())) {
			bindingResult.rejectValue("documentName", "metadata.exists", "document exists");
		}
		
		if(bindingResult.hasErrors()) {
			logger.debug("hasErrors");
			
			model.addAttribute("themes", managerDao.getAllThemas());
			return "/ba/metadata/add";
		}
		
		Thema thema = managerDao.getThema(metadataForm.getThemeId());
		
		MetadataDocument metadataDocument = new MetadataDocument();
		metadataDocument.setDocumentName(metadataForm.getDocumentName());
		metadataDocument.setThema(thema);
		metadataDocument.setDocumentType(metadataForm.getDocumentType());
		managerDao.update(metadataDocument);
		
		byte[] documentBytes = null;
		String documentContent = metadataForm.getDocumentContent();
		if(documentContent != null) {
			documentBytes = documentContent.getBytes("utf-8");
		}
		
		metadataManager.storeDocument(metadataDocument.getDocumentName(), documentBytes);
		
		return "redirect:/ba/metadata";
	}
}
