package gui.graph;

import java.util.Collection;
import java.util.Map;

public interface IterableSample {
	@FunctionalInterface
	public static interface Consumer {
		void next(double key, double value) throws Throwable;
	}
	
	void forEach(IterableSample.Consumer consumer) throws Throwable;
	
	default boolean isEmpty() {
		return 0>=size();
	}
	
	static <K extends Number, V extends Number> IterableSample iterable(
			Collection<Map.Entry<K, V>> collection) {
		return new IterableSample() {
			@Override
			public void forEach(IterableSample.Consumer consumer)
					throws Throwable {
				for (Map.Entry<K, V> entry: collection) {
					consumer.next(
							entry.getKey().doubleValue(),
							entry.getValue().doubleValue());
				}
			}
			
			@Override
			public int size() {
				return collection.size();
			}
		};
	}
	
	int size();
}
