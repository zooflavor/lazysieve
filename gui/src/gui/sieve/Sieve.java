package gui.sieve;

import gui.io.PrimesProducer;
import gui.ui.progress.Progress;
import gui.util.Supplier;

public interface Sieve {
	class Descriptor {
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
	
	boolean defaultPrime();
	
	void reset(PrimesProducer primesProducer, Progress progress,
			long segmentSize, long start) throws Throwable;
	
	void sieve(OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable;
	
	long start();
}
