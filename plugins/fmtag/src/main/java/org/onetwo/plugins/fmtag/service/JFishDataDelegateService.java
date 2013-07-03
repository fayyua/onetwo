package org.onetwo.plugins.fmtag.service;

import java.util.List;

import org.onetwo.common.fish.orm.JFishMappedEntry;
import org.onetwo.common.utils.Page;

public interface JFishDataDelegateService {

	public <T> List<T> findListByQName(String queryName, Object... params);

	public <T> Page<T> findPage(Class<T> entityClass, Page<T> page, Object...params);
	
	public JFishMappedEntry getJFishMappedEntry(Object clsName);
}