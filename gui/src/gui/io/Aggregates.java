package gui.io;

import gui.ui.progress.Progress;
import gui.util.ConcatIterable;
import gui.util.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Function;

public class Aggregates {
	private static class AggregatesIterator implements Iterator<Aggregate> {
		private int count;
		private final DataInput input;
		private final Progress progress;
		private final int size;
		
		public AggregatesIterator(DataInput input, Progress progress)
				throws IOException {
			this.input=input;
			this.progress=progress;
			size=input.readInt();
		}
		
		@Override
		public boolean hasNext() {
			progress.checkCancelled();
			if (size>count) {
				return true;
			}
			try {
				progress.finished();
				try {
					input.readByte();
					throw new IOException("no eof");
				}
				catch (EOFException ex) {
				}
			}
			catch (Throwable throwable) {
				throw new RuntimeException(throwable);
			}
			return false;
		}
		
		@Override
		public Aggregate next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			try {
				progress.progress(null, 1.0*count/size);
				++count;
				return Aggregate.readFrom(input);
			}
			catch (Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		}
	}
	
	public final NavigableMap<Long, Aggregate> aggregates;
	public final long firstMissingStart;
	
	public Aggregates(Iterable<Aggregate> aggregates) {
		NavigableMap<Long, Aggregate> aggregates2=new TreeMap<>();
		for (Aggregate aggregate: aggregates) {
			Long start=aggregate.segmentStart;
			Aggregate aggregate2=aggregates2.get(start);
			if ((null==aggregate2)
					|| (aggregate2.lastModification
							<aggregate.lastModification)) {
				aggregates2.put(start, aggregate);
			}
		}
		this.aggregates=Collections.unmodifiableNavigableMap(aggregates2);
		long firstMissingStart2=1l;
		for (; aggregates2.containsKey(firstMissingStart2);
				firstMissingStart2+=Segment.NUMBERS) {
		}
		firstMissingStart=firstMissingStart2;
	}
	
	public Collection<Aggregate> completePrefix() {
		return aggregates.subMap(1l, true, firstMissingStart, false).values();
	}
	
	public Aggregates importAggregates(Aggregates aggregates) {
		return new Aggregates(
				new ConcatIterable<>(Arrays.asList(
						this.aggregates.values(),
						aggregates.aggregates.values())));
	}
	
	public Database.TypeInfo info() {
		return Database.TypeInfo.info(aggregates);
	}
	
	public NavigableMap<Long, Long> prime12Z11Counts() {
		return primeCounts((aggregate)->aggregate.primeCount12Z11);
	}
	
	public NavigableMap<Long, Long> prime4Z1Counts() {
		return primeCounts((aggregate)->aggregate.primeCount4Z1);
	}
	
	public NavigableMap<Long, Long> prime4Z3Counts() {
		return primeCounts((aggregate)->aggregate.primeCount4Z3);
	}
	
	public NavigableMap<Long, Long> prime6Z1Counts() {
		return primeCounts((aggregate)->aggregate.primeCount6Z1);
	}
	
	public NavigableMap<Long, Long> primeCounts() {
		NavigableMap<Long, Long> result=new TreeMap<>();
		long sum=1l;
		result.put(2l, 1l);
		for (Aggregate aggregate: completePrefix()) {
			sum+=aggregate.primeCount4Z1+aggregate.primeCount4Z3;
			result.put(aggregate.segmentEnd-1, sum);
		}
		return result;
	}
	
	private NavigableMap<Long, Long> primeCounts(
			Function<Aggregate, Long> selector) {
		NavigableMap<Long, Long> result=new TreeMap<>();
		long sum=0l;
		for (Aggregate aggregate: completePrefix()) {
			sum+=selector.apply(aggregate);
			result.put(aggregate.segmentEnd-1, sum);
		}
		return result;
	}
	
	public NavigableMap<Long, NavigableMap<Long, Long>> primeGapFrequencies() {
		NavigableMap<Long, NavigableMap<Long, Long>> result=new TreeMap<>();
		Map<Long, Long> primeGapFrequencies=new HashMap<>();
		long lastPrime=2l;
		for (Aggregate aggregate: completePrefix()) {
			aggregate.primeGapFrequencies.forEach(
					(gap, frequency)
							->Maps.add(primeGapFrequencies, gap, frequency));
			if (0!=aggregate.maxPrime) {
				long gap=aggregate.minPrime-lastPrime;
				Maps.add(primeGapFrequencies, gap, 1l);
				lastPrime=aggregate.maxPrime;
			}
			result.put(aggregate.segmentEnd-1,
					new TreeMap<>(primeGapFrequencies));
		}
		return result;
	}
	
	public NavigableMap<Long, Long> primeGapStarts() {
		Map<Long, Long> result=new HashMap<>();
		long lastPrime=2l;
		for (Aggregate aggregate: completePrefix()) {
			aggregate.primeGapStarts.forEach(
					(gap, start)->Maps.min(result, gap, start));
			if (0!=aggregate.maxPrime) {
				long gap=aggregate.minPrime-lastPrime;
				Maps.min(result, gap, lastPrime);
				lastPrime=aggregate.maxPrime;
			}
		}
		return new TreeMap<>(result);
	}
	
	public static Aggregates readFrom(DataInput input, Progress progress)
			throws IOException {
		progress.checkCancelled();
		AggregatesIterator iterator=new AggregatesIterator(input, progress);
		return new Aggregates(()->iterator);
	}
	
	public NavigableMap<Long, Long> sieveNanos() {
		NavigableMap<Long, Long> result=new TreeMap<>();
		long sum=0l;
		for (Aggregate aggregate: completePrefix()) {
			sum+=aggregate.sieveNanos;
			result.put(aggregate.segmentEnd-1l, sum);
		}
		return result;
	}
	
	public void writeTo(DataOutput output, Progress progress)
			throws Throwable {
		output.writeInt(aggregates.size());
		int ii=0;
		for (Aggregate aggregate: aggregates.values()) {
			progress.progress(null, 1.0*ii/aggregates.size());
			aggregate.writeTo(output);
			++ii;
		}
		progress.finished();
	}
}
