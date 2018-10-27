package gui.sieve;

import gui.graph.PlotType;
import gui.graph.Sample;
import gui.io.PrimesProducer;
import gui.io.Segment;
import gui.plotter.Colors;
import gui.ui.Color;
import gui.ui.progress.Progress;
import java.util.function.Supplier;

public class SegmentedSieveMeasure extends AbstractSieveMeasure {
	public SegmentedSieveMeasure(Color color, String label, Measure measure,
			PrimesProducer primes, long segments, long segmentSize,
			Supplier<Sieve> sieveFactory, long startSegment, boolean sum) {
		super(color, label, measure, primes, segments, segmentSize,
				sieveFactory, startSegment, sum);
	}
	
	@Override
	public Sample measure(Progress progress) throws Throwable {
		progress.progress(0.0);
		Sieve sieve=sieveFactory.get();
		sieve.reset(
				primes,
				progress.subProgress(0.0, "init", 0.05),
				segmentSize,
				startSegment*segmentSize+1l);
		Segment segment=new Segment();
		segment.clear(0l, 0l, 0l, sieve.defaultPrime(), 1l);
		Sample.Builder sample=Sample.builder((int)segments);
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
				segment.clear(0l, 0l, 0l, sieve.defaultPrime(),
						Long.divideUnsigned(start-1l, Segment.NUMBERS)
								*Segment.NUMBERS+1l);
			}
			long startTime=System.nanoTime();
			sieve.sieve(counter, segment);
			long endTime=System.nanoTime();
			long measure2;
			if (time) {
				if (sum) {
					sieveTime+=endTime-startTime;
					measure2=sieveTime;
				}
				else {
					measure2=endTime-startTime;
				}
			}
			else {
				measure2=counter.get();
				if (!sum) {
					counter.reset();
				}
			}
			sample.add(end, measure2);
		}
		subProgress.finished();
		return sample.create(new Object(), label, Colors.INTERPOLATION,
				PlotType.LINE, color, color);
	}
}
