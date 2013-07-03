package org.onetwo.common.utils.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onetwo.common.utils.LangUtils;

public class ListMap<K, V> implements Map<K, List<V>>{
	
	public static <K, V> ListMap<K, V> newListMap(){
		return new ListMap<K, V>(new HashMap<K, List<V>>());
	}
	
	public static <K, V> ListMap<K, V> newLinkedListMap(){
		return new ListMap<K, V>(new LinkedHashMap<K, List<V>>());
	}
	
	public static <K, V> ListMap<K, V> newListMap(Map<K, List<V>> map){
		return new ListMap<K, V>(map);
	}

	private Map<K, List<V>> map;
	
	private ListMap(){
		this(new LinkedHashMap<K, List<V>>());
	}
	
	
	private ListMap(Map<K, List<V>> map) {
		super();
		this.map = map;
	}


	public List<V> putElement(K key, V value){
		List<V> list = get(key);
		if(list==null){
			list = LangUtils.newArrayList();
			map.put(key, list);
		}
		list.add(value);
		return list;
	}


	public int size() {
		return map.size();
	}


	public boolean isEmpty() {
		return map.isEmpty();
	}


	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}


	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}


	public List<V> get(Object key) {
		return map.get(key);
	}


	public List<V> put(K key, List<V> value) {
		return map.put(key, value);
	}


	public List<V> remove(Object key) {
		return map.remove(key);
	}


	public void putAll(Map<? extends K, ? extends List<V>> m) {
		map.putAll(m);
	}


	public void clear() {
		map.clear();
	}


	public Set<K> keySet() {
		return map.keySet();
	}


	public Collection<List<V>> values() {
		return map.values();
	}


	public Set<java.util.Map.Entry<K, List<V>>> entrySet() {
		return map.entrySet();
	}


	public boolean equals(Object o) {
		return map.equals(o);
	}


	public int hashCode() {
		return map.hashCode();
	}
	
	
	
}
