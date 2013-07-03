package org.onetwo.common.spring.web.mvc.config;

import java.util.List;

import org.onetwo.common.spring.ftl.JFishFreeMarkerConfigurer;
import org.onetwo.common.utils.list.JFishList;
import org.onetwo.common.utils.list.NoIndexIt;
import org.springframework.beans.PropertyEditorRegistrar;


public class JFishMvcConfigurerListenerManager {

	private JFishList<JFishMvcConfigurerListener> listeners = new JFishList<JFishMvcConfigurerListener>();

	public void notifyAfterMvcConfig(final JFishMvcApplicationContext applicationContext, final JFishMvcConfig mvcConfig, final List<PropertyEditorRegistrar> peRegisttrarList){
		this.listeners.each(new NoIndexIt<JFishMvcConfigurerListener>() {

			@Override
			protected void doIt(JFishMvcConfigurerListener element) {
				element.onMvcPropertyEditorRegistrars(peRegisttrarList);
				element.onMvcInitContext(applicationContext, mvcConfig);
			}
			
		});
	}
	
	public void notifyListenersOnFreeMarkerConfigurer(final JFishFreeMarkerConfigurer configurer, final boolean hasBuilt){
		this.listeners.each(new NoIndexIt<JFishMvcConfigurerListener>() {

			@Override
			protected void doIt(JFishMvcConfigurerListener element) {
				element.onMvcBuildFreeMarkerConfigurer(configurer, hasBuilt);
			}
			
		});
		
	}
	
	public void addListener(JFishMvcConfigurerListener l){
		listeners.add(l);
	}

}
