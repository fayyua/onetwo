package org.onetwo.common.hibernate;

import java.util.List;
import java.util.Map;

import javax.persistence.FlushModeType;

import org.hibernate.Query;
import org.onetwo.common.db.DataQuery;
import org.onetwo.common.utils.ArrayUtils;
import org.onetwo.common.utils.Page;

@SuppressWarnings("unchecked")
public class HibernateQueryImpl implements DataQuery {
 
	private Query query; 
	
	public HibernateQueryImpl(Query query){
		this.query = query;
	}

	public int executeUpdate() {
		return query.executeUpdate();
	}

	public <T> List<T> getResultList() {
		return query.list();
	}

	public Object getSingleResult() {
		return query.uniqueResult();
	}

	public DataQuery setFirstResult(int startPosition) {
		query.setFirstResult(startPosition);
		return this;
	}

	public DataQuery setMaxResults(int maxResult) {
		query.setMaxResults(maxResult);
		return this;
	}

	public DataQuery setParameter(int position, Object value) {
		query.setParameter(position, value);
		return this;
	}

	public DataQuery setParameter(String name, Object value) {
		query.setParameter(name, value);
		return this;
	}

	public DataQuery setParameters(Map<String, Object> params) {
		if(params==null)
			return this;
		for(Map.Entry<String, Object> entry : params.entrySet()){
			query.setParameter(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public DataQuery setParameters(List<Object> params) {
		if(params==null || params.isEmpty())
			return this;
		int position = 1;
		for(Object value : params){
			query.setParameter(position++, value);
		}
		return this;
	}


	@Override
	public DataQuery setParameters(Object[] params) {
		if(ArrayUtils.hasNotElement(params))
			return this;
		int position = 1;
		for(Object value : params){
			query.setParameter(position++, value);
		}
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	public DataQuery setPageParameter(final Page page) {
		return setLimited(page.getFirst()-1, page.getPageSize());
	}
	
	public DataQuery setLimited(final Integer first, final Integer max) {
		if (first >= 0) {
			query.setFirstResult(first);
		}
		if (max >= 1) {
			query.setMaxResults(max);
		}
		return this;
	}

	public DataQuery setFlushMode(FlushModeType flushMode){
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getRawQuery() {
		return (T)query;
	}

	@Override
	public DataQuery setQueryConfig(Map<String, Object> configs) {
		// TODO Auto-generated method stub
		return this;
	}
}
