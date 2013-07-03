package org.onetwo.common.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.onetwo.common.log.MyLoggerFactory;
import org.onetwo.common.spring.SpringApplication;
import org.onetwo.common.utils.MyUtils;
import org.onetwo.common.web.utils.RequestUtils;
import org.slf4j.Logger;
import org.springframework.core.OrderComparator;


@SuppressWarnings("unchecked")
public abstract class IgnoreFiler implements Filter{

	
	public static final String EXCLUDE_SUFFIXS_NAME = "excludeSuffixs";

	public static final String INCLUDE_SUFFIXS_NAME = "includeSuffixs";

	private static final String[] DEFAULT_EXCLUDE_SUFFIXS = { ".js", ".css", ".jpg", ".jpeg", ".gif", ".png",".htm" };

	private static final String[] DEFAULT_INCLUDE_SUFFIXS = { ".html", ".jsp", ".do", ".action", ".json", ".xml" };

	protected Collection<String> excludeSuffixs = new ArrayList<String>();

	protected Collection<String> includeSuffixs = new ArrayList<String>();
	
	/*****
	 * 是否过滤后缀
	 */
	protected boolean filterSuffix = false;
	
	protected List<FilterInitializer> filterInitializers;

	protected final Logger logger = MyLoggerFactory.getLogger(this.getClass());
	
	final public void init(FilterConfig config) throws ServletException {
//		logger = this.initLogger(config);
		logger.info("项目正在启动...");
		this.initApplication(config);

		filterSuffix = "true".equals(config.getInitParameter("filterSuffix"));
		if(!filterSuffix)
			return ;
		
		String[] excludeSuffixsStrs = StringUtils.split(config.getInitParameter("excludeSuffixs"), ',');
		if (excludeSuffixsStrs != null && excludeSuffixsStrs.length>0) {
			for (String path : excludeSuffixsStrs){
				path = path.trim();
				if(path.indexOf('.')==-1)
					path = "." + path;
				excludeSuffixs.add(path);
			}
		}else{
			excludeSuffixs = MyUtils.asList(DEFAULT_EXCLUDE_SUFFIXS);
		}

		String[] includeSuffixsStr = StringUtils.split(config.getInitParameter("includeSuffixs"), ',');
		if (includeSuffixsStr != null && includeSuffixsStr.length>0) {
			for (String path : includeSuffixsStr){
				path = path.trim();
				if(path.indexOf('.')==-1)
					path = "." + path;
				includeSuffixs.add(path);
			}
		}else{
			includeSuffixs = MyUtils.asList(DEFAULT_INCLUDE_SUFFIXS);
		}

		onInit(config);
	}
	
	/*too late
	 * protected Logger initLogger(FilterConfig config) throws ServletException {
		Logger logger = MyLoggerFactory.getLogger(this.getClass());
		return logger;
	}*/
	
	protected void initApplication(FilterConfig config) {
		/*ServletContext context = config.getServletContext();
		
		WebApplicationContext app = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
		SpringApplication.getInstance().initApplication(app);*/
	}
	

	public String[] getFilterInitializers(FilterConfig config){
		return null;
	}
	
	protected void onInit(FilterConfig config){
		String[] strs = getFilterInitializers(config);
		if(strs==null || strs.length==0)
			return ;
		if("auto".equals(strs[0].trim())){
			filterInitializers = (List<FilterInitializer>) SpringApplication.getInstance().getBeans(FilterInitializer.class);
		}else{
			filterInitializers = new ArrayList<FilterInitializer>(strs.length);
			FilterInitializer initer = null;
			for(String bn : strs){
				initer = (FilterInitializer)SpringApplication.getInstance().getBean(bn);
				filterInitializers.add(initer);
			}
		}
		
		if(filterInitializers==null || filterInitializers.isEmpty())
			return ;

		OrderComparator.sort(filterInitializers);
		/*
		Collections.sort(filterInitializers, new Comparator<FilterInitializer>(){
			@Override
			public int compare(FilterInitializer o1, FilterInitializer o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});*/
		
		for(FilterInitializer filterIniter : filterInitializers){
			filterIniter.onInit(config);
		}
	}
	

	protected void onFilter(HttpServletRequest request, HttpServletResponse response){
		if(filterInitializers==null || filterInitializers.isEmpty())
			return ;
		for(FilterInitializer filterIniter : filterInitializers){
			filterIniter.onFilter(request, response);
		}
	}
	

	protected void onFinally(HttpServletRequest request, HttpServletResponse response){
		if(filterInitializers==null || filterInitializers.isEmpty())
			return ;
		for(FilterInitializer filterIniter : filterInitializers){
			filterIniter.onFinally(request, response);
		}
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		/*if(!filterSuffix){
			filterChain.doFilter(request, response);
			return ;
		}*/
		
		if (this.requestSupportFilter(request)) {
			this.onFilter(request, response);
			try {
				this.doFilterInternal(servletRequest, servletResponse, filterChain);
			} finally {
				this.onFinally(request, response);
			}
		}else {
			filterChain.doFilter(request, response);
		}
	}

	abstract public void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException ;


	/***
	 * 请求是否支持过滤器
	 * 没有配置的默认不需要过滤器链
	 * @param request
	 * @return 返回false 表示请求不需要经过过滤器，返回true 表示请求需要经过过滤器链
	 * @throws ServletException
	 */
	protected boolean requestSupportFilter(HttpServletRequest request) throws ServletException {
		if(!filterSuffix){//如果设置了不要过滤后缀,必须经过过滤器
			return true;
		}
		String uri = RequestUtils.getServletPath(request);

		if(!uri.contains(".")){//没有后缀的url
			return true;
		}
		
		for (String suffix : excludeSuffixs) {//此类后缀的将不会经过过滤器
			if (uri.endsWith(suffix))
				return false;
		}
		
		for (String suffix : includeSuffixs) {//此类后缀的必须经过过滤器
			if (uri.endsWith(suffix))
				return true;
		}
		

		//没有配置的默认不需要过滤
		return false;
	}
	
	public void destroy(){
	}
}
