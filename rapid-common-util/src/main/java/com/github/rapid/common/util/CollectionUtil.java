package com.github.rapid.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.github.rapid.common.beanutils.BeanUtils;
import com.github.rapid.common.beanutils.PropertyUtils;

/**
 * @author badqiu
 */
public class CollectionUtil {
	
	public CollectionUtil(){}
	
	public static Object safeGet(List list,int index,Object defaultValue) {
		if(list == null) return defaultValue;
		if(list.size() - 1 < index) {
			return defaultValue;
		}
		return list.get(index);
	}
	
	public static LinkedHashSet asLinkedHashSet(Collection c) {
		return (LinkedHashSet)asTargetTypeCollection(c,LinkedHashSet.class);
	}
	
	public static HashSet asHashSet(Collection c) {
		return (HashSet)asTargetTypeCollection(c,HashSet.class);
	}
	
	public static ArrayList asArrayList(Collection c) {
		return (ArrayList)asTargetTypeCollection(c,ArrayList.class);
	}
	
	public static Collection asTargetTypeCollection(Collection c,Class targetCollectionClass) {
		if(targetCollectionClass == null) 
			throw new IllegalArgumentException("'targetCollectionClass' must be not null");
		if(c == null)
			return null;
		if(targetCollectionClass.isInstance(c)) 
			return c;
		
		Collection result = null;
		
		try {
			result = (Collection)targetCollectionClass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("targetCollectionClass="+targetCollectionClass.getName()+" is not correct!",e);
		}
		
		result.addAll(c);
		return result;
	}

	public static List selectProperty(Iterable from,String propertyName) {
		if(propertyName == null) throw new IllegalArgumentException("'propertyName' must be not null");
		if(from == null) return null;
		
		List result = new ArrayList();
		for(Object o : from) {
			try {
				if(o == null) {
					result.add(null);
				}else {
					Object value = PropertyUtils.getSimpleProperty(o, propertyName);
					result.add(value);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Cannot get propertyValue by propertyName:"+propertyName+" on class:"+o.getClass(),e);
			} 
		}
		return result;
	}
	
	public static Object findSingleObject(Collection c) {
		if(c == null || c.isEmpty())
			return null;
		if(c.size() > 1)
			throw new IllegalStateException("found more than one object when single object requested");
		return c.iterator().next();
	}

	public static double avg(Iterable objects,String propertyName) {
		List<Number> propertyValues = CollectionUtil.selectProperty(objects, propertyName);
		return avg(propertyValues);
	}
	
	public static double avg(Collection<Number> values) {
		if(values == null) return 0;
		if(values.isEmpty()) return 0;
		return sum(values) / values.size();
	}
	
	public static double sum(Iterable objects,String propertyName) {
		if(objects == null) return 0;
		List<Number> propertyValues = CollectionUtil.selectProperty(objects, propertyName);
		return sum(propertyValues);
	}

	public static double sum(Iterable<Number> values) {
		if(values == null) return 0;
		
		double sum = 0;
		for(Number num : values) {
			if(num == null) continue;
			sum += num.doubleValue();
		}
		return sum;
	}
	
	public static Object max(Collection objects,String propertyName) {
		List<Comparable> propertyValues = CollectionUtil.selectProperty(objects, propertyName);
		return Collections.max(propertyValues);
	}

	public static Object min(Collection objects,String propertyName) {
		List<Comparable> propertyValues = CollectionUtil.selectProperty(objects, propertyName);
		return Collections.min(propertyValues);
	}
	
	public static <T> Map<Object,T> list2Map(Collection<T> rows,String key) {
		if(rows == null) return null;
		
		Map result = new LinkedHashMap();
		for(Object obj : rows) {
			if(obj instanceof Map) {
				Map map = (Map)obj;
				if(map.containsKey(key)) {
					Object keyValue = map.get(key);
					result.put(keyValue, map);
				}
			}else {
				if(PropertyUtils.isReadable(obj, key)) {
					Object keyValue = PropertyUtils.getSimpleProperty(obj, key);
					result.put(keyValue, obj);
				}
			}
		}
		return result;
	}
	
	public static <T> Map<Object,Object> list2Map(Collection<T> rows,String mapKeyProperty,String mapValueProperty) {
		if(rows == null) return null;
		
		Map result = new LinkedHashMap();
		for(Object obj : rows) {
			if(obj instanceof Map) {
				Map map = (Map)obj;
				if(map.containsKey(mapKeyProperty)) {
					Object key = map.get(mapKeyProperty);
					Object value = map.get(mapValueProperty);
					if(key != null) {
						result.put(key, value);
					}
				}
			}else {
				if(PropertyUtils.isReadable(obj, mapKeyProperty)) {
					Object key = PropertyUtils.getSimpleProperty(obj, mapKeyProperty);
					Object value = PropertyUtils.getSimpleProperty(obj, mapValueProperty);
					if(key != null) {
						result.put(key, value);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 将rows转换成目标类型:targetClass,并返回
	 * 
	 * @param rows
	 * @param targetClass
	 * @return
	 */
	public static <T> List<T> toBeanList(List<Map> rows,Class<T> targetClass) {
		List<T> result = new ArrayList<T>();
		for(Map row : rows) {
			if(MapUtils.isEmpty(row))
				continue;
			
			try {
				T item = targetClass.newInstance();
				BeanUtils.populate(item, row);
				result.add(item);
			}catch(Exception e) {
				throw new RuntimeException("error on process row:"+row,e);
			}
		}
		return result;
	}
	
	public static List<Map<String, String>> toMaps(List<String> columnNames,List<List<String>> inputRows) {
		List<Map<String,String>> resultRows = new ArrayList<Map<String,String>>();
		for(List<String> columns : inputRows) {
			Map row = toMap(columnNames, columns);
			resultRows.add(row);
		}
		return resultRows;
	}

	private static Map toMap(List<String> columnNames, List<String> columns) {
		Map row = new LinkedHashMap();
		for(int col = 0; col < columnNames.size(); col++) {
			if(col >= columns.size()) {
				break;
			}
			
			String columnName = columnNames.get(col);
			if(columnName == null) continue;
			
			String key = columnName.replaceAll("\\s", "").trim();
			String value = StringUtils.trim(columns.get(col));
			row.put(key, value);
		}
		return row;
	}

}
