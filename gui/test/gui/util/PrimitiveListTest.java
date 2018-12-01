package gui.util;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class PrimitiveListTest {
	private static class DoubleMethods implements Methods<Double, DoubleList> {
		@Override
		public void add(DoubleList list, Double element) {
			list.add(element);
		}
		
		@Override
		public Double createElement(int element) {
			return (double)element;
		}
		
		@Override
		public DoubleList createList() {
			return new DoubleList();
		}
		
		@Override
		public DoubleList createList(int expectedSize) {
			return new DoubleList(expectedSize);
		}
		
		@Override
		public Double get(DoubleList list, int index) {
			return list.get(index);
		}
		
		@Override
		public Double set(DoubleList list, int index, Double element) {
			return list.set(index, element);
		}
	}
	
	private static class IntMethods implements Methods<Integer, IntList> {
		@Override
		public void add(IntList list, Integer element) {
			list.add(element);
		}
		
		@Override
		public Integer createElement(int element) {
			return element;
		}
		
		@Override
		public IntList createList() {
			return new IntList();
		}
		
		@Override
		public IntList createList(int expectedSize) {
			return new IntList(expectedSize);
		}
		
		@Override
		public Integer get(IntList list, int index) {
			return list.get(index);
		}
		
		@Override
		public Integer set(IntList list, int index, Integer element) {
			return list.set(index, element);
		}
	}
	
	private static class LongMethods implements Methods<Long, LongList> {
		@Override
		public void add(LongList list, Long element) {
			list.add(element);
		}
		
		@Override
		public Long createElement(int element) {
			return (long)element;
		}
		
		@Override
		public LongList createList() {
			return new LongList();
		}
		
		@Override
		public LongList createList(int expectedSize) {
			return new LongList(expectedSize);
		}
		
		@Override
		public Long get(LongList list, int index) {
			return list.get(index);
		}
		
		@Override
		public Long set(LongList list, int index, Long element) {
			return list.set(index, element);
		}
	}
	
	private static interface Methods<E, L extends PrimitiveList<L>> {
		void add(L list, E element);
		E createElement(int element);
		L createList();
		L createList(int expectedSize);
		E get(L list, int index);
		E set(L list, int index, E element);
	}
	
	@Test
	public void test() throws Throwable {
		test(new DoubleMethods());
		test(new IntMethods());
		test(new LongMethods());
	}
	
	private <E, L extends PrimitiveList<L>> void test(Methods<E, L> methods)
			throws Throwable {
		testAddClearGetSetSize(methods);
		testCapacity(methods);
		testIndex(methods);
		testSwap(methods);
		testToString(methods);
	}
	
	private <E, L extends PrimitiveList<L>> void testAddClearGetSetSize(
			Methods<E, L> methods) throws Throwable {
		L list=methods.createList();
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
		assertTrue(list.capacity()>list.size());
		
		methods.add(list, methods.createElement(3));
		methods.add(list, methods.createElement(4));
		methods.add(list, methods.createElement(5));
		assertFalse(list.isEmpty());
		assertEquals(3, list.size());
		assertTrue(list.capacity()>list.size());
		
		assertEquals(methods.createElement(3),
				methods.get(list, 0));
		assertEquals(methods.createElement(4),
				methods.get(list, 1));
		assertEquals(methods.createElement(5),
				methods.get(list, 2));
		
		assertEquals(methods.createElement(4),
				methods.set(list, 1, methods.createElement(7)));
		assertEquals(methods.createElement(3),
				methods.get(list, 0));
		assertEquals(methods.createElement(7),
				methods.get(list, 1));
		assertEquals(methods.createElement(5),
				methods.get(list, 2));
		
		list.clear();
		assertTrue(list.isEmpty());
	}
	
	private <E, L extends PrimitiveList<L>> void testCapacity(
			Methods<E, L> methods) throws Throwable {
		L list=methods.createList(8);
		assertEquals(8, list.capacity());
		for (int ii=9; 0<ii; --ii) {
			methods.add(list, methods.createElement(0));
		}
		assertEquals(16, list.capacity());
		assertEquals(9, list.size());
		L list2=list.compact();
		assertEquals(9, list2.capacity());
		assertEquals(9, list2.size());
		assertEquals(9, list.size());
		for (int ii=0; 9>ii; ++ii) {
			assertEquals(methods.get(list, ii), methods.get(list2, ii));
		}
		assertSame(list2, list2.compact());
	}
	
	private <E, L extends PrimitiveList<L>> void testIndex(
			Methods<E, L> methods) throws Throwable {
		L list=methods.createList();
		for (int ii=2; 0<ii; --ii) {
			for (int jj: new int[]{-1, list.size()}) {
				try {
					methods.get(list, jj);
					fail();
				}
				catch (ArrayIndexOutOfBoundsException ex) {
				}
				try {
					methods.set(list, jj, methods.createElement(0));
					fail();
				}
				catch (ArrayIndexOutOfBoundsException ex) {
				}
				try {
					list.swap(jj, 0);
					fail();
				}
				catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
			methods.add(list, methods.createElement(5));
			methods.add(list, methods.createElement(7));
			methods.add(list, methods.createElement(3));
		}
		assertEquals(6, list.size());
		assertEquals(methods.createElement(5), methods.get(list, 0));
		assertEquals(methods.createElement(7), methods.get(list, 1));
		assertEquals(methods.createElement(3), methods.get(list, 2));
		assertEquals(methods.createElement(5), methods.get(list, 3));
		assertEquals(methods.createElement(7), methods.get(list, 4));
		assertEquals(methods.createElement(3), methods.get(list, 5));
	}
	
	private <E, L extends PrimitiveList<L>> void testSwap(
			Methods<E, L> methods) throws Throwable {
		L list=methods.createList();
		methods.add(list, methods.createElement(3));
		methods.add(list, methods.createElement(7));
		methods.add(list, methods.createElement(5));
		list.swap(1, 1);
		list.swap(0, 2);
		assertEquals(methods.createElement(5),
				methods.get(list, 0));
		assertEquals(methods.createElement(7),
				methods.get(list, 1));
		assertEquals(methods.createElement(3),
				methods.get(list, 2));
	}
	
	private <E, L extends PrimitiveList<L>> void testToString(
			Methods<E, L> methods) throws Throwable {
		L list=methods.createList();
		assertEquals(
				Arrays.asList().toString(),
				list.toString());
		methods.add(list, methods.createElement(5));
		assertEquals(
				Arrays.asList(methods.createElement(5)).toString(),
				list.toString());
		methods.add(list, methods.createElement(7));
		methods.add(list, methods.createElement(3));
		assertEquals(
				Arrays.asList(methods.createElement(5),
								methods.createElement(7),
								methods.createElement(3)
						).toString(),
				list.toString());
	}
}
