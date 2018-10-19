package gui.util;

import java.util.Arrays;

public class LongList {
	private int size;
	private long[] values;
	
	public LongList(int expectedSize) {
		if (0>=expectedSize) {
			expectedSize=1;
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
	
	public void foreach(LongConsumer consumer) throws Throwable {
		for (int ii=0, ss=size; ss>ii; ++ii) {
			if (!consumer.next(values[ii])) {
				return;
			}
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
	
	public void swapLastAndRemove(int index) {
		check(index);
		int size1=size-1;
		if (index<size1) {
			values[index]=values[size1];
		}
		size=size1;
	}
}
