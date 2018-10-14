package gui.sieve;

import gui.graph.Sample2D;
import gui.io.PrimesProducer;
import gui.io.Segment;
import gui.plotter.Colors;
import gui.ui.Color;
import gui.ui.progress.Progress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SegmentedSieveMeasure extends AbstractSieveMeasure {
	public SegmentedSieveMeasure(Color color, String label, Measure measure,
			PrimesProducer primes, long segments, long segmentSize,
			Supplier<Sieve> sieveFactory, long startSegment) {
		super(color, label, measure, primes, segments, segmentSize,
				sieveFactory, startSegment);
	}
	
	@Override
	public Sample2D measure(Progress progress) throws Throwable {
		progress.progress(0.0);
		Sieve sieve=sieveFactory.get();
		sieve.reset(primes,
				progress.subProgress(0.0, "init", 0.05),
				(0l==startSegment)?3l:(startSegment*segmentSize+1l));
		Segment segment=new Segment();
		segment.clear(0l, 0l, 0l, 1l);
		Map<Double, Double> sample=new HashMap<>((int)segments);
		boolean time=Measure.NANOSECS.equals(measure);
		OperationCounter counter
				=time?OperationCounter.NOOP:OperationCounter.COUNTER;
		counter.reset();
		long sieveTime=0l;
		Progress subProgress=progress.subProgress(0.05, "sieve", 1.0);
		for (long ss=0; segments>ss; ++ss) {
			subProgress.progress(1.0*ss/segments);
			long start=(startSegment+ss)*segmentSize+1l;
			long end=start+segmentSize;
			if (0<=Long.compareUnsigned(start, segment.segmentEnd)) {
				segment.clear(0l, 0l, 0l,
						Long.divideUnsigned(start-1l, Segment.NUMBERS)
								*Segment.NUMBERS+1l);
			}
			long startTime=System.nanoTime();
			sieve.sieve(end, counter, segment);
			long endTime=System.nanoTime();
			sieveTime+=endTime-startTime;
			sample.put(1.0*end, 1.0*(time?sieveTime:(counter.get())));
		}
		subProgress.finished();
		return new Sample2D(new Object(), label, Colors.INTERPOLATION,
				color, sample, color);
	}
}
