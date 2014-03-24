package nl.ipo.cds.admin.i18n;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class VelocityAdapter implements ApplicationContextAware {
	
	ApplicationContext applicationContext;

	public Object get(String propertyName) {
		try {
			return applicationContext.getBean("i18n." + propertyName);
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;		
	}
}
