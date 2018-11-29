package gui.io;

import gui.ui.progress.Progress;

@FunctionalInterface
public interface PrimeProducer {
	void primes(PrimeConsumer consumer, long max, Progress progress)
			throws Throwable;
}
