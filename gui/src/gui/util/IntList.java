package gui.util;

import java.util.Arrays;

public class IntList extends PrimitiveList<IntConsumer, IntList> {
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
	
	@Override
	protected boolean forEach(IntConsumer consumer, int index)
			throws Throwable {
		return consumer.next(values[index]);
	}
	
	public int get(int index) {
		check(index);
		return values[index];
	}
	
	public int set(int index, int value) {
		check(index);
		int result=values[index];
		values[index]=value;
		return result;
	}
	
	@Override
	protected void swapImpl(int index0, int index1) {
		int temp=values[index0];
		values[index0]=values[index1];
		values[index1]=temp;
	}
	
	public void swapLastAndRemove(int index) {
		check(index);
		int size1=size-1;
		if (index<size1) {
			values[index]=values[size1];
		}
		size=size1;
	}
	
	@Override
	protected void toString(StringBuilder builder, int index) {
		builder.append(values[index]);
	}
}
