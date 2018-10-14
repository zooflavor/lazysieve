package gui.check;

import gui.io.PrimeConsumer;
import gui.io.PrimesProducer;
import gui.io.Segment;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import java.util.List;

public class SieveProcess extends CheckProcess {
	public SieveProcess(CheckSegments parent, List<Segment.Info> segmentInfos,
			int[] selectedRows) {
		super(parent, segmentInfos, selectedRows);
	}
	
	@Override
	protected void generate(PrimesProducer primesProducer, Progress progress,
			Segment segment) throws Throwable {
		generateReference(primesProducer, progress, segment);
	}
	
	public static void generateReference(PrimesProducer primesProducer,
			Progress progress, Segment segment) throws Throwable {
		long max=UnsignedLong.squareRootFloor(segment.segmentEnd);
		PrimeConsumer sieve=(prime)->{
			long position=UnsignedLong.firstSievePosition(
					prime, segment.segmentStart);
			if (0>Long.compare(position, segment.segmentEnd)) {
				int bitIndex=segment.bitIndex(position);
				if (prime<=Segment.BITS) {
					for (; Segment.BITS>bitIndex; bitIndex+=prime) {
						segment.setComposite(bitIndex);
					}
				}
				else {
					segment.setComposite(bitIndex);
				}
			}
		};
		primesProducer.primes(sieve, max, progress);
	}
}
