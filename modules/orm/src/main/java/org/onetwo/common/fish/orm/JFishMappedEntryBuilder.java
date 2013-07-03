package org.onetwo.common.fish.orm;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.onetwo.common.exception.BaseException;
import org.onetwo.common.fish.JFishQueryableMappedEntryImpl;
import org.onetwo.common.fish.annotation.JFishColumn;
import org.onetwo.common.fish.annotation.JFishEntity;
import org.onetwo.common.fish.annotation.JFishQueryable;
import org.onetwo.common.fish.orm.AbstractDBDialect.StrategyType;
import org.onetwo.common.fish.spring.JFishSpringUtils;
import org.onetwo.common.log.MyLoggerFactory;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.utils.AnnotationInfo;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.JFishFieldInfoImpl;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.common.utils.JFishPropertyInfoImpl;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.ReflectUtils;
import org.onetwo.common.utils.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;

@SuppressWarnings("rawtypes")
public class JFishMappedEntryBuilder implements MappedEntryBuilder, InitializingBean, ApplicationContextAware, Ordered {

	protected final Logger logger = MyLoggerFactory.getLogger(this.getClass());
	
//	private Map<String, JFishMappedEntry> entryCache = new HashMap<String, JFishMappedEntry>();
	private boolean byProperty = true;
	private DBDialect dialect;
	private ApplicationContext applicationContext;
	
	private int order = Ordered.LOWEST_PRECEDENCE;
	
	private MappedEntryBuilderListenerManager listenerManager;

	public JFishMappedEntryBuilder(){
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.dialect = JFishSpringUtils.getJFishDBDiaclet(applicationContext);
		Assert.notNull(dialect);
		List<MappedEntryBuilderListener> mappedEntryBuilderListener = SpringUtils.getBeans(applicationContext, MappedEntryBuilderListener.class);
		listenerManager = new MappedEntryBuilderListenerManager(mappedEntryBuilderListener);
	}

	protected DBDialect getDialect() {
		return dialect;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}


	public boolean isSupported(Object entity){
		if(MetadataReader.class.isInstance(entity)){
			MetadataReader metadataReader = (MetadataReader) entity;
			AnnotationMetadata am = metadataReader.getAnnotationMetadata();
			return am.hasAnnotation(JFishEntity.class.getName()) || am.hasAnnotation(JFishQueryable.class.getName());
		}else{
			Class<?> entityClass = ReflectUtils.getObjectClass(entity);
			return entityClass.getAnnotation(JFishEntity.class)!=null || entityClass.getAnnotation(JFishQueryable.class)!=null;
		}
	}
	
//	@Override
//	public JFishMappedEntry findEntry(Object object) {
//		try {
//			return getEntry(object);
//		} catch (Exception e) {
//			logger.error("can find the entry : " + object);
//		}
//		return null;
//	}

	/* (non-Javadoc)
	 * @see org.onetwo.common.db.wheel.MappedEntityManager#getEntry(java.lang.Object)
	 */
	public JFishMappedEntry getEntry(Object objects){
		Assert.notNull(objects, "entity can not be null");
		JFishMappedEntry entry = null;
		
		Object object = LangUtils.getFirst(objects);
		if(!isSupported(object)){
			LangUtils.throwBaseException("don't support this object : " + object);
//			return null;
		}
		
		if(object instanceof Map){
			Map mapEntity = (Map)object;
//			boolean removeMeta = true;
			/*if(mapEntity.containsKey(TMeta.entity_meta)){
				mapEntity = (Map)mapEntity.get(TMeta.entity_meta);
//				removeMeta = false;
			}*/
			
			entry = _buildMappedEntry(mapEntity);
		}else if(object instanceof Class){
			Class entityClass = (Class<?>)object;
			
			/*key = entityClass.getName();
			entry = getFromCache(key);
			if(entry!=null)
				return entry;*/
			
			entry = buildMappedEntry(entityClass, byProperty);
		}else{
			Class entityClass = object.getClass();
			
			/*key = entityClass.getName();
			entry = getFromCache(key);
			if(entry!=null)
				return entry;*/
			
			entry = buildMappedEntry(entityClass, byProperty);
		}
		
		if(entry==null)
			LangUtils.throwBaseException("no entry created : "+object);
		
//		putInCache(key, entry);
		
		return entry;
	}
	

	@Override
	public JFishMappedEntry buildMappedEntry(Object object) {
		Object entity = LangUtils.getFirst(object);
		Class<?> entityClass = ReflectUtils.getObjectClass(entity);
		return buildMappedEntry(entityClass, byProperty);
	}
	
	protected void afterBuildMappedEntry(DBDialect dbDialect, JFishMappedEntry entry){
		this.checkIdStrategy(dbDialect, entry); 
	}
	
	/********
	 * 自动检查更正id策略
	 * @param dbDialect
	 * @param entry
	 */
	protected void checkIdStrategy(DBDialect dbDialect, JFishMappedEntry entry){
		if(entry.getIdentifyField()==null)
			return ;
		if(entry.getIdentifyField().getStrategyType()!=null && !dbDialect.isSupportedIdStrategy(entry.getIdentifyField().getStrategyType())){
			if(!dbDialect.isAutoDetectIdStrategy()){
				LangUtils.throwBaseException("db["+dbDialect.getDbmeta().getDbName()+"] do not support this strategy : " + entry.getIdentifyField().getStrategyType());
			}else{
				entry.getIdentifyField().setStrategyType(dbDialect.getIdStrategy().get(0));
			}
		}
	}
	
	/*********
	 * 1. 创建一个实体映射对象
	 * 2. 把所有实体的注解添加到对象
	 * @param entityClass
	 * @return
	 */
	protected JFishMappedEntry createJFishMappedEntry(AnnotationInfo annotationInfo){
		AbstractJFishMappedEntryImpl entry = null;
		Class<?> entityClass = annotationInfo.getSourceClass();
		JFishEntity jentity = entityClass.getAnnotation(JFishEntity.class);
		JFishQueryable jqueryable = entityClass.getAnnotation(JFishQueryable.class);

		if(jentity==null && jqueryable==null)
			throw new BaseException("it's not a valid entity : " + entityClass);

		TableInfo tableInfo = newTableInfo(annotationInfo);
		if(jqueryable!=null){
			entry = new JFishQueryableMappedEntryImpl(annotationInfo, tableInfo);
		}else{
			if(jentity.type()==MappedType.QUERYABLE_ONLY){
				entry = new JFishQueryableMappedEntryImpl(annotationInfo, tableInfo);
			}else if(jentity.type()==MappedType.JOINED){
				entry = new JFishJoinedMappedEntryImpl(annotationInfo, tableInfo);
			}else{
				entry = new JFishMappedEntryImpl(annotationInfo, tableInfo);
			}
		}
		entry.setSqlBuilderFactory(this.dialect.getSqlBuilderFactory());
		return entry;
	}

	/*********
	 * build entity mapped info and put it into cache
	 * @param entityClass
	 * @param byProperty
	 * @return
	 */
	public JFishMappedEntry buildMappedEntry(Class<?> entityClass, boolean byProperty) {
		AnnotationInfo annotationInfo = new AnnotationInfo(entityClass);
		JFishMappedEntry entry = createJFishMappedEntry(annotationInfo);
		this.listenerManager.notifyAfterCreatedMappedEntry(entry);
		if(byProperty){
			entry = _buildMappedEntryByProperty(entry);
		}else{
			entry = _buildMappedEntryByField(entry);
		}
		
//		TableInfo tableInfo = entry.getTableInfo();
//		entry.build(tableInfo);
		afterBuildMappedEntry(dialect, entry);
		this.listenerManager.notifyAfterBuildMappedEntry(entry);
		
		return entry;
	}

	protected JFishMappedEntry _buildMappedEntryByProperty(JFishMappedEntry entry) {
		Class<?> entityClass = entry.getEntityClass();
		PropertyDescriptor[] props = ReflectUtils.desribProperties(entityClass);
		
		AbstractMappedField mfield = null;
//		List<AbstractMappedField> mfieldList = new ArrayList<AbstractMappedField>(props.length);
		for(PropertyDescriptor prop : props){
			mfield = createAndBuildMappedField(entry, new JFishPropertyInfoImpl(entityClass, prop));
			if(mfield==null)
				continue;
			this.listenerManager.notifyAfterBuildMappedField(entry, mfield);
			entry.addMappedField(mfield);
		}
		return entry;
	}

	protected JFishMappedEntry _buildMappedEntryByField(JFishMappedEntry entry) {
		Class<?> entityClass = entry.getEntityClass();
		Collection<Field> fields = ReflectUtils.findFieldsFilterStatic(entityClass);
		
		AbstractMappedField mfield = null;
		for(Field field : fields){
			mfield = createAndBuildMappedField(entry, new JFishFieldInfoImpl(entityClass, field));
			if(mfield==null)
				continue;
			entry.addMappedField(mfield);
		}

		return entry;
	}
	
	protected boolean ignoreMappedField(JFishProperty field){
		return Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers());
	}

	protected TableInfo newTableInfo(AnnotationInfo entry){
		TableInfo tableInfo = new TableInfo(buildTableName(entry));
		tableInfo.setSeqName(buildSeqName(entry, tableInfo));
		return tableInfo;
	}
	
	protected String buildSeqName(AnnotationInfo entry, TableInfo tableInfo){
//		String sname = entry.getEntityClass().getSimpleName();
		String sname = "SEQ_" + tableInfo.getName().toLowerCase();
		return sname;
	}
	
	protected String buildTableName(AnnotationInfo entry){
		String tname = null;
		if(entry.getAnnotation(JFishQueryable.class)!=null){
			JFishQueryable queryable = entry.getAnnotation(JFishQueryable.class);
			tname = queryable.table();
		}
		
		//JFishEntity's table name will be override JFishQueryable
		if(entry.getAnnotation(JFishEntity.class)!=null){
			JFishEntity jfishEntity = entry.getAnnotation(JFishEntity.class);
			tname = jfishEntity.table();
		}
		if(StringUtils.isBlank(tname))
			tname = StringUtils.convert2UnderLineName(entry.getSourceClass().getSimpleName());
		/*if(StringUtils.isBlank(tname)){
			LangUtils.throwBaseException("table can not be empty: " + entry.getEntityClass());
		}*/
		return tname;
	}

	protected BaseColumnInfo buildColumnInfo(TableInfo tableInfo, JFishMappedField field){
//		Method method = ReflectUtils.getReadMethod(entityClass, field.getProperty());
		
		String colName = null;
		JFishColumn jc = field.getPropertyInfo().getAnnotation(JFishColumn.class);
		if(jc!=null){
			colName = jc.name();
		}else{
			colName = field.getName();
			colName = StringUtils.convert2UnderLineName(colName);
		}
		
		ColumnInfo col = new ColumnInfo(tableInfo, colName);
		col.setJavaType(field.getPropertyInfo().getType());
		col.setPrimaryKey(field.isIdentify());
		
		return col;
	}
	
	/*protected void buildPKColumnInfo(AbstractMappedField field, ColumnInfo col){
		col.setPrimaryKey(true);
	}*/

	protected JFishMappedEntry _buildMappedEntry(Map entity){
		throw new IllegalArgumentException("unsupported map entity : " + entity);
		/*JFishMappedEntry entry = new JFishMappedEntryImpl(entity.getClass());
		//TODO
		return entry;*/
	}

	protected Object getValueFromMapEntity(Map entity, String key, boolean remove){
		if(!entity.containsKey(key))
			return null;
		Object val = entity.get(key);
		if(remove)
			entity.remove(key);
		return val;
	}
	
	/*******
	 * 通过类的属性
	 * 1. 创建字段映射对象
	 * 2. 添加字段的所有注解到对象
	 * 3. 设置对象是否为identify
	 * @param entry
	 * @param prop
	 * @return
	 */

	protected AbstractMappedField createAndBuildMappedField(JFishMappedEntry entry, JFishProperty prop){
		if(ignoreMappedField(prop))
			return null;
		
		AbstractMappedField mfield = newMappedField(entry, prop);
		this.buildMappedField(mfield);
		
		BaseColumnInfo col = this.buildColumnInfo(entry.getTableInfo(), mfield);
		
		//设置关系
		if(col!=null){
			mfield.setColumn(col);
			entry.getTableInfo().addColumn(col);
		}
		return mfield;
	}
	
	protected AbstractMappedField newMappedField(JFishMappedEntry entry, JFishProperty prop){
		JFishMappedProperty mfield = new JFishMappedProperty(entry, prop);
		return mfield;
	}

	protected void buildMappedField(JFishMappedField mfield){
		if("id".equals(mfield.getName()))
			mfield.setIdentify(true);
		if(this.getDialect().getDbmeta().isMySQL()){
			mfield.setStrategyType(StrategyType.increase_id);
		}else if(this.getDialect().getDbmeta().isOracle()){
			mfield.setStrategyType(StrategyType.seq);
		}
		
//		this.buildMappedFieldByAnnotations(mfield);
	}
	
	/*private void buildMappedFieldByAnnotations(AbstractMappedField mfield){
		//TODO
		JFishMeta jfishColumn = mfield.getPropertyInfo().getAnnotation(JFishMeta.class);
		if(mfield.getPropertyInfo().hasAnnotation(JFishMeta.class)){
			this.buildJFishGen(mfield, jfishColumn);
		}
	}
	
	private void buildJFishGen(AbstractMappedField mfield, JFishMeta gen){
		//TODO
	}*/

	public void setDialect(DBDialect dialect) {
		this.dialect = dialect;
	}


	public int getOrder() {
		return order;
	}


	public void setOrder(int order) {
		this.order = order;
	}

}
