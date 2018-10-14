package util;

import gui.util.UnsignedLongMinQueue;
import java.util.NoSuchElementException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class UnsignedLongMinQueueTest {
	@Test
	public void test() throws Throwable {
		UnsignedLongMinQueue queue=new UnsignedLongMinQueue(2);
		assertTrue(queue.isEmpty());
		assertEquals(0, queue.size());
		assertEquals("[]", queue.toString());
		try {
			queue.peek();
			fail();
		}
		catch (NoSuchElementException ex) {
		}
		
		queue.add(5l);
		assertFalse(queue.isEmpty());
		assertEquals(1, queue.size());
		assertEquals("[5]", queue.toString());
		assertEquals(5l, queue.peek());
		
		queue.add(7l);
		assertEquals(2, queue.size());
		assertEquals("[5, 7]", queue.toString());
		assertEquals(5l, queue.peek());
		
		queue.add(3l);
		assertEquals(3, queue.size());
		assertEquals("[3, 7, 5]", queue.toString());
		assertEquals(3l, queue.peek());
		
		assertEquals(3l, queue.replace(3l));
		assertEquals(3, queue.size());
		assertEquals("[3, 7, 5]", queue.toString());
		
		assertEquals(3l, queue.replace(4l));
		assertEquals("[4, 7, 5]", queue.toString());
		
		assertEquals(4l, queue.replace(2l));
		assertEquals("[2, 7, 5]", queue.toString());
		
		assertEquals(2l, queue.replace(9l));
		assertEquals("[5, 7, 9]", queue.toString());
		
		assertEquals(5l, queue.replace(8l));
		assertEquals("[7, 8, 9]", queue.toString());
		
		assertEquals(7l, queue.replace(13l));
		assertEquals("[8, 13, 9]", queue.toString());
		
		assertEquals(8l, queue.replace(11l));
		assertEquals("[9, 13, 11]", queue.toString());
		
		assertEquals(9l, queue.remove());
		assertEquals(2, queue.size());
		assertEquals("[11, 13]", queue.toString());
		
		assertEquals(11l, queue.replace(12l));
		assertEquals("[12, 13]", queue.toString());
		
		assertEquals(12l, queue.replace(14l));
		assertEquals("[13, 14]", queue.toString());
		
		queue.clear();
		assertEquals(0, queue.size());
		assertEquals("[]", queue.toString());
	}
}
