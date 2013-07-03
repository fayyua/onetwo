package org.onetwo.common.spring.utils;

import java.util.ArrayList;
import java.util.List;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.log.MyLoggerFactory;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.ReflectUtils;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

public class JFishResourcesScanner implements ResourcesScanner {

	protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	protected final Logger logger = MyLoggerFactory.getLogger(this.getClass());

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

	public JFishResourcesScanner(){}
	
	public JFishResourcesScanner(String resourcePattern) {
		super();
		this.resourcePattern = resourcePattern;
	}


	@Override
	public List<Class<?>> scanClasses(String... packagesToScan) {
		return scan(new ScanResourcesCallback<Class<?>>() {

			@Override
			public boolean isCandidate(MetadataReader metadataReader) {
				return true;
			}

			@Override
			public Class<?> doWithCandidate(MetadataReader metadataReader, Resource resource, int count) {
				Class<?> cls = ReflectUtils.loadClass(metadataReader.getClassMetadata().getClassName(), false);
				return cls;
			}
			
		}, packagesToScan);
	}

	@Override
	public <T> List<T> scan(ScanResourcesCallback<T> filter, String... packagesToScan) {
		Assert.notNull(filter);
		List<T> classesToBound = new ArrayList<T>();
		try {
			int count = 0;
			for (String pack : packagesToScan) {
				pack = resolveBasePackage(pack);
				if(!pack.endsWith("/"))
					pack += "/";
				String locationPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + pack + resourcePattern;
				Resource[] resources = this.resourcePatternResolver.getResources(locationPattern);
				if (LangUtils.isEmpty(resources))
					continue;
				for (Resource resource : resources) {
					if (resource.isReadable()) {
						MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
						if (filter.isCandidate(metadataReader)) {
							T obj = filter.doWithCandidate(metadataReader, resource, count++);
							if(obj!=null){
								classesToBound.add(obj);
							}
						}
					} else {
						if (logger.isTraceEnabled()) {
							logger.trace("Ignored because not readable: " + resource);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new BaseException("scan resource in[" + LangUtils.toString(packagesToScan) + "] error : " + e.getMessage(), e);
		}
		return classesToBound;
	}

	protected String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
	}
}
