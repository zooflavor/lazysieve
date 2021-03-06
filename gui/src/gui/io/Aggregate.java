package gui.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Aggregate {
	public final long aggregateNanos;
	public final long initNanos;
	public final long lastModification;
	public final long maxPrime;
	public final long minPrime;
	public final long primeCount12Z11;
	public final long primeCount4Z1;
	public final long primeCount4Z3;
	public final long primeCount6Z1;
	//prime gap->frequency
	public final NavigableMap<Long, Long> primeGapFrequencies;
	//prime gap->start of first occurance
	public final NavigableMap<Long, Long> primeGapStarts;
	public final long segment;
	public final long segmentEnd;
	public final long segmentStart;
	public final long sieveNanos;
	
	public Aggregate(long aggregateNanos, long initNanos,
			long lastModification, long maxPrime, long minPrime,
			long primeCount12Z11, long primeCount4Z1, long primeCount4Z3,
			long primeCount6Z1, Map<Long, Long> primeGapFrequencies,
			Map<Long, Long> primeGapStarts, long segment, long segmentEnd,
			long segmentStart, long sieveNanos) {
		if ((primeGapFrequencies.size()!=primeGapStarts.size())
				|| (!primeGapFrequencies.keySet().containsAll(
						primeGapStarts.keySet()))
				|| (!primeGapStarts.keySet().containsAll(
						primeGapFrequencies.keySet()))) {
			throw new IllegalArgumentException();
		}
		this.aggregateNanos=aggregateNanos;
		this.initNanos=initNanos;
		this.lastModification=lastModification;
		this.maxPrime=maxPrime;
		this.primeGapStarts=Collections.unmodifiableNavigableMap(
				new TreeMap<>(primeGapStarts));
		this.minPrime=minPrime;
		this.primeCount12Z11=primeCount12Z11;
		this.primeCount4Z1=primeCount4Z1;
		this.primeCount4Z3=primeCount4Z3;
		this.primeCount6Z1=primeCount6Z1;
		this.primeGapFrequencies=Collections.unmodifiableNavigableMap(
				new TreeMap<>(primeGapFrequencies));
		this.segment=segment;
		this.segmentEnd=segmentEnd;
		this.segmentStart=segmentStart;
		this.sieveNanos=sieveNanos;
	}
	
	public static Aggregate readFrom(DataInput input, int version)
			throws IOException {
		long aggregateNanos=input.readLong();
		long initNanos=input.readLong();
		long lastModification=input.readLong();
		long maxPrime=input.readLong();
		long minPrime=input.readLong();
		long primeCount12Z11=input.readLong();
		long primeCount4Z1=input.readLong();
		long primeCount4Z3=input.readLong();
		long primeCount6Z1=input.readLong();
		Map<Long, Long> primeGapFrequencies;
		Map<Long, Long> primeGapStarts;
		switch (version) {
			case 0:
				int primeGapFrequenciesSize=input.readInt();
				primeGapFrequencies=new HashMap<>(primeGapFrequenciesSize);
				for (int ii=primeGapFrequenciesSize; 0<ii; --ii) {
					long gap=input.readLong();
					long frequency=input.readLong();
					primeGapFrequencies.put(gap, frequency);
				}
				int primeGapStartsSize=input.readInt();
				primeGapStarts=new HashMap<>(primeGapStartsSize);
				for (int ii=primeGapStartsSize; 0<ii; --ii) {
					long gap=input.readLong();
					long start=input.readLong();
					primeGapStarts.put(gap, start);
				}
				break;
			case 1:
				int primeGapSize=input.readInt();
				primeGapFrequencies=new HashMap<>(primeGapSize);
				primeGapStarts=new HashMap<>(primeGapSize);
				for (int ii=primeGapSize; 0<ii; --ii) {
					Long gap=input.readLong();
					Long frequency=input.readLong();
					Long start=input.readLong();
					primeGapFrequencies.put(gap, frequency);
					primeGapStarts.put(gap, start);
				}
				break;
			default:
				throw new IllegalArgumentException(Integer.toString(version));
		}
		long segment=input.readLong();
		long segmentEnd=input.readLong();
		long segmentStart=input.readLong();
		long sieveNanos=input.readLong();
		return new Aggregate(aggregateNanos, initNanos, lastModification,
				maxPrime, minPrime, primeCount12Z11, primeCount4Z1,
				primeCount4Z3, primeCount6Z1, primeGapFrequencies,
				primeGapStarts, segment, segmentEnd, segmentStart, sieveNanos);
	}
	
	public void writeTo(DataOutput output) throws IOException {
		output.writeLong(aggregateNanos);
		output.writeLong(initNanos);
		output.writeLong(lastModification);
		output.writeLong(maxPrime);
		output.writeLong(minPrime);
		output.writeLong(primeCount12Z11);
		output.writeLong(primeCount4Z1);
		output.writeLong(primeCount4Z3);
		output.writeLong(primeCount6Z1);
		output.writeInt(primeGapFrequencies.size());
		for (Map.Entry<Long, Long> entry: primeGapFrequencies.entrySet()) {
			Long gap=entry.getKey();
			output.writeLong(gap);
			output.writeLong(entry.getValue());
			output.writeLong(primeGapStarts.get(gap));
		}
		output.writeLong(segment);
		output.writeLong(segmentEnd);
		output.writeLong(segmentStart);
		output.writeLong(sieveNanos);
	}
}
