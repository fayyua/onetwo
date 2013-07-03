package org.onetwo.common.utils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.onetwo.common.exception.BaseException;

public class JFishPropertyInfoImpl extends AbstractJFishProperty {
	
	private final Method readMethod;
	private final Method writeMethod;
	private final Class<?> propertyType;
	private final PropertyDescriptor property;


	public JFishPropertyInfoImpl(Class<?> beanClass, String propName){
		this(ClassWrapper.wrap(beanClass), ReflectUtils.findProperty(beanClass, propName));
	}

	public JFishPropertyInfoImpl(Class<?> beanClass, PropertyDescriptor property){
		this(ClassWrapper.wrap(beanClass), property);
	}

	public JFishPropertyInfoImpl(ClassWrapper<?> beanClassWrapper, PropertyDescriptor property){
		super(property.getName(), beanClassWrapper);
		Class<?> beanClass = beanClassWrapper.getClazz();
		this.property = property;
		if(property.getReadMethod()==null || property.getReadMethod().isBridge()){
			readMethod = ReflectUtils.getReadMethod(beanClass, property.getName(), property.getPropertyType());
			writeMethod = ReflectUtils.getWriteMethod(beanClass, property.getName());
			if(readMethod!=null){
				propertyType = readMethod.getReturnType();
			}else{
				propertyType = null;
			}
		}else{
			propertyType = property.getPropertyType();
			readMethod = property.getReadMethod();
			writeMethod = property.getWriteMethod();
		}
		Annotation[] annotations = getReadMethod().getAnnotations();
		annotationInfo = new AnnotationInfo(beanClass, annotations);
		this.propertyClassWrapper = ClassWrapper.wrap(propertyType);
	}

	@Override
	public void setValue(Object entity, Object value){
		Assert.notNull(entity);
		if(getWriteMethod()==null){
			throw new BaseException("property["+getName()+"] of ["+getBeanClass()+"] h write method ");
		}
		try {
			ReflectUtils.invokeMethod(getWriteMethod(), entity, value);
		} catch (Exception e) {
			throw new BaseException("set entity["+beanClassWrapper.getClazz()+"] property["+getName()+"] propertyType["+this.getType()+"]value error", e);
		}
	}
	
	
	@Override
	public Class<?> getBeanClass() {
		return beanClassWrapper.getClazz();
	}


	@Override
	public Object getValue(Object entity){
		Assert.notNull(entity);
		if(getReadMethod()==null){
			throw new BaseException("property["+getName()+"] of ["+getBeanClass()+"] h read method ");
		}
		return ReflectUtils.invokeMethod(getReadMethod(), entity);
	}
	
	public Method getReadMethod() {
		return readMethod;
	}
	public Method getWriteMethod() {
		return writeMethod;
	}

	@Override
	public Class<?> getType() {
		return propertyType;
	}
	
	@Override
	public Field getField() {
		return beanClassWrapper.getField(getName());
	}

	public List<Annotation> getAnnotations() {
		return annotationInfo.getAnnotations();
	}

	public boolean hasAnnotation(Class<? extends Annotation> annoClass) {
		return annotationInfo.hasAnnotation(annoClass);
	}

	public <T extends Annotation> T getAnnotation(Class<T> annoClass) {
		return annotationInfo.getAnnotation(annoClass);
	}

	public boolean isTransientModifier(){
		return Modifier.isTransient(readMethod.getModifiers());
	}

	@Override
	public int getModifiers() {
		return readMethod.getModifiers();
	}
	
	public String toString(){
		return getName();
	}

	@Override
	public ClassWrapper<?> getBeanClassWrapper() {
		return beanClassWrapper;
	}

	@Override
	public PropertyDescriptor getPropertyDescriptor() {
		return property;
	}

}
