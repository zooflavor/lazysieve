package gui.util;

import java.util.Arrays;
import java.util.function.LongConsumer;

public class LongList {
	private int size;
	private long[] values;
	
	public LongList(int expectedSize) {
		if (0>=expectedSize) {
			expectedSize=1;
		}
		int highest=Integer.highestOneBit(expectedSize);
		if (expectedSize!=highest) {
			expectedSize=highest<<1;
		}
		values=new long[expectedSize];
	}
	
	public LongList() {
		this(16);
	}
	
	public void add(long value) {
		if (values.length<=size) {
			values=Arrays.copyOf(values, 2*values.length);
		}
		values[size]=value;
		++size;
	}
	
	public void check(int index) {
		if ((0>index)
				|| (size<=index)) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}
	
	public void clear() {
		size=0;
	}
	
	public void foreach(LongConsumer consumer) {
		for (int ii=0; size>ii; ++ii) {
			consumer.accept(values[ii]);
		}
	}
	
	public long get(int index) {
		check(index);
		return values[index];
	}
	
	public boolean isEmpty() {
		return 0>=size;
	}
	
	public long set(int index, long value) {
		check(index);
		long result=values[index];
		values[index]=value;
		return result;
	}
	
	public int size() {
		return size;
	}
}
