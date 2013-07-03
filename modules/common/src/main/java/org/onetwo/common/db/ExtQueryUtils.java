package org.onetwo.common.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.onetwo.common.db.ExtQuery.K.IfNull;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.ReflectUtils;
import org.onetwo.common.utils.StringUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class ExtQueryUtils {
	
	public static class F {
		public static final String POSTFIX = "_____%&^&*#$#$%____";
		/*public static final String func(String func){
			return codeString(func, K.FUNC, "");
		}
		public static final String unFunc(String func){
			return decodeString(func, K.FUNC, "");
		}*/
		public static final QueryField sqlFunc(String func){
			return SqlFuncFieldImpl.create(func);
		}
		/*public static final String unSqlFunc(String func){
			return decodeString(func, K.RAW_FUNC, POSTFIX);
		}*/
		
		public static final RawSqlWrapper sqlSelect(String sql){
			return RawSqlWrapper.wrap(sql);
		}
		
		public static final RawSqlWrapper sqlJoin(String sql){
			return RawSqlWrapper.wrap(sql);
		}
		/*public static final String unSqlJoin(RawSqlWrapper sqlJoin){
			return sqlJoin.getRawSql();
		}*/
		
		public static final String codeString(String str, String prefix, String postfix){
			return LangUtils.append(prefix, str, postfix);
		}
		public static final String decodeString(String str, String prefix, String postfix){
			String newStr = str.substring(prefix.length());
			newStr = newStr.substring(0, newStr.length()-postfix.length());
			return newStr;
		}
		
	}
	
	public static String[] appendOperationToFields(String[] fields, String op){
		Assert.notEmpty(fields);
		String[] newFileds = null;
		for(String field : fields){
			field = field + SQLSymbolManager.SPLIT_SYMBOL + op;
			newFileds = (String[])ArrayUtils.add(newFileds, field);
		}
		return newFileds;
	}

	public static List processValue(Object fields, Object values, IfNull ifNull){
		return processValue(fields, values, ifNull, false);
	}
	
	/*********
	 * 
	 * @param values
	 * @param ifNull
	 * @return
	 */
	public static List processValue(Object fields, Object values, IfNull ifNull, boolean trimNull){
		List valueList = null;
		if(LangUtils.hasNotElement(values)){
			if(ifNull==IfNull.Ignore){
				return null;
			}else if(ifNull==IfNull.Throw){
				throw LangUtils.asBaseException("the fields["+LangUtils.toString(fields)+"] 's value can not be null or empty.");
			}else {//calm
				valueList = new ArrayList();
				valueList.add(values);
			}
		}else{
			valueList = LangUtils.asList(values, trimNull);
		}
		return valueList;
	}
	
	public static Map field2Map(Object obj){
		if (obj == null)
			return Collections.EMPTY_MAP;
		Collection<Field> fields = ReflectUtils.findFieldsFilterStatic(obj.getClass());
		if (fields == null || fields.isEmpty())
			return Collections.EMPTY_MAP;
		Map rsMap = new HashMap();
		Object val = null;
		for (Field field : fields) {
			val = ReflectUtils.getFieldValue(field, obj, false);
			if (val == null)
				continue;
			String name = field.getName();
			if(val instanceof String){
				name += ":like";
			}else if(Date.class.isInstance(val)){
				name += ":date in";
			}
			rsMap.put(name, val);
		}
		return rsMap;
	}
	
	public static String buildCountSql(String sql, String countValue){
		String countField = "*";
		String hql = StringUtils.substringAfter(sql, "from ");
		if(StringUtils.isBlank(hql)){
			hql = StringUtils.substringAfter(sql, "FROM ");
		}
		hql = StringUtils.substringBefore(hql, " order by ");

		if(StringUtils.isNotBlank(countValue))
			countField = countValue;
		
		hql = "select count(" + countField + ") from " + hql;
		return hql;
	}
	
	public static final String REGX_NOT_WORD = "[^A-Za-z0-9\\u4e00-\\u9fa5]+";

	public static String getLikeString(String like) {
//		String like = str.replaceAll(REGX_NOT_WORD, "%");
		if(like.indexOf('%')!=-1)
			return like;
		
		return "%"+like+"%";
	}
	
}
