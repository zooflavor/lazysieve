package gui.sieve;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import gui.util.IntList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TrialDivision implements Sieve {
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
	
	private long start=3l;
	private final IntList primes=new IntList(UnsignedLong.MAX_PRIME_COUNT);
	
	@Override
	public void reset() throws Throwable {
		primes.clear();
		start=3l;
	}
	
	@Override
	public void reset(PrimesProducer primesProducer, Progress progress,
			long start) throws Throwable {
		checkOdd(start);
		this.start=start;
		primes.clear();
		primesProducer.primes(
				(prime)->primes.add((int)prime),
				UnsignedLong.min(UnsignedLong.MAX_PRIME, start-1),
				progress);
	}
	
	@Override
	public void sieve(long end, OperationCounter operationCounter,
			SieveTable sieveTable) throws Throwable {
		checkOdd(end);
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
	
	@Override
	public long start() {
		return start;
	}
}
