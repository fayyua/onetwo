package org.onetwo.common.spring.web.mvc.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.onetwo.common.excel.DefaultXmlTemplateExcelFacotory;
import org.onetwo.common.fish.plugin.JFishPluginManager;
import org.onetwo.common.fish.plugin.JFishPluginManagerFactory;
import org.onetwo.common.fish.spring.config.JFishAppConfigrator;
import org.onetwo.common.interfaces.XmlTemplateGeneratorFactory;
import org.onetwo.common.log.MyLoggerFactory;
import org.onetwo.common.spring.SpringApplication;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.ftl.JFishFreeMarkerConfigurer;
import org.onetwo.common.spring.ftl.JFishFreeMarkerView;
import org.onetwo.common.spring.propeditor.JFishDateEditor;
import org.onetwo.common.spring.web.authentic.SpringAuthenticationInvocation;
import org.onetwo.common.spring.web.authentic.SpringSecurityInterceptor;
import org.onetwo.common.spring.web.mvc.JFishFirstInterceptor;
import org.onetwo.common.spring.web.mvc.JFishJaxb2Marshaller;
import org.onetwo.common.spring.web.mvc.JFishWebArgumentResolver;
import org.onetwo.common.spring.web.mvc.JsonView;
import org.onetwo.common.spring.web.mvc.ModelAndViewPostProcessInterceptor;
import org.onetwo.common.spring.web.mvc.WebAttributeArgumentResolver;
import org.onetwo.common.spring.web.mvc.WebExceptionResolver;
import org.onetwo.common.spring.web.mvc.WebInterceptorAdapter;
import org.onetwo.common.spring.web.mvc.WebInterceptorAdapter.InterceptorOrder;
import org.onetwo.common.spring.web.mvc.annotation.JFishMvc;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.list.JFishList;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.springframework.web.servlet.view.xml.MarshallingView;

/*******
 * 扩展mvc配置
 * @author wayshall
 *
 */
@Configuration
@JFishMvc
@ComponentScan(basePackageClasses = { JFishMvcConfig.class, SpringAuthenticationInvocation.class })
@ImportResource("classpath:mvc/spring-mvc.xml")
public class JFishMvcConfig extends WebMvcConfigurerAdapter implements InitializingBean, ApplicationContextAware {

	protected final Logger logger = MyLoggerFactory.getLogger(this.getClass());
	
	@Resource
	private RequestMappingHandlerAdapter requestMappingHandlerAdapter;
	
	@Resource
	private JFishMvcApplicationContext applicationContext;
	
//	private JFishList<JFishMvcConfigurerListener> listeners = new JFishList<JFishMvcConfigurerListener>();
	
	private JFishMvcConfigurerListenerManager listenerManager = new JFishMvcConfigurerListenerManager();
	
	@Autowired
	protected JFishPluginManager pluginManager;

	@Autowired
	private JFishAppConfigrator jfishAppConfigurator;
	
	public JFishMvcConfig() {
//		jfishAppConfigurator = BaseSiteConfig.getInstance().getWebAppConfigurator(JFishAppConfigurator.class);
	}

	@Configuration
	static class MvcInterceptors {
		@Resource
		private JFishMvcApplicationContext applicationContext;
		
		JFishPluginManager pluginManager = JFishPluginManagerFactory.getPluginManager();

//		@Bean
//		public SecurityInterceptorFacotryBean securityInterceptorFacotryBean() {
//			SecurityInterceptorFacotryBean fb = new SecurityInterceptorFacotryBean();
//			return fb;
//		}
		@Bean
		public SpringSecurityInterceptor springSecurityInterceptor(){
			SpringSecurityInterceptor springSecurityInterceptor = SpringUtils.getHighestOrder(applicationContext, SpringSecurityInterceptor.class);
			if(springSecurityInterceptor==null){
				springSecurityInterceptor = new SpringSecurityInterceptor();
				springSecurityInterceptor.setOrder(InterceptorOrder.SECURITY);
			}
			return springSecurityInterceptor;
		}

		@Bean
		public MappedInterceptor mappedInterceptor4Security() {
//			SpringSecurityInterceptor springSecurityInterceptor = SpringUtils.getHighestOrder(applicationContext, SpringSecurityInterceptor.class);
//			if(springSecurityInterceptor==null){
//				springSecurityInterceptor = new SpringSecurityInterceptor();
//			}
//			SpringSecurityInterceptor springSecurityInterceptor = new SpringSecurityInterceptor();
			return new MappedInterceptor(null, springSecurityInterceptor());
		}

		/************
		 * spring mvc will scan {@code MappedInterceptor}
		 * @return
		 */
		@Bean
		public MappedInterceptor mappedInterceptor4ModelAndViewPostProcess() {
			ModelAndViewPostProcessInterceptor post = SpringUtils.getHighestOrder(applicationContext, ModelAndViewPostProcessInterceptor.class);
			if(post!=null){
				post.setPluginManager(pluginManager);
				return WebInterceptorAdapter.createMappedInterceptor(post);
			}
			return WebInterceptorAdapter.createMappedInterceptor(new ModelAndViewPostProcessInterceptor(pluginManager));
		}

		@Bean
		public MappedInterceptor mappedInterceptor4First() {
			return WebInterceptorAdapter.createMappedInterceptor(new JFishFirstInterceptor());
		}

		/*@Bean
		public MappedInterceptor mappedInterceptor4Security() {
			try {
				return (MappedInterceptor) securityInterceptorFacotryBean().getObject();
			} catch (Exception e) {
				throw new BaseException("create security interceptor error : " + e.getMessage(), e);
			}
		}*/
	}

	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		listenerManager.addListener((JFishMvcConfigurerListener)pluginManager);
	}

	@Bean
	public JFishFreeMarkerConfigurer freeMarkerConfigurer() {
		final JFishFreeMarkerConfigurer freeMarker = new JFishFreeMarkerConfigurer(listenerManager);
		final List<String> templatePaths = new ArrayList<String>(3);
		templatePaths.add("/WEB-INF/ftl/");
		final Properties setting = freemarkerSetting();
		freeMarker.setTemplateLoaderPaths(templatePaths.toArray(new String[templatePaths.size()]));
		freeMarker.setDefaultEncoding("UTF-8");
		freeMarker.setFreemarkerSettings(setting);
		
		return freeMarker;
	}

	@Bean(name = "freemarkerSetting")
	public Properties freemarkerSetting() {
		Properties prop = SpringUtils.createProperties("/mvc/freemarker.properties", true);
		return prop;
	}

	@Bean(name = "mediaType")
	public Map<String, String> mediaType() {
		Properties prop = SpringUtils.createProperties("/mvc/media-type.properties", true);
		return LangUtils.toMap(prop);
	}

	@Bean(name = "mvcSetting")
	public Properties mvcSetting() {
		Properties prop = SpringUtils.createProperties("/mvc/mvc.properties", true);
		return prop;
	}

	@Bean
	public FreeMarkerViewResolver freeMarkerViewResolver() {
		FreeMarkerViewResolver fmResolver = new FreeMarkerViewResolver();
		fmResolver.setViewClass(JFishFreeMarkerView.class);
		fmResolver.setSuffix(".ftl");
		fmResolver.setContentType("text/html;charset=UTF-8");
		fmResolver.setExposeRequestAttributes(true);
		fmResolver.setExposeSessionAttributes(true);
		fmResolver.setExposeSpringMacroHelpers(true);
		fmResolver.setRequestContextAttribute("request");
		fmResolver.setOrder(1);
		return fmResolver;
	}

	@Bean
	public View jsonView() {
		JsonView jview = SpringUtils.getHighestOrder(applicationContext, JsonView.class);
		if(jview==null){
			jview = new JsonView();
		}
		return jview;
	}

	@Bean
	public View xmlView() {
		MarshallingView view = new MarshallingView();
		view.setMarshaller(jaxb2Marshaller());
		return view;
	}

	/*@Bean
	public View jfishExcelView() {
		JFishDownloadView view = new JFishDownloadView();
		return view;
	}*/

	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		JFishJaxb2Marshaller marshaller = new JFishJaxb2Marshaller();
		
		if(jfishAppConfigurator!=null && !LangUtils.isEmpty(jfishAppConfigurator.getXmlBasePackages())){
			marshaller.setClassesToBeBoundByBasePackages(jfishAppConfigurator.getXmlBasePackages());
		}else{
			String xmlBasePackage = mvcSetting().getProperty("xml.base.packages");
			Assert.hasText(xmlBasePackage, "xmlBasePackage in mvc.properties must has text");
			marshaller.setXmlBasePackage(xmlBasePackage);
		}
		return marshaller;
	}

	@Bean
	public ViewResolver contentNegotiatingViewResolver() {
		ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
		viewResolver.setUseNotAcceptableStatusCode(true);
		viewResolver.setOrder(0);
		viewResolver.setMediaTypes(mediaType());
		List<View> views = LangUtils.asListWithType(View.class, xmlView(), jsonView());
		viewResolver.setDefaultViews(views);
		viewResolver.setDefaultContentType(MediaType.TEXT_HTML);
		viewResolver.setIgnoreAcceptHeader(true);
		return viewResolver;
	}

	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new JFishWebArgumentResolver());
		argumentResolvers.add(new WebAttributeArgumentResolver());
		/*List<HttpMessageConverter<?>> converters = LangUtils.newArrayList();
		converters.add(new MappingJacksonHttpMessageConverter());
//		converters.add(new JsonStringHttpMessageConverter());
		ModelAndJsonCompatibleResolver modelAndJson = new ModelAndJsonCompatibleResolver(converters);
		argumentResolvers.add(modelAndJson);*/
	}
	

	/*@Bean
	public HandlerMethodArgumentResolver webArgumentResolver() {
		JFishWebArgumentResolver webArgs = new JFishWebArgumentResolver();
		return webArgs;
	}

	@Bean
	public HandlerMethodArgumentResolver webAttributeArgumentResolver() {
		WebAttributeArgumentResolver webattr = new WebAttributeArgumentResolver();
		return webattr;
	}*/

	@Bean
	public HandlerExceptionResolver webExceptionResolver() {
		WebExceptionResolver webexception = SpringUtils.getHighestOrder(this.applicationContext, WebExceptionResolver.class);
		if(webexception==null){
			webexception = new WebExceptionResolver();
		}
		return webexception;
	}

	public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
		exceptionResolvers.add(webExceptionResolver());
	}
	
	@Bean(name="multipartResolver")
	public MultipartResolver multipartResolver(){
		CommonsMultipartResolver multipart = new CommonsMultipartResolver();
		multipart.setMaxUploadSize(1024*1024*10);//10m
		return multipart;
	}

	public void afterPropertiesSet() throws Exception{
//		this.applicationContext.registerMvcResourcesOfPlugins();

//		this.jfishAppConfigurator = SpringUtils.getBean(applicationContext, JFishAppConfigrator.class);
//		Assert.notNull(jfishAppConfigurator, "there is not app configurator");
		
		final List<PropertyEditorRegistrar> peRegisttrarList = new JFishList<PropertyEditorRegistrar>();
		peRegisttrarList.add(new PropertyEditorRegistrar() {
			@Override
			public void registerCustomEditors(PropertyEditorRegistry registry) {
				registry.registerCustomEditor(Date.class, new JFishDateEditor());
			}
		});
		
		this.listenerManager.notifyAfterMvcConfig(applicationContext, this, peRegisttrarList);
		
		/*this.listeners.each(new NoIndexIt<JFishMvcConfigurerListener>() {

			@Override
			protected void doIt(JFishMvcConfigurerListener element) {
				element.onMvcPropertyEditorRegistrars(peRegisttrarList);
				element.onMvcInitContext(applicationContext, JFishMvcConfig.this);
			}
			
		});*/
		
		((ConfigurableWebBindingInitializer)requestMappingHandlerAdapter.getWebBindingInitializer()).setPropertyEditorRegistrars(peRegisttrarList.toArray(new PropertyEditorRegistrar[peRegisttrarList.size()]));

		SpringApplication.initApplication(applicationContext);
		
	}
	
	public Validator getValidator() {
//		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
//		validator.setValidationMessageSource(validateMessageSource());
		Validator validator = SpringApplication.getInstance().getBean(Validator.class);
		Assert.notNull(validator, "validator can not be null");
		return validator;
	}
	
	@Bean
	public XmlTemplateGeneratorFactory xmlTemplateGeneratorFactory(){
		String className = "org.onetwo.common.excel.POIExcelGeneratorImpl";
		XmlTemplateGeneratorFactory factory = null;
		if(ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader())){
			factory = new DefaultXmlTemplateExcelFacotory();
//			factory.setCacheTemplate(true);
		}else{
			logger.warn("there is not bean implements [" + className + "]");
		}
		return factory;
	}

}
