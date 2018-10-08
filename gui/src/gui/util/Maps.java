package gui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

public class Maps {
	private Maps() {
	}
	
	public static void add(Map<Long, Long> map, Long key, Long value) {
		Long value0=map.get(key);
		map.put(key, (null==value0)?value:(value0+value));
	}
	
	public static Map<Long, Long> add(Map<Long, Long> map0,
			Map<Long, Long> map1) {
		Map<Long, Long> result=new HashMap<>(map0);
		map1.forEach((key, value1)->add(result, key, value1));
		return result;
	}
	
	public static <K, T, V> NavigableMap<K, V> apply(Function<T, V> function,
			Map<K, T> map) {
		NavigableMap<K, V> result=new TreeMap<>();
		map.forEach((key, object)->{
			V value=function.apply(object);
			if (null!=value) {
				result.put(key, value);
			}
		});
		return result;
	}
	
	public static void min(Map<Long, Long> map, Long key, Long value) {
		Long value0=map.get(key);
		if ((null==value0)
				|| (value0>value)) {
			map.put(key, value);
		}
	}
	
	public static Map<Long, Long> min(Map<Long, Long> map0,
			Map<Long, Long> map1) {
		Map<Long, Long> result=new HashMap<>(map0);
		map1.forEach((key, value1)->min(result, key, value1));
		return result;
	}
	
	public static NavigableMap<Double, Double> toDouble(
			Map<? extends Number, ? extends Number> map) {
		NavigableMap<Double, Double> result=new TreeMap<>();
		map.forEach((key, value)->
				result.put(key.doubleValue(), value.doubleValue()));
		return result;
	}
}
