package gui.util;

public class QuickSort {
	public static class PivotProperty implements Property {
		public Compare compare;
		public int pivotIndex;
		
		public PivotProperty(Compare compare) {
			this.compare=compare;
		}
		
		@Override
		public boolean hasProperty(int index) {
			return 0>=compare.compare(pivotIndex, index);
		}
	}
	
	private QuickSort() {
	}
	
	public static void sort(Compare compare, int from, Swap swap, int to) {
		sort(from, new PivotProperty(compare), swap, to);
	}
	
	public static void sort(int from, PivotProperty pivotProperty, Swap swap,
			int to) {
		int size=to-from;
		if (1>=size) {
			return;
		}
		if (2==size) {
			if (0<pivotProperty.compare.compare(from, from+1)) {
				swap.swap(from, from+1);
			}
			return;
		}
		int last=to-1;
		int middle=from+size/2;
		int pivotIndex;
		int cc=pivotProperty.compare.compare(middle, last);
		if (0==cc) {
			pivotIndex=last;
		}
		else {
			int dd=pivotProperty.compare.compare(from, middle);
			if ((0==dd)
					|| ((0<cc) && (0<dd))
					|| ((0>cc) && (0>dd))) {
				pivotIndex=middle;
			}
			else {
				int ee=pivotProperty.compare.compare(from, last);
				pivotIndex=((0==ee)
								|| ((0>cc) && (0<ee))
								|| ((0<cc) && (0>ee)))
						?last
						:from;
			}
		}
		if (pivotIndex!=last) {
			swap.swap(pivotIndex, last);
		}
		pivotProperty.pivotIndex=last;
		int split=split(from, pivotProperty, swap, last);
		swap.swap(split, last);
		sort(from, pivotProperty, swap, split);
		sort(split+1, pivotProperty, swap, to);
	}
	
	public static int split(int from, Property property, Swap swap, int to) {
		--to;
		while (from<=to) {
			while ((from<=to)
					&& (!property.hasProperty(from))) {
				++from;
			}
			while ((from<=to)
					&& property.hasProperty(to)) {
				--to;
			}
			if (from<to) {
				swap.swap(from, to);
				++from;
				--to;
			}
		}
		return from;
	}
}
