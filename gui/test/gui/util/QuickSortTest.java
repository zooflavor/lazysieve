package gui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class QuickSortTest {
	private static <T> void swap(List<T> list, int index0, int index1) {
		assertFalse(index0==index1);
		list.set(index0, list.set(index1, list.get(index0)));
	}
	
	@Test
	public void testSort() throws Throwable {
		List<Integer> list=new ArrayList<>();
		List<Integer> input=Arrays.asList(1, 5, 8, 3, 6, 4, 9, 7, 2);
		Swap swap=(index0, index1)->swap(list, index0, index1);
		for (int from=0; input.size()>=from; ++from) {
			for (int to=0; input.size()>=to; ++to) {
				for (Compare compare: new Compare[]{
						(index0, index1)->list.get(index0)
								.compareTo(list.get(index1)),
						(index0, index1)->Integer.compare(
								list.get(index0)%2,
								list.get(index1)%2),
						(index0, index1)->{
							int cc=Integer.compare(
									list.get(index0)%2,
									list.get(index1)%2);
							if (0!=cc) {
								return cc;
							}
							return list.get(index0)
									.compareTo(list.get(index1));
						},
						(index0, index1)->Integer.compare(
								list.get(index0)%3,
								list.get(index1)%3)}) {
					list.clear();
					list.addAll(input);
					QuickSort.sort(compare, from, swap, to);
					assertTrue(list.containsAll(input));
					for (int ii=from; to>ii+1; ++ii) {
						assertTrue(0>=compare.compare(ii, ii+1));
					}
				}
			}
		}
	}
	
	@Test
	public void testSplit() throws Throwable {
		List<Integer> list=new ArrayList<>();
		List<Integer> input=Arrays.asList(1, 5, 8, 3, 6, 4, 9, 7, 2);
		Swap swap=(index0, index1)->swap(list, index0, index1);
		for (int from=0; input.size()>=from; ++from) {
			for (int to=0; input.size()>=to; ++to) {
				for (Property property: new Property[]{
						(index)->0==(list.get(index)%2),
						(index)->0==(list.get(index)%3)}) {
					list.clear();
					list.addAll(input);
					int split=QuickSort.split(from, property, swap, to);
					assertTrue(list.containsAll(input));
					assertTrue(from<=split);
					if (from<to) {
						assertTrue(split<=to);
						for (int ii=from; split>ii; ++ii) {
							assertFalse(property.hasProperty(ii));
						}
						for (int ii=split; to>ii; ++ii) {
							assertTrue(property.hasProperty(ii));
						}
					}
				}
			}
		}
	}
}
