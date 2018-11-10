package gui.sieve;

public interface SieveTable {
	@FunctionalInterface
	interface PrimeConsumer {
		void prime(long prime) throws Throwable;
	}
	
	void clear(boolean prime);
	void flip(long number) throws Throwable;
	boolean isPrime(long number) throws Throwable;
	void listPrimes(long end, PrimeConsumer primeConsumer, long start)
			throws Throwable;
	void setComposite(long number) throws Throwable;
	void setPrime(long number) throws Throwable;
}
