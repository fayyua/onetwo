package org.onetwo.common.db;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.utils.Page;

@SuppressWarnings("rawtypes")
public interface BaseEntityManager {

	public <T> T load(Class<T> entityClass, Serializable id);
	
	public <T> T findById(Class<T> entityClass, Serializable id);

	public <T> T save(T entity);
	
	public void persist(Object entity);

	public void remove(Object entity);
	
	public void removeList(List entities);

	public <T> T removeById(Class<T> entityClass, Serializable id);

	public <T> List<T> findAll(Class<T> entityClass);

	public Number countRecord(Class entityClass, Map<Object, Object> properties);

	public Number countRecord(Class entityClass, Object... params);

	public <T> T findUnique(final String sql, final Object... values);
//	public <T> T findUnique(QueryBuilder squery);
	
	public <T> T findUnique(Class<T> entityClass, Object... properties);
	
	public <T> T findUnique(Class<T> entityClass, boolean tryTheBest, Object... properties);
	
	public <T> T findUnique(Class<T> entityClass, Map<Object, Object> properties);

	public <T> List<T> findByProperties(Class entityClass, Object... properties);

	public <T> List<T> findByProperties(Class entityClass, Map<Object, Object> properties);
	
//	public <T> List<T> findList(QueryBuilder squery);
	
//	public void findPage(final Page page, QueryBuilder squery);

	public void findPage(final Class entityClass, final Page page, Object... properties);

	public void findPage(final Class entityClass, final Page page, Map<Object, Object> properties);


	public void delete(ILogicDeleteEntity entity);
	
	public <T extends ILogicDeleteEntity> T deleteById(Class<T> entityClass, Serializable id);
	
	public void flush();
	
	public void clear();
	
	public <T> T merge(T entity);
	
//	public EntityManager getEntityManager();
	
	public DataQuery createSQLQuery(String sqlString, Class entityClass);
	
	public DataQuery createMappingSQLQuery(String sqlString, String resultSetMapping);
	
	public DataQuery createQuery(String ejbqlString);
	
	public DataQuery createNamedQuery(String name);
	public DataQuery createQuery(String sql, Map<String, Object> values);
	
	public Long getSequences(String sequenceName, boolean createIfNotExist);
	public Long getSequences(Class entityClass, boolean createIfNotExist);
//	public SequenceNameManager getSequenceNameManager();
	
	public EntityManagerProvider getEntityManagerProvider();
	
	public SQLSymbolManager getSQLSymbolManager();
	

	public <T> List<T> findList(JFishQueryValue queryValue);
	
	public <T> T findUnique(JFishQueryValue queryValue);
	
	public <T> void findPage(Page<T> page, JFishQueryValue squery);

}