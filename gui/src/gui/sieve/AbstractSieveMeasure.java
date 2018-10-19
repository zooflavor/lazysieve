package gui.sieve;

import gui.io.PrimesProducer;
import gui.io.Segment;
import gui.math.UnsignedLong;
import gui.ui.Color;
import java.util.function.Supplier;

public abstract class AbstractSieveMeasure implements SieveMeasure {
	protected final Color color;
	protected final String label;
	protected final Measure measure;
	protected final PrimesProducer primes;
	protected final long segments;
	protected final long segmentSize;
	protected final Supplier<Sieve> sieveFactory;
	protected final long startSegment;
	protected final boolean sum;
	
	public AbstractSieveMeasure(Color color, String label, Measure measure,
			PrimesProducer primes, long segments, long segmentSize,
			Supplier<Sieve> sieveFactory, long startSegment, boolean sum) {
		if (0!=(segmentSize&1l)) {
			throw new IllegalArgumentException(
					UnsignedLong.format(segmentSize));
		}
		if (0!=Long.remainderUnsigned(Segment.NUMBERS, segmentSize)) {
			throw new IllegalArgumentException(
					UnsignedLong.format(segmentSize));
		}
		long allSegments=1l<<(64-Long.numberOfTrailingZeros(segmentSize));
		if (0>=segments) {
			throw new IllegalArgumentException(UnsignedLong.format(segments));
		}
		if (0>startSegment) {
			throw new IllegalArgumentException(
					UnsignedLong.format(startSegment));
		}
		if (0>Long.compareUnsigned(allSegments, startSegment+segments)) {
			throw new IllegalArgumentException(String.format(
					"%1$s<%2$s+%3$s",
					UnsignedLong.format(allSegments),
					UnsignedLong.format(startSegment),
					UnsignedLong.format(segments)));
		}
		this.color=color;
		this.label=label
				+"-"+measure
				+"-2^"+(63-Long.numberOfLeadingZeros(segmentSize))
				+"x("+startSegment
				+"+"+segments+")";
		this.measure=measure;
		this.primes=primes;
		this.segments=segments;
		this.segmentSize=segmentSize;
		this.sieveFactory=sieveFactory;
		this.startSegment=startSegment;
		this.sum=sum;
	}
}
