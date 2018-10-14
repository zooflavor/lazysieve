package gui.io;

import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import gui.util.IntList;
import java.lang.ref.SoftReference;

public abstract class PrimesCache implements PrimesProducer {
	private SoftReference<IntList> cache;
	protected final long max;
	
	public PrimesCache(long max) {
		this.max=max;
	}

	@Override
	public void primes(PrimeConsumer consumer, long max, Progress progress)
			throws Throwable {
		max=UnsignedLong.min(max, UnsignedLong.MAX_PRIME);
		if (3l>max) {
			return;
		}
		if (this.max<max) {
			throw new IllegalStateException();
		}
		IntList primes=(null==cache)?null:(cache.get());
		if (null==primes) {
			primes=primes(progress.subProgress(0.0, null, 0.05));
			cache=new SoftReference<>(primes);
		}
		Progress subProgress=progress.subProgress(0.05, null, 1.0);
		for (int ii=0; primes.size()>ii; ++ii) {
			long prime=UnsignedLong.unsignedInt(primes.get(ii));
			if (max<prime) {
				break;
			}
			subProgress.progress(1.0*prime/max);
			consumer.prime(prime);
		}
		progress.finished();
	}
	
	protected abstract IntList primes(Progress progress) throws Throwable;
}
