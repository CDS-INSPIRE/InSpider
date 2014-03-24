package nl.ipo.cds.admin.i18n;

import java.io.IOException;
import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

public class MessageInterfaceRegistar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		final String basePackage = (String)importingClassMetadata.getAnnotationAttributes(EnableI18N.class.getName()).get("basePackage");
		
		ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false) {
			
			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {					
				try {
					final String beanClassName = beanDefinition.getBeanClassName();
					final Class<?> clazz = Class.forName(beanClassName);
					
					for(Method method : clazz.getDeclaredMethods()) {
						if(!method.getReturnType().equals(String.class)) {
							throw new IllegalArgumentException("i18n message interface with non-String returning method: " + beanClassName + "." + method.getName());
						}
					}
					
					return true;
				} catch(ClassNotFoundException e) {
					return false;
				}
			}
		};
		
		componentProvider.addIncludeFilter(new TypeFilter() {
			@Override
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {								
				return metadataReader.getClassMetadata().isInterface();				
			}
		});
		
		for(final BeanDefinition component : componentProvider.findCandidateComponents(basePackage)) {
			final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			beanDefinition.setFactoryBeanName("i18nFactory");
			beanDefinition.setFactoryMethodName("create");
			
			final ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
			constructorArgumentValues.addGenericArgumentValue(component.getBeanClassName());
			
			beanDefinition.setConstructorArgumentValues(constructorArgumentValues);
			
			final String beanClassName = component.getBeanClassName();
			final int lastIndex = beanClassName.lastIndexOf(".");
			
			final String name;
			if(lastIndex == -1) {
				name = beanClassName;
			} else {
				name = beanClassName.substring(lastIndex + 1);
			}
				
			registry.registerBeanDefinition("i18n." + name.toLowerCase(), beanDefinition);
		}
	}
}
