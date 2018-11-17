package gui.io;

import gui.graph.Sample;
import gui.ui.progress.Progress;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Aggregates {
	static final int MAGIC=0xd1d1b0b0;
	
	@FunctionalInterface
	private static interface PrimeGapConsumer {
		void consume(long gap, long start, long occurances) throws Throwable;
	}
	
	private Aggregates() {
	}
	
	public static DatabaseInfo.TypeInfo info(
			NavigableMap<Long, Long> lastModifications) throws Throwable {
		return DatabaseInfo.typeInfo(lastModifications.navigableKeySet());
	}
	
	public static NavigableMap<Long, Long> lastModifications(Progress progress,
			AggregatesReader reader) throws Throwable {
		NavigableMap<Long, Long> lastModifications=new TreeMap<>();
		reader.consume(false,
				(aggregateSupplier, progress2)->{
					Aggregate aggregate=aggregateSupplier.get();
					lastModifications.put(
							aggregate.segmentStart,
							aggregate.lastModification);
				},
				null, progress);
		return lastModifications;
	}
	
	public static NavigableMap<Long, Long> maxPrimeGaps(Progress progress,
			AggregatesReader reader) throws Throwable {
		NavigableMap<Long, Long> result=new TreeMap<>(Long::compareUnsigned);
		class BC implements BiConsumer<Long, Long> {
			private long lastStart;
			private long maxGap;
			private long maxStart;

			@Override
			public void accept(Long start, Long gap) {
				if (0==maxGap) {
					result.put(start, gap);
					maxGap=gap;
					maxStart=start;
				}
				else if (0>Long.compareUnsigned(maxGap, gap)) {
					if (maxStart!=start-1l) {
						result.put(start-1l, maxGap);
					}
					result.put(start, gap);
					maxGap=gap;
					maxStart=start;
				}
				lastStart=start;
			}
		}
		BC bc=new BC();
		newPrimeGaps(progress, reader).forEach(bc);
		if (bc.lastStart!=bc.maxStart) {
			result.put(bc.lastStart, bc.maxGap);
		}
		return result;
	}
	
	public static NavigableMap<Long, Long> newPrimeGaps(Progress progress,
			AggregatesReader reader) throws Throwable {
		NavigableMap<Long, Long> result=new TreeMap<>(Long::compareUnsigned);
		primeGapStarts(progress, reader)
				.forEach((gap, start)->result.put(start, gap));
		return result;
	}
	
	public static Sample.Builder prime12Z11Counts(Progress progress,
			AggregatesReader reader) throws Throwable {
		return sum(progress, reader, Sample.builder(),
				(aggregate)->aggregate.primeCount12Z11,
				0l, null);
	}
	
	public static Sample.Builder prime4Z1Counts(Progress progress,
			AggregatesReader reader) throws Throwable {
		return sum(progress, reader, Sample.builder(),
				(aggregate)->aggregate.primeCount4Z1,
				0l, null);
	}
	
	public static Sample.Builder prime4Z3Counts(Progress progress,
			AggregatesReader reader) throws Throwable {
		return sum(progress, reader, Sample.builder(),
				(aggregate)->aggregate.primeCount4Z3,
				0l, null);
	}
	
	public static Sample.Builder prime6Z1Counts(Progress progress,
			AggregatesReader reader) throws Throwable {
		return sum(progress, reader, Sample.builder(),
				(aggregate)->aggregate.primeCount6Z1,
				0l, null);
	}
	
	public static Sample.Builder primeCounts(Progress progress,
			AggregatesReader reader) throws Throwable {
		return sum(progress, reader, Sample.builder(),
				(aggregate)->aggregate.primeCount4Z1+aggregate.primeCount4Z3,
				1l, null);
	}
	
	public static Sample.Builder primeCountsAbsoluteError(Progress progress,
			AggregatesReader reader) throws Throwable {
		return sum(progress, reader, Sample.builder(),
				(aggregate)->aggregate.primeCount4Z1+aggregate.primeCount4Z3,
				1l,
				(lastNumber, sum)->sum-lastNumber/Math.log(lastNumber));
	}
	
	public static Sample.Builder primeCountsExpectedValue(Progress progress,
			AggregatesReader reader) throws Throwable {
		return sum(progress, reader, Sample.builder(),
				(aggregate)->aggregate.primeCount4Z1+aggregate.primeCount4Z3,
				1l,
				(lastNumber, sum)->lastNumber/Math.log(lastNumber));
	}
	
	public static Sample.Builder primeCountsRelativeError(Progress progress,
			AggregatesReader reader) throws Throwable {
		return sum(progress, reader, Sample.builder(),
				(aggregate)->aggregate.primeCount4Z1+aggregate.primeCount4Z3,
				1l,
				(lastNumber, sum)->(sum-lastNumber/Math.log(lastNumber))/sum);
	}
	
	public static NavigableMap<Long, Long> primeGapFrequencies(
			Progress progress, AggregatesReader reader) throws Throwable {
		NavigableMap<Long, Long> result=new TreeMap<>(Long::compareUnsigned);
		primeGaps(
				(gap, start, occurances)->{
					Long key=gap;
					Long value=result.get(key);
					result.put(key,
							(null==value)
									?occurances
									:(value+occurances));
				},
				progress, reader);
		return result;
	}
	
	public static NavigableMap<Long, Double> primeGapMerits(Progress progress,
			AggregatesReader reader) throws Throwable {
		NavigableMap<Long, Double> result=new TreeMap<>();
		Comparator<Double> comparator=Double::compare;
		primeGapStarts(progress, reader)
				.forEach((gap, start)->result.put(gap, gap/Math.log(start)));
		return result;
	}
	
	private static void primeGaps(PrimeGapConsumer consumer, Progress progress,
			AggregatesReader reader) throws Throwable {
		reader.consume(
				true,
				new AggregatesConsumer() {
					long lastPrime=2l;
					
					@Override
					public void consume(AggregateBlock aggregateBlock,
							Progress progress) throws Throwable {
						Aggregate aggregate=aggregateBlock.get();
						if (0!=aggregate.maxPrime) {
							long gap=aggregate.minPrime-lastPrime;
							consumer.consume(gap, lastPrime, 1l);
							lastPrime=aggregate.maxPrime;
						}
						for (Map.Entry<Long, Long> entry:
								aggregate.primeGapStarts.entrySet()) {
							Long gap=entry.getKey();
							consumer.consume(gap, entry.getValue(),
									aggregate.primeGapFrequencies.get(gap));
						}
					}
				},
				null, progress);
	}
	
	public static NavigableMap<Long, Long> primeGapStarts(Progress progress,
			AggregatesReader reader) throws Throwable {
		NavigableMap<Long, Long> result=new TreeMap<>(Long::compareUnsigned);
		primeGaps(
				(gap, start, frequency)->result.putIfAbsent(gap, start),
				progress, reader);
		return result;
	}
	
	public static Sample.Builder sieveNanos(Progress progress,
			AggregatesReader reader, boolean sum) throws Throwable {
		if (sum) {
			return sum(progress, reader, Sample.builder(),
					(aggregate)->aggregate.sieveNanos,
					0l, null);
		}
		else {
			Sample.Builder sample=Sample.builder();
			reader.consume(false,
					(aggregateBlock, progress2)->
						sample.add(
								aggregateBlock.get().segmentEnd-1l,
								aggregateBlock.get().sieveNanos),
					null,
					progress);
			return sample;
		}
	}
	
	private static Sample.Builder sum(Progress progress,
			AggregatesReader reader,
			Sample.Builder sample, Function<Aggregate, Long> selector,
			long sum, BiFunction<Long, Long, Double> transform)
			throws Throwable {
		reader.consume(
				true,
				new AggregatesConsumer() {
					private long sum2=sum;
					
					@Override
					public void consume(AggregateBlock aggregateBlock,
							Progress progress) throws Throwable {
						Aggregate aggregate=aggregateBlock.get();
						sum2+=selector.apply(aggregate);
						long lastNumber=aggregate.segmentEnd-1l;
						if (null==transform) {
							sample.add(lastNumber, sum2);
						}
						else {
							Double transformed
									=transform.apply(lastNumber, sum2);
							if (null!=transformed) {
								sample.add(lastNumber, transformed);
							}
						}
					}
				},
				null, progress);
		return sample;
	}
}
