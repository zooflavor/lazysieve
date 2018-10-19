package gui.io;

import java.util.Random;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SegmentTest {
	@Test
	public void testListPrimes() throws Throwable {
		Segment segment=new Segment();
		segment.clear(0l, 0l, 0l, true, 1l);
		new Random(1234l).nextBytes(segment.segment);
		for (long start=1l; 4000l>start; start+=6) {
			for (long end=start; 4000l>end; end+=10) {
				long[] lastPrime={start-2l};
				segment.listPrimes(
						end,
						(prime)->{
							assertTrue(lastPrime[0]<prime);
							assertTrue(segment.isPrime(prime));
							lastPrime[0]+=2l;
							for (; prime>lastPrime[0]; lastPrime[0]+=2l) {
								assertFalse(segment.isPrime(lastPrime[0]));
							}
							lastPrime[0]=prime;
						},
						start);
			}
		}
	}
}
