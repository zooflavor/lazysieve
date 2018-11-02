package gui.util;

import gui.graph.Sample;
import java.util.Comparator;
import java.util.Map;

public class Maps {
	private Maps() {
	}
	
	public static void add(Map<Long, Long> map, Long key, Long value) {
		Long value0=map.get(key);
		map.put(key, (null==value0)?value:(value0+value));
	}
	
	public static <K, V> void min(Map<K, V> map, K key, V value,
			Comparator<V> comparator) {
		V value0=map.get(key);
		if ((null==value0)
				|| (0<comparator.compare(value0, value))) {
			map.put(key, value);
		}
	}
	
	public static Sample.Builder toSample(
			Map<? extends Number, ? extends Number> map) {
		Sample.Builder result=Sample.builder(map.size());
		map.forEach((key, value)->
				result.add(key.longValue(), value.doubleValue()));
		return result;
	}
}
