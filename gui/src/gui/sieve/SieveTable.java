package gui.sieve;

public interface SieveTable {
	boolean isPrime(long number) throws Throwable;
	void setComposite(long number) throws Throwable;
}
