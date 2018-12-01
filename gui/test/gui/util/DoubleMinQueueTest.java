package gui.util;

import java.util.Arrays;
import java.util.NoSuchElementException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class DoubleMinQueueTest {
	private static void testAddPeekRemove(double... values) {
		DoubleMinQueue queue=new DoubleMinQueue(values.length);
		for (double value: values) {
			queue.add(value);
		}
		Arrays.sort(values);
		for (int ii=0; values.length>ii; ++ii) {
			assertFalse(queue.isEmpty());
			assertEquals(values.length-ii, queue.size());
			assertEquals(values[ii], queue.peek(), 0.1);
			assertEquals(values[ii], queue.remove(), 0.1);
		}
		assertTrue(queue.isEmpty());
		assertEquals(0, queue.size());
		try {
			queue.peek();
			fail();
		}
		catch (NoSuchElementException ex) {
		}
		try {
			queue.remove();
			fail();
		}
		catch (NoSuchElementException ex) {
		}
		try {
			queue.replace(1.0);
			fail();
		}
		catch (NoSuchElementException ex) {
		}
	}
	
	@Test
	public void testAddPeekRemove() throws Throwable {
		testAddPeekRemove(1.0, 2.0, 3.0, 4.0, 5.0);
		testAddPeekRemove(5.0, 4.0, 3.0, 2.0, 1.0);
		
		testAddPeekRemove(2.0, 2.0, 3.0, 3.0, 3.0);
		testAddPeekRemove(3.0, 3.0, 3.0, 2.0, 2.0);
		testAddPeekRemove(3.0, 2.0, 3.0, 2.0, 3.0);
		
		testAddPeekRemove(3.0, 5.0, 2.0, 1.0, 4.0);
		testAddPeekRemove(4.0, 3.0, 5.0, 2.0, 1.0);
		testAddPeekRemove(1.0, 4.0, 3.0, 5.0, 2.0);
		testAddPeekRemove(2.0, 1.0, 4.0, 3.0, 5.0);
		testAddPeekRemove(5.0, 2.0, 1.0, 4.0, 3.0);
		
		testAddPeekRemove(4.0, 1.0, 2.0, 5.0, 3.0);
		testAddPeekRemove(3.0, 4.0, 1.0, 2.0, 5.0);
		testAddPeekRemove(5.0, 3.0, 4.0, 1.0, 2.0);
		testAddPeekRemove(2.0, 5.0, 3.0, 4.0, 1.0);
		testAddPeekRemove(1.0, 2.0, 5.0, 3.0, 4.0);
	}
	
	@Test
	public void testToClear() throws Throwable {
		DoubleMinQueue queue=new DoubleMinQueue(4);
		queue.add(1.0);
		assertEquals(1.0, queue.peek(), 0.1);
		assertEquals(1, queue.size());
		queue.clear();
		assertEquals(0, queue.size());
	}
	
	@Test
	public void testFixDown() throws Throwable {
		DoubleMinQueue queue=new DoubleMinQueue(8);
		queue.add(1.0);
		queue.add(3.0);
		queue.add(4.0);
		assertEquals(1.0, queue.replace(2.0), 0.1);
		assertEquals(2.0, queue.remove(), 0.1);
		assertEquals(3.0, queue.remove(), 0.1);
		assertEquals(4.0, queue.remove(), 0.1);
		assertTrue(queue.isEmpty());
		
		queue.add(1.0);
		queue.add(2.0);
		queue.add(4.0);
		assertEquals(1.0, queue.replace(3.0), 0.1);
		assertEquals(2.0, queue.remove(), 0.1);
		assertEquals(3.0, queue.remove(), 0.1);
		assertEquals(4.0, queue.remove(), 0.1);
		assertTrue(queue.isEmpty());
		
		queue.add(1.0);
		queue.add(4.0);
		queue.add(2.0);
		assertEquals(1.0, queue.replace(3.0), 0.1);
		assertEquals(2.0, queue.remove(), 0.1);
		assertEquals(3.0, queue.remove(), 0.1);
		assertEquals(4.0, queue.remove(), 0.1);
		assertTrue(queue.isEmpty());
		
		queue.add(1.0);
		queue.add(2.0);
		queue.add(3.0);
		assertEquals(1.0, queue.replace(4.0), 0.1);
		assertEquals(2.0, queue.remove(), 0.1);
		assertEquals(3.0, queue.remove(), 0.1);
		assertEquals(4.0, queue.remove(), 0.1);
		assertTrue(queue.isEmpty());
	}
	
	@Test
	public void testReplace() throws Throwable {
		DoubleMinQueue queue=new DoubleMinQueue(8);
		queue.add(4.0);
		queue.add(3.0);
		queue.add(2.0);
		queue.add(1.0);
		queue.add(0.0);
		assertEquals(0.0, queue.replace(7.0), 0.1);
		assertEquals(1.0, queue.replace(5.0), 0.1);
		assertEquals(2.0, queue.replace(9.0), 0.1);
		assertEquals(3.0, queue.replace(8.0), 0.1);
		assertEquals(4.0, queue.replace(6.0), 0.1);
		assertEquals(5.0, queue.remove(), 0.1);
		assertEquals(6.0, queue.remove(), 0.1);
		assertEquals(7.0, queue.remove(), 0.1);
		assertEquals(8.0, queue.remove(), 0.1);
		assertEquals(9.0, queue.remove(), 0.1);
		assertTrue(queue.isEmpty());
	}
	
	@Test
	public void testToString() throws Throwable {
		DoubleMinQueue queue=new DoubleMinQueue(4);
		assertEquals("[]", queue.toString());
		queue.add(1.0);
		assertEquals("[1.0]", queue.toString());
		queue.add(2.0);
		queue.add(3.0);
		queue.add(4.0);
		queue.add(5.0);
		assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0]", queue.toString());
	}
}
