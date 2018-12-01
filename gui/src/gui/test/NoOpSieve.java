package gui.test;

import gui.io.PrimeProducer;
import gui.sieve.OperationCounter;
import gui.sieve.Sieve;
import gui.sieve.SieveTable;
import gui.ui.progress.Progress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NoOpSieve extends Sieve {
	public static List<Sieve.Descriptor> SIEVES
			=Collections.unmodifiableList(Arrays.asList(
					new Sieve.Descriptor(
							NoOpSieve::new,
							"Noop",
							"no-op",
							20, 1, 16)));
	
	@Override
	public boolean clearBitsToPrime() {
		return false;
	}
	
	@Override
	protected void reset(PrimeProducer primeProducer, Progress progress)
			throws Throwable {
	}
	
	@Override
	protected void sieve(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
	}
}
