package nl.ipo.cds.dao.impl.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class GebruikerContextMapper implements ContextMapper {

	private final GebruikerAttributesMapper mapper = new GebruikerAttributesMapper ();
	
	@Override
	public Object mapFromContext (final Object ctx) {
		final DirContextAdapter context = (DirContextAdapter) ctx;
		final Attributes attributes = context.getAttributes ();
		
		attributes.put ("dn", context.getDn ().toString ());
		
		try {
			return mapper.fromAttributes (attributes);
		} catch (NamingException e) {
			return null;
		}
	}
}
