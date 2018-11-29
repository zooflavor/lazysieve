package gui.sieve;

import gui.io.PrimeProducer;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import gui.util.IntList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TrialDivision extends Sieve {
	public static List<Sieve.Descriptor> SIEVES
			=Collections.unmodifiableList(Arrays.asList(
					new Sieve.Descriptor(
							TrialDivision::new,
							"Próbaosztás",
							"trial-division",
							20, 1, 16)));
	
	private final IntList primes=new IntList(UnsignedLong.MAX_PRIME_COUNT);
	
	@Override
	public boolean clearBitsToPrime() {
		return true;
	}
	
	@Override
	protected void reset(PrimeProducer primeProducer, Progress progress)
			throws Throwable {
		primes.clear();
		if (0l==startSegment) {
			return;
		}
		primeProducer.primes(
				(prime)->primes.add((int)prime),
				UnsignedLong.min(UnsignedLong.MAX_PRIME,
						segmentSize*startSegment),
				progress);
	}
	
	@Override
	protected void sieve(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
		if (0l==startSegment) {
			start=3l;
		}
		for (; 0<Long.compareUnsigned(end, start); start+=2) {
			boolean prime=true;
			for (int ii=0; primes.size()>ii; ++ii) {
				operationCounter.increment();
				long prime2=UnsignedLong.unsignedInt(primes.get(ii));
				if (0<Long.compareUnsigned(prime2*prime2, start)) {
					break;
				}
				if (0l==Long.remainderUnsigned(start, prime2)) {
					prime=false;
					break;
				}
			}
			if (prime) {
				primes.add((int)start);
			}
			else {
				sieveTable.setComposite(start);
			}
		}
	}
}
