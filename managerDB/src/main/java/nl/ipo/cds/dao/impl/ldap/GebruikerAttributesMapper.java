package nl.ipo.cds.dao.impl.ldap;

import java.nio.charset.Charset;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import nl.ipo.cds.domain.LdapGebruiker;

import org.apache.commons.lang.StringUtils;

public class GebruikerAttributesMapper extends EntityAttributesMapper<LdapGebruiker> {

	@Override
	public LdapGebruiker fromAttributes(Attributes attributes) throws NamingException {
		final LdapGebruiker gebruiker = new LdapGebruiker ();
		
		gebruiker.setGebruikersnaam (getString (attributes, "uid"));
		gebruiker.setEmail (getString (attributes, "mail"));
		gebruiker.setMobile (getString (attributes, "mobile", null));
		gebruiker.setWachtwoordHash (getString (attributes, "userPassword"));
		
		return gebruiker;
	}
	
	@Override
	public Attributes toAttributes (final LdapGebruiker gebruiker) throws NamingException {
		final Attributes attributes = new BasicAttributes ();
		final BasicAttribute objectClass = new BasicAttribute ("objectclass");
		
		objectClass.add ("inetOrgPerson");
		objectClass.add ("organizationalPerson");
		objectClass.add ("person");
		objectClass.add ("top");
		
		attributes.put (objectClass);
		attributes.put ("cn", gebruiker.getGebruikersnaam ());
		attributes.put ("sn", gebruiker.getGebruikersnaam ());
		attributes.put ("uid", gebruiker.getGebruikersnaam ());
		attributes.put ("mail", gebruiker.getEmail ());
		// Mobile is optional
		if (StringUtils.isNotBlank(gebruiker.getMobile())) {
			attributes.put ("mobile", gebruiker.getMobile ());
		}
		attributes.put ("userPassword", String.format ("{SHA}%s", gebruiker.getWachtwoordHash ()));
		
		return attributes;
	}

	private static String getString (Attributes attributes, String name, String defaultValue) throws NamingException {
		final Attribute attribute = attributes.get (name);
		
		if (attribute == null) {
			return defaultValue;
		}

		Object object = attribute.get();
		if (object instanceof byte[])
		{
			String password = new String((byte[])object, Charset.forName("UTF-8"));
			password = StringUtils.replace(password, "{SHA}", "");
			return password;
		} else {
			return object.toString ();
		}
		
	}
	
	private static String getString (Attributes attributes, String name) throws NamingException {
		final String value = getString (attributes, name, null);
		
		if (value == null) {
			throw new NamingException ("No attribute `" + name + "`");
		}
		
		return value;
	}
}
