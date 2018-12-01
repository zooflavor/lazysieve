package gui.util;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BinarySearchTest {
	@Test
	public void test() throws Throwable {
		final List<Boolean> list
				=Arrays.asList(false, false, false, true, true, true);
		Property property=list::get;
		assertEquals(1, BinarySearch.search(1, property, 1));
		assertEquals(3, BinarySearch.search(3, property, 3));
		assertEquals(2, BinarySearch.search(1, property, 2));
		assertEquals(4, BinarySearch.search(4, property, 5));
		assertEquals(4, BinarySearch.search(4, property, 8));
		assertEquals(3, BinarySearch.search(0, property, 3));
		assertEquals(3, BinarySearch.search(0, property, 6));
		assertEquals(3, BinarySearch.search(1, property, 6));
		assertEquals(3, BinarySearch.search(1, property, 5));
		assertEquals(3, BinarySearch.search(0, property, 5));
	}
}
