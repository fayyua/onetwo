package org.onetwo.common.utils;


public interface TypeJudge {

	public Object ifList(Object obj);
	public Object ifCollection(Object obj);
	public Object ifMap(Object obj);
	public Object ifArray(Object array);
	public Object other(Object obj, Class<?> toType);
	public Object all(Object obj);
}
