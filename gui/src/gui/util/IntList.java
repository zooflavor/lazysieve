package gui.util;

import java.util.Arrays;

public class IntList {
	private int size;
	private int[] values;
	
	public IntList(int expectedSize) {
		if (0>=expectedSize) {
			expectedSize=1;
		}
		values=new int[expectedSize];
	}
	
	public IntList() {
		this(16);
	}
	
	public void add(int value) {
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
	
	public void foreach(IntConsumer consumer) throws Throwable {
		for (int ii=0, ss=size; ss>ii; ++ii) {
			if (!consumer.next(values[ii])) {
				return;
			}
		}
	}
	
	public int get(int index) {
		check(index);
		return values[index];
	}
	
	public boolean isEmpty() {
		return 0>=size;
	}
	
	public int set(int index, int value) {
		check(index);
		int result=values[index];
		values[index]=value;
		return result;
	}
	
	public int size() {
		return size;
	}
}
