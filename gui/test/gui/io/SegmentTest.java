package gui.io;

import gui.util.LongIterator;
import java.util.NoSuchElementException;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class SegmentTest {
	@Test
	public void testPrimeIterator() throws Throwable {
		Segment segment=new Segment();
		segment.clear(0l, 0l, 0l, 1l);
		new Random(1234l).nextBytes(segment.segment);
		segment.segment[13]=0;
		LongIterator iterator=segment.iteratePrimes();
		for (int ii=0; Segment.BITS>ii; ++ii) {
			if (segment.isPrime(ii)) {
				assertTrue(iterator.hasNext());
				assertEquals(segment.number(ii), iterator.next());
			}
		}
		assertFalse(iterator.hasNext());
		try {
			iterator.next();
			fail();
		}
		catch (NoSuchElementException ex) {
		}
		assertFalse(iterator.hasNext());
	}
}
