package gui.sieve.eratosthenes;

import gui.io.PrimeProducer;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.Sieve;
import gui.sieve.SieveTable;
import gui.ui.progress.Progress;
import gui.util.IntList;
import gui.util.LongList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SieveOfEratosthenes extends EratosthenesianSieve {
	public static final List<Sieve.Descriptor> SIEVES
			=Collections.unmodifiableList(Arrays.asList(
					new Sieve.Descriptor(
							SieveOfEratosthenes::new,
							"Eratoszthenész szitája-szegmentált",
							"eratosthenes-segmented",
							30, 6, 20)));
	
	private final LongList positions
			=new LongList(UnsignedLong.MAX_PRIME_COUNT);
	private final IntList primes=new IntList(UnsignedLong.MAX_PRIME_COUNT);
	private int sqrtIndex;
	
	public SieveOfEratosthenes() {
		super(3l);
	}
	
	@Override
	protected void addPrime(long end, OperationCounter operationCounter,
			long position, long prime, SieveTable sieveTable, long start)
			throws Throwable {
		positions.add(position);
		primes.add((int)prime);
	}
	
	@Override
	protected void reset(PrimeProducer primeProducer, Progress progress)
			throws Throwable {
		positions.clear();
		primes.clear();
		sqrtIndex=0;
		if (0l==startSegment) {
			return;
		}
		primeProducer.primes(
				(prime)->{
					positions.add(
							UnsignedLong.firstSievePosition(prime, start()));
					primes.add((int)prime);
				},
				UnsignedLong.min(UnsignedLong.MAX_PRIME,
						segmentSize*startSegment),
				progress);
	}
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
		while ((primes.size()>sqrtIndex)
				&& (0<=Long.compareUnsigned(end,
						UnsignedLong.square(UnsignedLong.unsignedInt(
								primes.get(sqrtIndex)))))) {
			++sqrtIndex;
		}
		for (int ii=0; sqrtIndex>ii; ++ii) {
			operationCounter.increment();
			long position=positions.get(ii);
			if (0<Long.compareUnsigned(end, position)) {
				position=sieve(end, operationCounter, position,
						UnsignedLong.unsignedInt(primes.get(ii)), sieveTable);
				positions.set(ii, position);
			}
		}
	}
}
