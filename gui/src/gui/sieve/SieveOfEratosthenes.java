package gui.sieve;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import gui.util.IntList;
import gui.util.LongList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SieveOfEratosthenes extends SegmentedSieve {
	public static List<SieveCheckFactory> CHECKS
			=Collections.unmodifiableList(Arrays.asList(
					SieveCheckFactory.create(
							"Sieve of Eratosthenes",
							SieveOfEratosthenes::new,
							24)));
	public static List<SieveMeasureFactory> MEASURES
			=Collections.unmodifiableList(Arrays.asList(
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes",
							true,
							SieveOfEratosthenes::new,
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-segmented",
							false,
							SieveOfEratosthenes::new,
							30, 1, 20)));
	
	private final LongList positions
			=new LongList(UnsignedLong.MAX_PRIME_COUNT);
	private final IntList primes=new IntList(UnsignedLong.MAX_PRIME_COUNT);
	private int sqrtIndex;
	
	@Override
	protected void addPrime(long position, int prime) {
		positions.add(position);
		primes.add(prime);
	}
	
	@Override
	public void reset() throws Throwable {
		positions.clear();
		primes.clear();
		sqrtIndex=0;
		start=3l;
	}
	
	@Override
	public void reset(PrimesProducer primesProducer, Progress progress,
			long start) throws Throwable {
		checkOdd(start);
		this.start=start;
		positions.clear();
		sqrtIndex=0;
		primes.clear();
		primesProducer.primes(
				(prime)->{
					positions.add(
							UnsignedLong.firstSievePosition(prime, start));
					primes.add((int)prime);
				},
				UnsignedLong.min(UnsignedLong.MAX_PRIME, start-1),
				progress);
	}
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable) throws Throwable {
		while ((primes.size()>sqrtIndex)
				&& (0<=Long.compareUnsigned(end,
						UnsignedLong.square(primes.get(sqrtIndex))))) {
			++sqrtIndex;
		}
		for (int ii=0; sqrtIndex>ii; ++ii) {
			operationCounter.increment();
			long position=positions.get(ii);
			if (0<Long.compareUnsigned(end, position)) {
				long prime=2l*UnsignedLong.unsignedInt(primes.get(ii));
				for(; 0<Long.compareUnsigned(end, position); position+=prime) {
					operationCounter.increment();
					sieveTable.setComposite(position);
				}
				positions.set(ii, position);
			}
		}
	}
	
	@Override
	public long start() {
		return start;
	}
}
