package org.onetwo.common.fish.orm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onetwo.common.fish.annotation.JFishFieldListeners;
import org.onetwo.common.fish.event.JFishEntityFieldListener;
import org.onetwo.common.fish.event.JFishEventAction;
import org.onetwo.common.fish.orm.AbstractDBDialect.StrategyType;
import org.onetwo.common.utils.JFishProperty;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.StringUtils;


@SuppressWarnings("unchecked")
abstract public class AbstractMappedField implements JFishMappedField{
	
	private final JFishMappedEntry entry;
	
	private String name;
	private String label;
	private boolean identify;
//	private PropertyDescriptor property;
	private BaseColumnInfo column;

	private JFishMappedFieldType mappedFieldType = JFishMappedFieldType.FIELD;
	private StrategyType strategyType;
	private final JFishProperty propertyInfo;
	

	private DataHolder<String, Object> dataHolder = new DataHolder<String, Object>();
	
	private boolean freezing;
	
	private List<JFishEntityFieldListener> fieldListeners = Collections.EMPTY_LIST;
	
	public AbstractMappedField(JFishMappedEntry entry, JFishProperty propertyInfo) {
		super();
		this.entry = entry;
		this.propertyInfo = propertyInfo;
		this.name = propertyInfo.getName();
		
		JFishFieldListeners listenersAnntation = propertyInfo.getAnnotation(JFishFieldListeners.class);
		if(listenersAnntation!=null){
			fieldListeners = OrmUtils.initJFishEntityFieldListeners(listenersAnntation);
		}else{
			if(!entry.getFieldListeners().isEmpty()){
				this.fieldListeners = new ArrayList<JFishEntityFieldListener>(entry.getFieldListeners());
			}
		}
	}

//	abstract public Class getMappedType();
	
	/*abstract public Method getReadMethod();
	
	abstract protected Method getWriteMethod();*/
	
	@Override
	public void setValue(Object entity, Object value){
		propertyInfo.setValue(entity, value);
	}
	
	@Override
	public Object getValue(Object entity){
		return propertyInfo.getValue(entity);
	}
	
	@Override
	public void setColumnValue(Object entity, Object value){
		propertyInfo.setValue(entity, value);
	}
	

	@Override
	public Object getColumnValue(Object entity){
		return propertyInfo.getValue(entity);
	}
	

	public Object getColumnValueWithJFishEventAction(Object entity, JFishEventAction eventAction){
		Object oldValue = getColumnValue(entity);
		Object newValue = oldValue;
		boolean doListener = false;
		if(JFishEventAction.insert==eventAction){
			if(!getFieldListeners().isEmpty()){
				for(JFishEntityFieldListener fl : getFieldListeners()){
					newValue = fl.beforeFieldInsert(getName(), newValue);
					doListener = true;
				}
			}
		}else if(JFishEventAction.update==eventAction){
			if(!getFieldListeners().isEmpty()){
				for(JFishEntityFieldListener fl : getFieldListeners()){
					newValue = fl.beforeFieldUpdate(getName(), newValue);
					doListener = true;
				}
			}
		}
		if(doListener){
			setColumnValue(entity, newValue);
		}
		return newValue;
	}
	
	public Class<?> getColumnType(){
		return propertyInfo.getType();
	}
	
	@Override
	public boolean isIdentify() {
		return identify;
	}


	@Override
	public void setIdentify(boolean identify) {
		this.checkFreezing("setIdentify");
		this.identify = identify;
	}

	@Override
	public BaseColumnInfo getColumn() {
		return column;
	}

	@Override
	public void setColumn(BaseColumnInfo column) {
		this.checkFreezing("setColumn");
		this.column = column;
	}

	@Override
	public String getName() {
		return name;
	}

	void setName(String name) {
		this.checkFreezing("setName");
		this.name = name;
	}


	@Override
	public JFishMappedEntry getEntry() {
		return entry;
	}

	@Override
	public String getLabel() {
		if(StringUtils.isBlank(label)){
			return getName();
		}
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public boolean isGeneratedValueFetchBeforeInsert() {
		return isSeqStrategy();
	}

	@Override
	public boolean isGeneratedValue() {
		return this.strategyType!=null;
	}

	@Override
	public boolean isSeqStrategy() {
		return this.strategyType==StrategyType.seq;
	}

	@Override
	public boolean isIncreaseIdStrategy() {
		return this.strategyType==StrategyType.increase_id;
	}

	@Override
	public StrategyType getStrategyType() {
		return strategyType;
	}

	@Override
	public void setStrategyType(StrategyType strategyType) {
		this.strategyType = strategyType;
	}

	@Override
	public JFishProperty getPropertyInfo() {
		return propertyInfo;
	}

	@Override
	public void freezing() {
		freezing = true;
	}

	@Override
	public void checkFreezing(String name) {
		if(isFreezing()){
			throw new UnsupportedOperationException("the field["+getName()+"] is freezing now, don not supported this operation : " + name);
		}
	}

	@Override
	public boolean isFreezing() {
		return freezing;
	}

	@Override
	public JFishMappedFieldType getMappedFieldType() {
		return mappedFieldType;
	}

	@Override
	public final void setMappedFieldType(JFishMappedFieldType mappedFieldType) {
		this.mappedFieldType = mappedFieldType;
	}
	
	@Override
	public boolean isJoinTableField(){
		return false;
	}

	@Override
	public DataHolder<String, Object> getDataHolder() {
		return dataHolder;
	}
	
	public String toString(){
		return LangUtils.append(getName());
	}

	public List<JFishEntityFieldListener> getFieldListeners() {
		return fieldListeners;
	}
	
	
}
