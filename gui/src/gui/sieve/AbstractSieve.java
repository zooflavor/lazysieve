package gui.sieve;

import gui.io.PrimesProducer;
import gui.ui.progress.Progress;

public abstract class AbstractSieve implements Sieve {
	protected long segmentSize;
	protected long startSegment;
	
	@Override
	public void reset(PrimesProducer primesProducer, Progress progress,
			long segmentSize, long start) throws Throwable {
		if (64l>segmentSize) {
			throw new IllegalArgumentException(""+segmentSize);
		}
		if (Long.highestOneBit(segmentSize)!=segmentSize) {
			throw new IllegalArgumentException(""+segmentSize);
		}
		this.segmentSize=segmentSize;
		startSegment=Long.divideUnsigned(start-1l, segmentSize);
		reset(primesProducer, progress);
	}
	
	protected abstract void reset(PrimesProducer primesProducer,
			Progress progress) throws Throwable;

	@Override
	public void sieve(OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable {
		long start=segmentSize*startSegment+1l;
		long end=start+segmentSize;
		sieve(end, operationCounter, sieveTable, start);
		++startSegment;
	}
	
	protected abstract void sieve(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable;

	@Override
	public long start() {
		return segmentSize*startSegment+1l;
	}
}
