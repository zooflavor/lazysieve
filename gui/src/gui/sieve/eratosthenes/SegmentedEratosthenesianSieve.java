package gui.sieve.eratosthenes;

import gui.sieve.OperationCounter;
import gui.sieve.SegmentedSieve;
import gui.sieve.SieveTable;

public abstract class SegmentedEratosthenesianSieve extends SegmentedSieve {
	public SegmentedEratosthenesianSieve(long firstNumber) {
		super(firstNumber);
	}
	
	@Override
	protected void addPrime(long end, OperationCounter operationCounter,
			long prime, SieveTable sieveTable, long start) throws Throwable {
		long position=sieve(
				end, operationCounter, prime*prime, prime, sieveTable);
		addPrime(end, operationCounter, position, prime, sieveTable, start);
	}
	
	protected abstract void addPrime(long end,
			OperationCounter operationCounter, long position, long prime,
			SieveTable sieveTable, long start) throws Throwable;
	
	@Override
	public boolean defaultPrime() {
		return true;
	}
	
	protected long sieve(long end, OperationCounter operationCounter,
			long position, long prime, SieveTable sieveTable)
			throws Throwable {
		long prime2=2l*prime;
		for(; 0<Long.compareUnsigned(end, position); position+=prime2) {
			operationCounter.increment();
			sieveTable.setComposite(position);
		}
		return position;
	}
}
