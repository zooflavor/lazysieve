package gui.sieve;

import gui.graph.Sample2D;
import gui.io.PrimesProducer;
import gui.plotter.Colors;
import gui.ui.Color;
import gui.ui.progress.Progress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RestartSieveMeasure extends AbstractSieveMeasure {
	private static class Table implements SieveTable {
		private final long[] bits;
		
		public Table(long size) {
			bits=new long[(int)(size>>6)];
		}
		
		public void clear() {
			Arrays.fill(bits, 0l);
		}
		
		@Override
		public boolean isPrime(long number) throws Throwable {
			number=(number-3l)>>>1;
			return 0l==(bits[(int)(number>>>6)]&(1l<<(number&0x3fl)));
		}
		
		@Override
		public void setComposite(long number) throws Throwable {
			number=(number-3l)>>>1;
			bits[(int)(number>>>6)]|=1l<<(number&0x3fl);
		}
	}
	
	private final long tableSize;
	
	public RestartSieveMeasure(Color color, String label, Measure measure,
			PrimesProducer primes, long segments, long segmentSize,
			Supplier<Sieve> sieveFactory, long startSegment) {
		super(color, label, measure, primes, segments, segmentSize,
				sieveFactory, startSegment);
		tableSize=segmentSize*segments/2;
		if (0>Long.compareUnsigned(1l<<32, tableSize)) {
			throw new IllegalArgumentException("sieve table is too large");
		}
	}
	
	@Override
	public Sample2D measure(Progress progress) throws Throwable {
		progress.progress(0.0);
		Sieve sieve=sieveFactory.get();
		sieve.reset();
		Map<Double, Double> sample=new HashMap<>((int)segments);
		boolean time=Measure.NANOSECS.equals(measure);
		OperationCounter counter
				=time?OperationCounter.NOOP:OperationCounter.COUNTER;
		Table table=new Table(tableSize);
		for (long ss=0; segments>ss; ++ss) {
			progress.progress(1.0*ss/segments);
			long start=(startSegment+ss)*segmentSize+1l;
			long end=start+segmentSize;
			counter.reset();
			sieve.reset();
			table.clear();
			long startTime=System.nanoTime();
			sieve.sieve(end, counter, table);
			long endTime=System.nanoTime();
			long sieveTime=endTime-startTime;
			sample.put(1.0*end, 1.0*(time?sieveTime:(counter.get())));
		}
		progress.finished();
		return new Sample2D(new Object(), label, Colors.INTERPOLATION,
				color, sample, color);
	}
}
