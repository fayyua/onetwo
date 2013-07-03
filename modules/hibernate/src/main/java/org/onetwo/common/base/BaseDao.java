package org.onetwo.common.base;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.onetwo.common.db.BaseEntityManager;
import org.onetwo.common.db.ILogicDeleteEntity;
import org.onetwo.common.db.IdEntity;
import org.onetwo.common.jdbc.JFishJdbcOperations;
import org.onetwo.common.jdbc.JdbcDao;
import org.onetwo.common.utils.Page;
import org.onetwo.common.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*****
 * 数据访问的基类
 * 
 * @author weishao
 *
 * @param <T>
 * @param <PK>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BaseDao<T extends IdEntity, PK extends Serializable> {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Class<T> entityClass;
//	protected SequenceNameManager sequenceNameManager;
	
	protected JdbcDao jdbcDao;
	
	protected BaseEntityManager baseEntityManager;

	public BaseDao() {
		this.entityClass = ReflectUtils.getSuperClassGenricType(this.getClass());
	}

	public BaseDao(Class<T> clazz, BaseEntityManager baseEntityManager) {
		this.entityClass = clazz;
		this.baseEntityManager = baseEntityManager;
	}


	protected JFishJdbcOperations getJdbcTemplate() {
		return getJdbcDao().getJdbcTemplate();
	}
	
	/**
	 * 获取当前VO所对应的的序列
	 * 
	 * @return
	 * @throws Exception
	 */
	public Long createSequences() {
		return this.getBaseEntityManager().getSequences(entityClass, true);
	}
	/*public SequenceNameManager getSequenceNameManager() {
		return sequenceNameManager;
	}

	@Resource
	public void setSequenceNameManager(SequenceNameManager sequenceNameManager) {
		this.sequenceNameManager = sequenceNameManager;
	}*/

	public JdbcDao getJdbcDao() {
		return jdbcDao;
	}

	@Resource
	public void setJdbcDao(JdbcDao jdbcDao) {
		this.jdbcDao = jdbcDao;
	}

	@Resource
	public void setBaseEntityManager(BaseEntityManager baseEntityManager) {
		this.baseEntityManager = baseEntityManager;
	}

	public BaseEntityManager getBaseEntityManager() {
		return baseEntityManager;
	}

	public Number countRecord(Map<Object, Object> properties) {
		return baseEntityManager.countRecord(entityClass, properties);
	}

	public Number countRecord(Object... params) {
		return baseEntityManager.countRecord(entityClass, params);
	}

	public void delete(ILogicDeleteEntity entity) {
		baseEntityManager.delete(entity);
	}

	public <T extends ILogicDeleteEntity> T deleteById(Class<T> entityClass, Serializable id){
		return baseEntityManager.deleteById(entityClass, id);
	}

	public List<T> findAll() {
		return baseEntityManager.findAll(entityClass);
	}

	public T findById(Serializable id) {
		return baseEntityManager.findById(entityClass, id);
	}

	public List<T> findByProperties(Map<Object, Object> properties) {
		return baseEntityManager.findByProperties(entityClass, properties);
	}

	public List<T> findByProperties(Object... properties) {
		return baseEntityManager.findByProperties(entityClass, properties);
	}

	public void findPage(Page page, Map<Object, Object> properties) {
		baseEntityManager.findPage(entityClass, page, properties);
	}

	public void findPage(Page page, Object... properties) {
		baseEntityManager.findPage(entityClass, page, properties);
	}

	public T findUnique(Map<Object, Object> properties) {
		return (T) baseEntityManager.findUnique(entityClass, properties);
	}

	public T findUnique(Object... properties) {
		return (T) baseEntityManager.findUnique(entityClass, properties);
	}

	public T findUnique(String sql, Object... values) {
		return (T) baseEntityManager.findUnique(sql, values);
	}

	public T load(PK id) {
		return baseEntityManager.load(entityClass, id);
	}

	public void persist(IdEntity entity) {
		baseEntityManager.persist(entity);
	}

	public void remove(IdEntity entity) {
		baseEntityManager.remove(entity);
	}

	public T removeById(PK id) {
		return baseEntityManager.removeById(entityClass, id);
	}

	public void save(IdEntity entity) {
		baseEntityManager.save(entity);
	}

}
