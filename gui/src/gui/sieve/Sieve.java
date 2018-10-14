package gui.sieve;

import gui.io.PrimesProducer;
import gui.ui.progress.Progress;

public interface Sieve {
	default void checkOdd(long number) {
		if (0==(number%2)) {
			throw new IllegalArgumentException(String.format(
					"even number %1$,d", number));
		}
	}
	
	void reset() throws Throwable;
	
	void reset(PrimesProducer primesProducer, Progress progress,
			long start) throws Throwable;
	
	void sieve(long end, OperationCounter operationCounter,
			SieveTable sieveTable) throws Throwable;
	
	long start();
}
