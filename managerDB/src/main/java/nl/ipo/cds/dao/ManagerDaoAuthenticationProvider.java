package nl.ipo.cds.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikerThemaAutorisatie;
import nl.ipo.cds.domain.TypeGebruik;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * {@link AuthenticationProvider} for Spring security that uses the {@link ManagerDao} to
 * provide users and roles.
 * 
 * Adds one or more of the following roles to the resulting authentication token upon success:
 * - ROLE_USER: all users have this role.
 * - ROLE_SUPERUSER: superusers/admins. These users have the superuser flag set to true.
 * - The constants in {@link TypeGebruik}, prefixed with ROLE_: ROLE_RAADPLEGER, ROLE_DATABEHEERDER and ROLE_VASTSTELLER.
 */
public class ManagerDaoAuthenticationProvider implements AuthenticationProvider {

	private final static Logger logger = LoggerFactory.getLogger (ManagerDaoAuthenticationProvider.class);
	
	private final ManagerDao managerDao;

	/**
	 * Creates a {@link ManagerDaoAuthenticationProvider} by providing the DAO instance to use.
	 * 
	 * @param managerDao The {@link ManagerDao} instance to use. Cannot be null.
	 */
	public ManagerDaoAuthenticationProvider (final ManagerDao managerDao) {
		if (managerDao == null) {
			throw new NullPointerException ("managerDao cannot be null");
		}
		
		this.managerDao = managerDao;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Authentication authenticate (final Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
			throw new IllegalArgumentException ("Only UsernamePasswordAuthenticationToken is supported");
		}
		
		final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
		
        final String username = token.getName ();
        final String password = (String) authentication.getCredentials();
		
        if (username == null || username.isEmpty ()) {
        	logger.error ("Failed to authenticate: empty username");
        	throw new BadCredentialsException ("Empty username");
        }
        if (password == null) {
        	logger.error ("Failed to authenticate user " + username + ": null password was provided");
        	throw new BadCredentialsException ("null password was provided");
        }
        
        // Attempt to authenticate:
        if (!managerDao.authenticate (username, password)) {
        	logger.error ("Failed to authenticate user " + username + ": bad credentials");
        	throw new BadCredentialsException ("Bad credentials");
        }

        // Create user details:
        final List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities (username);
        
        logger.info ("User " + username + " successfully authenticated (" + grantedAuthorities.toString () + ")");
        
        return new UsernamePasswordAuthenticationToken (username, password, grantedAuthorities);
	}
	
	private List<GrantedAuthority> getGrantedAuthorities (final String username) {
        final Gebruiker gebruiker = managerDao.getGebruiker (username);
        if (gebruiker == null) {
        	return Collections.<GrantedAuthority>emptyList ();
        }

        final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority> ();
        
        authorities.add (new SimpleGrantedAuthority ("ROLE_USER"));
        
        final Set<TypeGebruik> typeGebruik = new HashSet<TypeGebruik> ();
        
        if (gebruiker.isSuperuser ()) {
        	authorities.add (new SimpleGrantedAuthority ("ROLE_SUPERUSER"));
        	if (!managerDao.getBronhouderThemas ().isEmpty ()) {
        		typeGebruik.addAll (Arrays.asList (TypeGebruik.values ()));
        	}
        } else {
	        for (final GebruikerThemaAutorisatie gta: managerDao.getGebruikerThemaAutorisatie (gebruiker)) {
	        	for (final TypeGebruik permission: gta.getTypeGebruik ().getPermissions ()) {
		        	typeGebruik.add (permission);
	        	}
	        }
        }
        
        for (final TypeGebruik tg: typeGebruik) {
        	authorities.add (new SimpleGrantedAuthority ("ROLE_" + tg.toString().toUpperCase ()));
        }
        
        return Collections.unmodifiableList (authorities);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports (final Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom (authentication);
	}
}
