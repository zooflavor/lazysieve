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
		
		public void clear(boolean prime) {
			Arrays.fill(bits, prime?0l:1l);
		}
		
		@Override
		public void flip(long number) throws Throwable {
			number=(number-3l)>>>1;
			bits[(int)(number>>>6)]^=1l<<(number&0x3fl);
		}
		
		@Override
		public boolean isPrime(long number) throws Throwable {
			number=(number-3l)>>>1;
			return 0l==(bits[(int)(number>>>6)]&(1l<<(number&0x3fl)));
		}
		
		@Override
		public void listPrimes(long end, PrimeConsumer primeConsumer,
				long start) throws Throwable {
			if (0l==(end&1l)) {
				--end;
			}
			if (0l==(start&1l)) {
				++start;
			}
			int ei=(int)(end>>>1);
			int si=(int)(start>>>1);
			for (; (ei>si) && (0!=(si&0x3f)); ++si) {
				if (0l==(bits[si>>>6]&(1l<<(si&0x3fl)))) {
					primeConsumer.prime(2l*si+1l);
				}
			}
			for (; ei>si; si+=64) {
				long bb=~bits[si>>>6];
				int bi=si;
				while (0!=bb) {
					int nz=Long.numberOfTrailingZeros(bb);
					bi+=nz;
					bb>>>=nz;
					primeConsumer.prime(2l*bi+1);
					++bi;
					bb>>>=1;
				}
			}
			for (; ei>si; ++si) {
				if (0l==(bits[si>>>6]&(1l<<(si&0x3fl)))) {
					primeConsumer.prime(2l*si+1l);
				}
			}
		}
		
		@Override
		public void setComposite(long number) throws Throwable {
			number=(number-3l)>>>1;
			bits[(int)(number>>>6)]|=1l<<(number&0x3fl);
		}
		
		@Override
		public void setPrime(long number) throws Throwable {
			number=(number-3l)>>>1;
			bits[(int)(number>>>6)]&=~(1l<<(number&0x3fl));
		}
	}
	
	private final long tableSize;
	
	public RestartSieveMeasure(Color color, String label, Measure measure,
			PrimesProducer primes, long segments, long segmentSize,
			Supplier<Sieve> sieveFactory, long startSegment, boolean sum) {
		super(color, label, measure, primes, segments, segmentSize,
				sieveFactory, startSegment, sum);
		tableSize=segmentSize*segments/2;
		if (0>Long.compareUnsigned(1l<<32, tableSize)) {
			throw new IllegalArgumentException("sieve table is too large");
		}
	}
	
	@Override
	public Sample2D measure(Progress progress) throws Throwable {
		progress.progress(0.0);
		Sieve sieve=sieveFactory.get();
		sieve.reset(
				primes,
				progress.subProgress(0.0, null, 0.0),
				segmentSize,
				1l);
		Map<Double, Double> sample=new HashMap<>((int)segments);
		boolean time=Measure.NANOSECS.equals(measure);
		OperationCounter counter
				=time?OperationCounter.NOOP:OperationCounter.COUNTER;
		Table table=new Table(tableSize);
		long lastMeasure=0l;
		for (long ss=0; segments>ss; ++ss) {
			progress.progress(1.0*ss/segments);
			long start=(startSegment+ss)*segmentSize+1l;
			long end=start+segmentSize;
			counter.reset();
			sieve.reset(
					primes,
					progress.subProgress(
							1.0*ss/segments, null, 1.0*ss/segments),
					segmentSize,
					1l);
			table.clear(sieve.defaultPrime());
			long startTime=System.nanoTime();
			sieve.sieve(counter, table);
			long endTime=System.nanoTime();
			long measure2;
			if (time) {
				measure2=endTime-startTime;
			}
			else {
				measure2=counter.get();
			}
			if (!sum) {
				long measure3=lastMeasure;
				lastMeasure=measure2;
				measure2-=measure3;
			}
			sample.put(1.0*end, 1.0*measure2);
		}
		progress.finished();
		return new Sample2D(new Object(), label, Colors.INTERPOLATION,
				color, sample, color);
	}
}
