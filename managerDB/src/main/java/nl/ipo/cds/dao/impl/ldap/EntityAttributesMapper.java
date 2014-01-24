package nl.ipo.cds.dao.impl.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

public abstract class EntityAttributesMapper<T> implements AttributesMapper {

	@Override
	public final Object mapFromAttributes(final Attributes attributes) throws NamingException {
		return fromAttributes (attributes);
	}

	public abstract T fromAttributes (Attributes attributes) throws NamingException;
	public abstract Attributes toAttributes (T object) throws NamingException;
}
