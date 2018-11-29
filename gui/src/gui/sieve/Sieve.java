package gui.sieve;

import gui.io.PrimeProducer;
import gui.ui.progress.Progress;
import gui.util.Supplier;

public abstract class Sieve {
	public static class Descriptor {
		public final Supplier<Sieve> factory;
		public final String longName;
		public final String shortName;
		public final int smallSegmentSizeMaxLog2;
		public final int smallSegmentSizeMinLog2;
		public final int smallSegmentSizeSuggestedLog2;
		
		public Descriptor(Supplier<Sieve> factory, String longName,
				String shortName, int smallSegmentSizeMaxLog2,
				int smallSegmentSizeMinLog2,
				int smallSegmentSizeSuggestedLog2) {
			this.factory=factory;
			this.longName=longName;
			this.shortName=shortName;
			this.smallSegmentSizeMaxLog2=smallSegmentSizeMaxLog2;
			this.smallSegmentSizeMinLog2=smallSegmentSizeMinLog2;
			this.smallSegmentSizeSuggestedLog2=smallSegmentSizeSuggestedLog2;
		}
		
		@Override
		public String toString() {
			return longName;
		}
	}
	
	protected long segmentSize;
	protected int segmentSizeLog2;
	protected long startSegment;
	
	public abstract boolean clearBitsToPrime();
	
	public void reset(PrimeProducer primeProducer, Progress progress,
			long segmentSize, long start) throws Throwable {
		if (64l>segmentSize) {
			throw new IllegalArgumentException(""+segmentSize);
		}
		if (Long.highestOneBit(segmentSize)!=segmentSize) {
			throw new IllegalArgumentException(""+segmentSize);
		}
		this.segmentSize=segmentSize;
		segmentSizeLog2=Long.numberOfTrailingZeros(segmentSize);
		startSegment=Long.divideUnsigned(start-1l, segmentSize);
		reset(primeProducer, progress);
	}
	
	protected abstract void reset(PrimeProducer primeProducer,
			Progress progress) throws Throwable;
	
	public void sieve(OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable {
		long start=segmentSize*startSegment+1l;
		long end=start+segmentSize;
		sieve(end, operationCounter, sieveTable, start);
		++startSegment;
	}
	
	protected abstract void sieve(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable;

	public long start() {
		return segmentSize*startSegment+1l;
	}
}
