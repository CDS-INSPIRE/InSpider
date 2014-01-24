package nl.ipo.cds.naming;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class JndiPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private static final Log logger = LogFactory.getLog(JndiPropertyPlaceholderConfigurer.class);


	@Override
	protected void loadProperties(Properties props) throws IOException {
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put(Context.PROVIDER_URL,"jnp://localhost:1099");
		env.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		env.put(Context.URL_PKG_PREFIXES,"org.jboss.naming:org.jnp.interfaces");
		InitialContext context = null;
		Properties jndiProperties = new Properties();
		NamingEnumeration<Binding> bindings;
		try {
			context = new InitialContext(env);
			bindings = context.listBindings("");
			while (bindings.hasMore()) {
			    Binding bd = (Binding)bindings.next();
			    logger.debug("Property read from JNDI: " + bd.getName() + ": " + bd.getObject());
			    jndiProperties.put(bd.getName(), bd.getObject());
			}
		} catch (NamingException ne) {
			throw new RuntimeException(ne);
		}

		props.putAll(jndiProperties);
		
		super.loadProperties(props);
		
	}

}
