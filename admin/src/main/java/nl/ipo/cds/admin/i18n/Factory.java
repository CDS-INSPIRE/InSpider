package nl.ipo.cds.admin.i18n;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

public class Factory {
	
	final Method HASH_CODE, EQUALS, TO_STRING;
	
	final MessageSource messageSource;
	final LocaleProvider localeProvider;
	
	public Factory(MessageSource messageSource, LocaleProvider localeProvider) throws NoSuchMethodException, SecurityException {
		HASH_CODE = Object.class.getMethod("hashCode");
		EQUALS = Object.class.getMethod("equals", Object.class);
		TO_STRING = Object.class.getMethod("toString");
		
		this.messageSource = messageSource;
		this.localeProvider = localeProvider;
	}

	public Object create(final Class<?> clazz) {
		return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{clazz}, new InvocationHandler() {
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if(method.equals(HASH_CODE)) {
					return System.identityHashCode(proxy);
				}
				
				if(method.equals(EQUALS)) {
					return proxy == args[0];
				}
				
				if(method.equals(TO_STRING)) {
					return "i18n: " + clazz.getCanonicalName();
				}
					
				String code = method.getDeclaringClass().getSimpleName().toLowerCase() + "." + method.getName();
				return messageSource.getMessage(code, args, code, localeProvider.getLocale());
			}
		});
	}
}
