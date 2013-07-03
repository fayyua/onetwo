package org.onetwo.common.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.onetwo.common.db.sqlext.SQLDialet;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.map.M;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ParamValues {
	
	public static enum PlaceHolder {
		POSITION,
		NAMED
	}

	protected SQLDialet sqlDialet;
	private PlaceHolder holder;
	private Object values;
	
	public ParamValues(SQLDialet sqlDialet){
		this(PlaceHolder.NAMED, sqlDialet);
	}
	
	public ParamValues(PlaceHolder holder, SQLDialet sqlDialet){
		this.sqlDialet = sqlDialet;
		this.holder = holder;
		if(PlaceHolder.POSITION.equals(holder))
			this.values = new ArrayList();
		else
			this.values = new LinkedHashMap();
	}

	public void addValue(String field, Object value, StringBuilder sqlScript){
		if(PlaceHolder.POSITION.equals(holder)){
			List list = getValues();
			if(sqlScript!=null)
				sqlScript.append(this.sqlDialet.getPlaceHolder(list.size()));
			list.add(value);
		}
		else {
			Map map = getValues();
			int size = map.entrySet().size();
			String fieldName = this.sqlDialet.getNamedPlaceHolder(field, size);
			sqlScript.append(":").append(fieldName);
			map.put(fieldName, value);
		}
	}
	
	public void directAddValue(Object value){
		if(PlaceHolder.POSITION.equals(holder)){
			List list = getValues();
			/*if(value instanceof Collection)
				list.addAll((Collection)value);
			else
				list.add(value);*/
			list.add(value);
		}
		else {
			Map map = getValues();
			if(value instanceof Collection){
				Map m = CUtils.asMap(((Collection)value).toArray());
				if(m!=null && !m.isEmpty())
					map.putAll(m);
			}else if(value instanceof Map){
				map.putAll((Map)value);
			}else{
				LangUtils.throwBaseException("unsupported args: "+LangUtils.toString(value));
			}
		}
	}
	
	public void joinToQuery(ExtQuery subQuery){
		if(PlaceHolder.POSITION.equals(holder)){
			List pvs = getValues();
			List subParamValues = (List)subQuery.getParamsValue().getValues();
			subParamValues.addAll(pvs);
			subQuery.build();
			pvs.clear();
			pvs.addAll(subParamValues);
		}else{
			Map pvs = getValues();
			Map subParamValues = subQuery.getParamsValue().getValues();
			subParamValues.putAll(pvs);
			subQuery.build();
			pvs.clear();
			pvs.putAll(subParamValues);
		}
	}
	
	/*********
	 * List Or Map
	 * @return
	 */
	public <T> T getValues(){
		return (T) values;
	}
	public Map asMap(){
		return (Map) getValues();
	}
	public List asList(){
		return (List) getValues();
	}
	
	public boolean isList(){
		return List.class.isInstance(values);
	}
	
	public boolean isMap(){
		return Map.class.isInstance(values);
	}

}
