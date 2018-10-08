package gui.check;

import gui.io.Segment;
import gui.math.UnsignedLongMath;
import gui.ui.progress.Progress;
import gui.util.LongList;
import java.util.List;

public class SieveProcess extends CheckProcess {
	public SieveProcess(CheckSegments parent, List<Segment.Info> segmentInfos,
			int[] selectedRows) {
		super(parent, segmentInfos, selectedRows);
	}
	
	@Override
	protected void generate(CheckProcess.Primes primes, Progress progress,
			Segment segment) throws Throwable {
		LongList primes2=primes.get(
				progress.subProgress(0.0, "read primes", 0.05));
		Progress subProgress=progress.subProgress(0.05, "sieve", 1.0);
		for (int ii=0; primes2.size()>ii; ++ii) {
			subProgress.progress(1.0*ii/primes2.size());
			long prime=primes2.get(ii);
			if (0<=Long.compareUnsigned(prime*prime, segment.segmentEnd)) {
				break;
			}
			long first=UnsignedLongMath.square(prime);
			if (0>Long.compareUnsigned(first, segment.segmentStart)) {
				first=prime
						*Long.divideUnsigned(segment.segmentStart, prime);
				if (0>Long.compareUnsigned(first, segment.segmentStart)) {
					first+=prime;
				}
				if (0l==(first&1l)) {
					first+=prime;
				}
			}
			if (0>Long.compare(first, segment.segmentEnd)) {
				for (int bitIndex=segment.bitIndex(first);
						Segment.BITS>bitIndex;
						bitIndex+=prime) {
					segment.setNotPrime(bitIndex);
				}
			}
		}
	}
}
