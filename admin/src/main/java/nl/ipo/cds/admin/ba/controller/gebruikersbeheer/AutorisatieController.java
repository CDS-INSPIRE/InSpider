package nl.ipo.cds.admin.ba.controller.gebruikersbeheer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.Valid;

import nl.ipo.cds.admin.ba.controller.gebruikersbeheer.beans.BronhouderThemas;
import nl.ipo.cds.admin.ba.controller.gebruikersbeheer.beans.GebruikerThemas;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.BronhouderThema;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikerThemaAutorisatie;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TypeGebruik;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for displaying and managing the authorization relationships between 
 * bronhouders and themes and between users and themes.
 */
@Controller
@RequestMapping ("/ba/gebruikersbeheer/autorisatie")
public class AutorisatieController {

	@Inject
	private ManagerDao managerDao;
	
	/**
	 * Displays authorization between bronhouders and themes.
	 */
	@RequestMapping (value = "bronhouders", method = RequestMethod.GET)
	public String showBronhouderThemaAutorisatie (final Model model) {
		model.addAttribute ("bronhouderThemas", getBronhouderThemas ());
		return "/ba/gebruikersbeheer/bronhouder-autorisatie";
	}

	/**
	 * Displays a form to edit the authorization between a single bronhouder and the available themes.
	 * Redirects back to {@link AutorisatieController#showBronhouderThemaAutorisatie(Model)} when the
	 * bronhouder can't be found. 
	 */
	@RequestMapping (value = "bronhouders/{bronhouderId}/edit", method = RequestMethod.GET)
	public String showBronhouderThemaAutorisatieForm (final @PathVariable("bronhouderId") long bronhouderId, final Model model) {
		final Bronhouder bronhouder = managerDao.getBronhouder (bronhouderId);
		if (bronhouder == null) {
			return "redirect:/ba/gebruikersbeheer/autorisatie/bronhouders";
		}
		
		model.addAttribute ("bronhouder", bronhouder);
		model.addAttribute ("themas", managerDao.getAllThemas ());
		model.addAttribute ("selectedThemas", new HashSet<Thema> (managerDao.getAllThemas (bronhouder)));
		
		return "/ba/gebruikersbeheer/bronhouder-autorisatie-edit";
	}

	/**
	 * Processes the authorization form for a bronhouder. Redirects back to {@link AutorisatieController#showBronhouderThemaAutorisatie(Model)}
	 * after completing or when the bronhouder can't be found.
	 */
	@RequestMapping (value = "bronhouders/{bronhouderId}/edit", method = RequestMethod.POST)
	@Transactional
	public String processBronhouderThemaAutorisatieForm (
			final @PathVariable("bronhouderId") long bronhouderId, 
			final Model model,
			final @Valid IdSet idSet,
			final BindingResult bindingResult) {
		
		final Bronhouder bronhouder = managerDao.getBronhouder (bronhouderId);
		
		// Redirect immediately in case of an error:
		if (bronhouder == null || bindingResult.hasErrors ()) {
			return "redirect:/ba/gebruikersbeheer/autorisatie/bronhouders";
		}
		
		final Set<Long> ids = idSet.getIds ().keySet ();
		final Map<Long, BronhouderThema> bronhouderThemas = new HashMap<Long, BronhouderThema> ();
		
		// Create a map of BronhouderThemas for convenient lookups:
		for (final BronhouderThema bronhouderThema: managerDao.getBronhouderThemas (bronhouder)) {
			bronhouderThemas.put (bronhouderThema.getThema ().getId (), bronhouderThema);
		}
		
		// Insert new BronhouderThema instances:
		for (final Long id: ids) {
			if (bronhouderThemas.containsKey (id)) {
				// An entry already exists: remove from the map and skip.
				bronhouderThemas.remove (id);
				continue;
			}
			
			final Thema thema = managerDao.getThema (id);
			if (thema == null) {
				continue;
			}
			
			managerDao.create (new BronhouderThema (thema, bronhouder));
		}

		// Remove bronhouder themas that are no longer relevant:
		for (final BronhouderThema bronhouderThema: bronhouderThemas.values ()) {
			managerDao.delete (bronhouderThema);
		}
		
		return "redirect:/ba/gebruikersbeheer/autorisatie/bronhouders";
	}

	/**
	 * Displays the authorization between users and themes.
	 */
	@RequestMapping (value = "gebruikers", method = RequestMethod.GET)
	public String showGebruikerThemaAutorisatie (final Model model) {
		
		model.addAttribute ("gebruikerThemas", getGebruikerThemas ());
		
		return "/ba/gebruikersbeheer/gebruiker-autorisatie";
	}

	/**
	 * Displays a form to edit the authorization between a single user and all available themes.
	 * Redirects to {@link AutorisatieController#showGebruikerThemaAutorisatie(Model)} if the user
	 * can't be found.
	 */
	@RequestMapping (value = "gebruikers/{username}/edit", method = RequestMethod.GET)
	public String showGebruikerThemaAutorisatieForm (final @PathVariable("username") String username, final Model model) {
		final Gebruiker gebruiker = managerDao.getGebruiker (username);
		if (gebruiker == null) {
			return "redirect:/ba/gebruikersbeheer/autorisatie/gebruikers";
		}
		
		model.addAttribute ("gebruiker", gebruiker);
		model.addAttribute ("bronhouderThemas", managerDao.getBronhouderThemas ());
		
		final Map<BronhouderThema, GebruikerThemaAutorisatie> autorisatie = new HashMap<BronhouderThema, GebruikerThemaAutorisatie> ();
		for (final GebruikerThemaAutorisatie gta: managerDao.getGebruikerThemaAutorisatie (gebruiker)) {
			autorisatie.put (gta.getBronhouderThema (), gta);
		}
		
		model.addAttribute ("autorisatie", autorisatie);
		
		return "/ba/gebruikersbeheer/gebruiker-autorisatie-edit";
	}

	/**
	 * Processes the authorization between a single user and all available themes.
	 * Redirects to {@link AutorisatieController#showGebruikerThemaAutorisatie(Model)} upon completion or if the user
	 * can't be found.
	 */
	@RequestMapping (value = "gebruikers/{username}/edit", method = RequestMethod.POST)
	@Transactional
	public String processGebruikerThemaAutorisatieForm (
			final @PathVariable("username") String username, 
			final Model model,
			final @Valid AutorisatieMap autorisatieMap,
			final BindingResult bindingResult) {
		
		final Gebruiker gebruiker = managerDao.getGebruiker (username);
		
		// Redirect back to the list in case of error:
		if (gebruiker == null || bindingResult.hasErrors ()) {
			return "redirect:/ba/gebruikersbeheer/autorisatie/gebruikers";
		}

		// Delete all current authorization for the user:
		final List<GebruikerThemaAutorisatie> currentGtas = new ArrayList<GebruikerThemaAutorisatie> (managerDao.getGebruikerThemaAutorisatie (gebruiker));
		for (final GebruikerThemaAutorisatie gta: currentGtas) {
			managerDao.delete (gta);
		}
		
		// Insert new authorization for the user:
		for (final Map.Entry<String, String> entry: autorisatieMap.getAutorisatie ().entrySet ()) {
			if (entry.getValue () == null || entry.getValue ().isEmpty ()) {
				continue;
			}
			
			// Decode the typeGebruik value:
			final TypeGebruik typeGebruik;
			try {
				typeGebruik = TypeGebruik.valueOf (entry.getValue ());
			} catch (IllegalArgumentException e) {
				continue;
			}
			
			// Decode the ids and locate thema and bronhouder:
			final String[] parts = entry.getKey ().split ("\\-");
			if (parts.length != 2) {
				continue;
			}
			
			final Bronhouder bronhouder;
			final Thema thema;

			try {
				bronhouder = managerDao.getBronhouder (Long.parseLong (parts[0]));
				thema = managerDao.getThema (Long.parseLong (parts[1]));
			} catch (NumberFormatException e) {
				continue;
			}
			
			if (bronhouder == null || thema == null) {
				continue;
			}
			
			// Locate the appropriate BronhouderThema instance:
			final BronhouderThema bronhouderThema = managerDao.getBronhouderThema (bronhouder, thema);
			if (bronhouderThema == null) {
				continue;
			}

			// Create the GebruikerThemaAutorisatie relation:
			managerDao.createGebruikerThemaAutorisatie (gebruiker, bronhouderThema, typeGebruik);
		}
		
		return "redirect:/ba/gebruikersbeheer/autorisatie/gebruikers";
	}
	
	/**
	 * Combines the list of all users with all {@link GebruikerThemaAutorisatie} associations.
	 * 
	 * @return A list of {@link GebruikerThemas} instances each containing a user and associated
	 * 			{@link GebruikerThemaAutorisatie}'s.
	 */
	private List<GebruikerThemas> getGebruikerThemas () {
		final List<Gebruiker> gebruikers = new ArrayList<Gebruiker> (managerDao.getAllGebruikers ());
		final List<GebruikerThemaAutorisatie> gtas = new ArrayList<GebruikerThemaAutorisatie> (managerDao.getGebruikerThemaAutorisatie ());

		// Sort both lists on gebruikersnaam:
		Collections.sort (gebruikers, new Comparator<Gebruiker> () {
			@Override
			public int compare (final Gebruiker o1, final Gebruiker o2) {
				return o1.getGebruikersnaam ().compareTo (o2.getGebruikersnaam ());
			}
		});
		Collections.sort (gtas, new Comparator<GebruikerThemaAutorisatie> () {
			@Override
			public int compare (final GebruikerThemaAutorisatie o1, final GebruikerThemaAutorisatie o2) {
				return o1.getGebruiker ().getGebruikersnaam ().compareTo (o2.getGebruiker ().getGebruikersnaam ());
			}
		});

		final Set<String> usernames = new HashSet<String> ();
		for (final Gebruiker gebruiker: gebruikers) {
			usernames.add (gebruiker.getGebruikersnaam ());
		}
		
		// Merge the two lists:
		int i = 0;
		final List<GebruikerThemas> result = new ArrayList<GebruikerThemas> ();
		for (final Gebruiker gebruiker: gebruikers) {
			final List<GebruikerThemaAutorisatie> gebruikerGtas = new ArrayList<GebruikerThemaAutorisatie> ();

			while (i < gtas.size () && !usernames.contains (gtas.get (i).getGebruiker ().getGebruikersnaam ())) {
				++ i;
			}
			
			while (i < gtas.size () && gebruiker.getGebruikersnaam ().equals (gtas.get (i).getGebruiker ().getGebruikersnaam ())) {
				gebruikerGtas.add (gtas.get (i));
				++ i;
			}
			
			result.add (new GebruikerThemas (gebruiker, gebruikerGtas));
		}
		
		return Collections.unmodifiableList (result);
	}
	
	/**
	 * Combines the list of all bronhouders with all {@link BronhouderThema} assosications.
	 * 
	 * @return 	A list of {@link BronhouderThemas} instances each containing a bronhouder and
	 * 			associated {@link Thema}'s.
	 */
	private List<BronhouderThemas> getBronhouderThemas () {
		final List<BronhouderThema> bronhouderThemas = managerDao.getBronhouderThemas ();
		final List<Bronhouder> bronhouders = new ArrayList<Bronhouder> (managerDao.getAllBronhouders ());
		final List<BronhouderThemas> result = new ArrayList<BronhouderThemas> ();
		
		// Make sure the bronhouder list is ordered by name:
		Collections.sort (bronhouders, new Comparator<Bronhouder> () {
			@Override
			public int compare (Bronhouder o1, Bronhouder o2) {
				return o1.getNaam ().compareTo (o2.getNaam ());
			}
		});
		
		int i = 0;
		
		for (final Bronhouder bronhouder: bronhouders) {
			final List<Thema> themas = new ArrayList<Thema> ();
			
			while (i < bronhouderThemas.size () && bronhouderThemas.get (i).getBronhouder ().getId ().equals (bronhouder.getId ())) {
				themas.add (bronhouderThemas.get (i).getThema ());
				++ i;
			}
			
			result.add (new BronhouderThemas (bronhouder, themas));
		}
		
		return Collections.unmodifiableList (result);
	}
	
	private static class IdSet {
		@Valid
		private Map<Long, Boolean> ids = new HashMap<Long, Boolean> ();

		public Map<Long, Boolean> getIds () {
			return ids;
		}

		@SuppressWarnings("unused")
		public void setIds (final Map<Long, Boolean> ids) {
			this.ids = ids;
		}
	}
	
	private static class AutorisatieMap {
		@Valid
		private Map<String, String> autorisatie = new HashMap<String, String> ();

		public Map<String, String> getAutorisatie () {
			return autorisatie;
		}

		@SuppressWarnings("unused")
		public void setAutorisatie (final Map<String, String> autorisatie) {
			this.autorisatie = autorisatie;
		}
	}
}