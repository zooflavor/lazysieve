package gui.util;

import java.util.Arrays;

public class IntList extends PrimitiveList<IntList> {
	private int[] values;
	
	private IntList(int size, int[] values) {
		super(size);
		this.values=values;
	}
	
	public IntList(int expectedSize) {
		super(0);
		values=new int[Math.max(1, expectedSize)];
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
	
	@Override
	public int capacity() {
		return values.length;
	}
	
	@Override
	protected IntList cast() {
		return this;
	}
	
	@Override
	public IntList copy() {
		return new IntList(size, Arrays.copyOf(values, size));
	}
	
	public int get(int index) {
		check(index);
		return values[index];
	}
	
	@Override
	protected void swapImpl(int index0, int index1) {
		int temp=values[index0];
		values[index0]=values[index1];
		values[index1]=temp;
	}
	
	@Override
	protected void toString(StringBuilder builder, int index) {
		builder.append(values[index]);
	}
}
