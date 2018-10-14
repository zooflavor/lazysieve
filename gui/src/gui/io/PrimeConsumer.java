package gui.io;

@FunctionalInterface
public interface PrimeConsumer {
	void prime(long prime) throws Throwable;
}
