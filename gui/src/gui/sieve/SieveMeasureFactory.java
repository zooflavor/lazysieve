package gui.sieve;

import gui.io.PrimesProducer;
import gui.ui.Color;
import java.util.function.Supplier;

public interface SieveMeasureFactory {
	SieveMeasure create(Color color, Measure measure,
			PrimesProducer primes, long segments, long segmentSize,
			long startSegment, boolean sum) throws Throwable;
	
	static SieveMeasureFactory create(String label, boolean restart,
			Supplier<Sieve> sieveFactory, int smallSegmentSizeMaxLog2,
			int smallSegmentSizeMinLog2, int smallSegmentSizeSuggestedLog2) {
		return new SieveMeasureFactory() {
			@Override
			public SieveMeasure create(Color color, Measure measure,
					PrimesProducer primes, long segments, long segmentSize,
					long startSegment, boolean sum) throws Throwable {
				return restart
						?new RestartSieveMeasure(color, label, measure, primes,
								segments, segmentSize, sieveFactory,
								startSegment, sum)
						:new SegmentedSieveMeasure(color, label, measure,
								primes, segments, segmentSize, sieveFactory,
								startSegment, sum);
			}
			
			@Override
			public int smallSegmentSizeMaxLog2() {
				return smallSegmentSizeMaxLog2;
			}
			
			@Override
			public int smallSegmentSizeMinLog2() {
				return smallSegmentSizeMinLog2;
			}
			
			@Override
			public int smallSegmentSizeSuggestedLog2() {
				return smallSegmentSizeSuggestedLog2;
			}
			
			@Override
			public String toString() {
				return label;
			}
		};
	}
	
	int smallSegmentSizeMaxLog2();
	
	int smallSegmentSizeMinLog2();
	
	int smallSegmentSizeSuggestedLog2();
}
