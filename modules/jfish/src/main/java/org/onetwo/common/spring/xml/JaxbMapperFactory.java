package org.onetwo.common.spring.xml;

import java.util.List;

import org.onetwo.common.fish.exception.JFishException;
import org.onetwo.common.spring.utils.JFishResourcesScanner;
import org.onetwo.common.spring.utils.JaxbClassFilter;
import org.onetwo.common.spring.utils.ResourcesScanner;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.xml.jaxb.JaxbMapper;

final public class JaxbMapperFactory {
	
	private final static ResourcesScanner scaner = new JFishResourcesScanner();
	
	public static JaxbMapper createMapper(String...basePackages){
		List<Class<?>> xmlClasses = scaner.scan(JaxbClassFilter.Instance, basePackages);
		if(LangUtils.isEmpty(xmlClasses))
			throw new JFishException("can not find any xml class.");
		try {
			JaxbMapper mapper = JaxbMapper.createMapper(xmlClasses.toArray(new Class[xmlClasses.size()]));
			return mapper;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new JFishException("create jaxb mapper error: " + e.getMessage(), e);
		}
	}

}
