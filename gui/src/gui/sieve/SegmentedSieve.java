package gui.sieve;

import gui.math.UnsignedLong;

public abstract class SegmentedSieve extends Sieve {
	private final long firstNumber;
	
	public SegmentedSieve(long firstNumber) {
		this.firstNumber=firstNumber;
	}
	
	protected abstract void addPrime(long end,
			OperationCounter operationCounter, long prime,
			SieveTable sieveTable, long start) throws Throwable;
	
	@Override
	protected void sieve(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
		sieveSegment(end, operationCounter, sieveTable, start);
		long start2=(0l==startSegment)?firstNumber:start;
		if (0<=Long.compareUnsigned(UnsignedLong.MAX_PRIME, start2)) {
			sieveTable.listPrimes(
					UnsignedLong.min(UnsignedLong.MAX_PRIME+1l, end),
					(prime)->{
						operationCounter.increment();
						addPrime(end, operationCounter, prime,
								sieveTable, start);
					},
					start2);
		}
	}
	
	protected abstract void sieveSegment(long end,
			OperationCounter operationCounter, SieveTable sieveTable,
			long start) throws Throwable;
}
