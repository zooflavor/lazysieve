package gui.sieve;

import gui.math.UnsignedLong;

public abstract class SegmentedSieve implements Sieve {
	protected long start=3l;
	
	protected abstract void addPrime(long position, int prime);
	
	@Override
	public void sieve(long end, OperationCounter operationCounter,
			SieveTable sieveTable) throws Throwable {
		checkOdd(end);
		sieveSegment(end, operationCounter, sieveTable);
		if (0<=Long.compareUnsigned(UnsignedLong.MAX_PRIME, start)) {
			for (; 0<Long.compareUnsigned(end, start); start+=2) {
				if (sieveTable.isPrime(start)) {
					operationCounter.increment();
					long prime1=start;
					if (0>Long.compareUnsigned(
							UnsignedLong.MAX_PRIME, prime1)) {
						break;
					}
					long position=prime1*prime1;
					long prime2=2l*prime1;
					for(; 0<Long.compareUnsigned(end, position);
							position+=prime2) {
						operationCounter.increment();
						sieveTable.setComposite(position);
					}
					addPrime(position, (int)prime1);
				}
			}
		}
		start=end;
	}
	
	protected abstract void sieveSegment(long end,
			OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable;
	
	@Override
	public long start() {
		return start;
	}
}
