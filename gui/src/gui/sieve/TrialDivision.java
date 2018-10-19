package gui.sieve;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import gui.util.IntList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TrialDivision extends AbstractSieve {
	public static List<SieveCheckFactory> CHECKS
			=Collections.unmodifiableList(Arrays.asList(
					SieveCheckFactory.create(
							"Trial division",
							TrialDivision::new,
							10)));
	public static List<SieveMeasureFactory> MEASURES
			=Collections.unmodifiableList(Arrays.asList(
					SieveMeasureFactory.create(
							"Trial division",
							false,
							TrialDivision::new,
							20, 1, 16)));
	
	private final IntList primes=new IntList(UnsignedLong.MAX_PRIME_COUNT);
	
	@Override
	public boolean defaultPrime() {
		return true;
	}
	
	@Override
	protected void reset(PrimesProducer primesProducer, Progress progress)
			throws Throwable {
		primes.clear();
		if (0l==startSegment) {
			return;
		}
		primesProducer.primes(
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
