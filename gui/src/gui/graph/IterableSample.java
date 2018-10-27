package gui.graph;

import java.util.Collection;
import java.util.Map;

public interface IterableSample {
	@FunctionalInterface
	public static interface DoubleConsumer {
		void next(double key, double value);
	}
	
	@FunctionalInterface
	public static interface LongConsumer {
		void next(long key, double value);
	}
	
	void forEachDouble(DoubleConsumer consumer);
	
	void forEachLong(LongConsumer consumer);
	
	default boolean isEmpty() {
		return 0>=size();
	}
	
	static <K extends Number, V extends Number> IterableSample iterable(
			Collection<Map.Entry<K, V>> collection) {
		return new IterableSample() {
			@Override
			public void forEachDouble(DoubleConsumer consumer) {
				collection.forEach((entry)->consumer.next(
						entry.getKey().doubleValue(),
						entry.getValue().doubleValue()));
			}
			
			@Override
			public void forEachLong(LongConsumer consumer) {
				collection.forEach((entry)->consumer.next(
						entry.getKey().longValue(),
						entry.getValue().doubleValue()));
			}
			
			@Override
			public int size() {
				return collection.size();
			}
		};
	}
	
	int size();
}
