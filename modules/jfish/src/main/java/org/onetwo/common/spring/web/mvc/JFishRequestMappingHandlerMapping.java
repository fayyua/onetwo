package org.onetwo.common.spring.web.mvc;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.onetwo.common.fish.plugin.JFishPluginManager;
import org.onetwo.common.fish.plugin.JFishPluginMeta;
import org.onetwo.common.fish.plugin.PluginInfo;
import org.onetwo.common.fish.plugin.anno.PluginControllerConf;
import org.onetwo.common.utils.StringUtils;
import org.springframework.core.OrderComparator;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**********
 * 
 * @author wayshall
 *
 */
public class JFishRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	private JFishPluginManager pluginManager;

	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
		JFishPluginMeta plugin = pluginManager.getJFishPluginMetaOf(handlerType);
		if(info!=null && plugin!=null){
			info = createPluginRequestMappingInfo(plugin.getPluginInfo(), method, handlerType).combine(info);
		}
		return info;
	}

	private RequestMappingInfo createPluginRequestMappingInfo(PluginInfo plugin, Method method, Class<?> handlerType) {
		String rootPath = plugin.getContextPath();
		PluginControllerConf conf = handlerType.getAnnotation(PluginControllerConf.class);
		if(conf!=null){
			if(StringUtils.isBlank(conf.contextPath()) || conf.contextPath().equals("/")){
				rootPath = "";
			}else{
				rootPath = conf.contextPath(); 
			}
		}
		return new RequestMappingInfo(
				new PatternsRequestCondition(rootPath),
				null,
				null,
				null,
				null,
				null, 
				null);
	}

	public void setPluginManager(JFishPluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}
	

	protected void detectMappedInterceptors(List<MappedInterceptor> mappedInterceptors) {
		super.detectMappedInterceptors(mappedInterceptors);
		Collections.sort(mappedInterceptors, new Comparator<MappedInterceptor>() {

			@Override
			public int compare(MappedInterceptor o1, MappedInterceptor o2) {
				return OrderComparator.INSTANCE.compare(o1.getInterceptor(), o2.getInterceptor());
			}
			
		});
	}
	
}
