package org.onetwo.common.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.Validator;

import org.apache.log4j.Logger;
import org.onetwo.common.db.BaseEntityManager;
import org.onetwo.common.exception.BaseException;
import org.onetwo.common.spring.validator.ValidatorWrapper;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;

/****
 * 可通过这个类手动获取spring容器里注册的bean
 * @author weishao
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SpringApplication {

	protected static Logger logger = Logger.getLogger(SpringApplication.class);

	private static SpringApplication instance = new SpringApplication();

	private ApplicationContext appContext;
	
	private boolean initialized;
	
	private BaseEntityManager baseEntityManager;
	
	private ValidatorWrapper validatorWrapper;
	
	private SpringApplication() {
	}

	public static SpringApplication getInstance() {
		if(!instance.initialized){
			throw new BaseException("application context has not init ...");
		}
		return instance;
	}

	public static void initApplication(ApplicationContext webappContext) {
		instance = new SpringApplication();
		instance.appContext = webappContext;
		instance.initialized = true;
//		instance.printBeanNames();
		try {
			instance.baseEntityManager = instance.getBean(BaseEntityManager.class);
		} catch (Exception e) {
			logger.error("can not find the BaseEntityManager, ignore it: " + e.getMessage());
		}
	}

	public ApplicationContext getAppContext() {
		return appContext;
	}
	
	public Object getBean(String beanName) {
		return getBean(beanName, false);
	}

	public Object getBean(String beanName, boolean throwIfError) {
		Object bean = null;
		try {
			bean = getAppContext().getBean(beanName);
		} catch (Exception e) {
			if(throwIfError)
				throw new BaseException("get bean from spring error! ", e);
//			logger.error("get bean from spring error! ", e);
		}
		return bean;
	}

	public <T> T getBean(Class<T> clazz, String beanName) {
		T bean = (T) getBean(beanName);
		if (bean == null)
			bean = getBean(clazz);
		return bean;
	}


	public <T> T getBeanByDefaultName(Class<T> clazz) {
		return (T)getBean(StringUtils.uncapitalize(clazz.getSimpleName()));
	}


	public <T> T getBean(Class<T> clazz) {
		return getBean(clazz, false);
	}

	public <T> T getBean(Class<T> clazz, boolean throwIfError) {
		T bean = null;
			String beanName = StringUtils.uncapitalize(clazz.getSimpleName());
//			Map map = this.getAppContext().getBeansOfType(clazz);
			Map map = getBeansMap(clazz);
			if (map == null || map.isEmpty())
				return (T) getBean(beanName, throwIfError);
			else
				bean = (T) map.get(beanName);
			if (bean == null)
				bean = (T) map.values().iterator().next();
		
		return bean;
	}

	public <T> Map<String, T> getBeansMap(Class<T> clazz) {
		Map<String, T> map = BeanFactoryUtils.beansOfTypeIncludingAncestors(getAppContext(), clazz);
		return map;
	}
	
	public <T> List<T> getBeans(Class<T> clazz) {
//		Map map = getAppContext().getBeansOfType(clazz);
		Map map = BeanFactoryUtils.beansOfTypeIncludingAncestors(getAppContext(), clazz);
		if(map==null || map.isEmpty())
			return Collections.EMPTY_LIST;
		List<T> list = new ArrayList<T>(map.values());
		OrderComparator.sort(list);
		return list;
	}
	
	public <T> T getSpringHighestOrder(Class<T> clazz){
		return SpringUtils.getHighestOrder(getAppContext(), clazz);
	}
	
	public <T> T getSpringLowestOrder(Class<T> clazz){
		return SpringUtils.getLowestOrder(getAppContext(), clazz);
	}
	
	
	public <T> T getLastedImplementor(Class<T> clazz, boolean throwIfNo){
		T[] a = orderAscImplementor(clazz, throwIfNo);
		return a==null?null:a[a.length-1];
	}
	
	public <T> T getFirstImplementor(Class<T> clazz, boolean throwIfNo){
		T[] a = orderAscImplementor(clazz, throwIfNo);
		return a==null?null:a[0];
	}
	
	public <T> T[] orderAscImplementor(Class<T> clazz, boolean throwIfNo){
		Map<String, T> beans = getBeansMap(clazz);
		if(LangUtils.hasNotElement(beans)){
			if(throwIfNo)
				LangUtils.throwBaseException("can not find any bean for class : " + clazz);
			else
				return null;
		}
		T[] a = (T[])beans.values().toArray();
		if(a.length==1)
			return a;
		LangUtils.asc(a);
		return a;
	}
	
	public void printBeanNames(){
		String[] beanNames = getAppContext().getBeanDefinitionNames();
		System.out.println("=================================== spring beans ===================================");
		int index = 0;
		for (String bn : beanNames) {
			Object obj = SpringApplication.getInstance().getBean(bn);
			System.out.println("["+(++index)+"]" + bn + ":" + (obj != null ? obj.getClass() : "null"));
		}
		System.out.println("=================================== spring beans ===================================");
	}

	public BaseEntityManager getBaseEntityManager() {
		return baseEntityManager;
	}
	

	public ValidatorWrapper getValidator(){
		if(this.validatorWrapper!=null)
			return validatorWrapper;
		
		 Validator validator = getBean(Validator.class);
		 if(validator==null)
			 throw new BaseException("no validator found!");
		 this.validatorWrapper = ValidatorWrapper.wrap(validator);
		 return validatorWrapper;
	}

}
