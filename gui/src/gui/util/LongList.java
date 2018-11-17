package gui.util;

import java.util.Arrays;

public class LongList extends PrimitiveList<LongList> {
	private long[] values;
	
	private LongList(int size, long[] values) {
		super(size);
		this.size=size;
		this.values=values;
	}
	
	public LongList(int expectedSize) {
		super(0);
		values=new long[Math.max(1, expectedSize)];
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
	
	@Override
	public int capacity() {
		return values.length;
	}
	
	@Override
	protected LongList cast() {
		return this;
	}
	
	@Override
	public LongList copy() {
		return new LongList(size, Arrays.copyOf(values, size));
	}
	
	public long get(int index) {
		check(index);
		return values[index];
	}
	
	public long set(int index, long value) {
		check(index);
		long result=values[index];
		values[index]=value;
		return result;
	}
	
	@Override
	protected void swapImpl(int index0, int index1) {
		long temp=values[index0];
		values[index0]=values[index1];
		values[index1]=temp;
	}
	
	@Override
	protected void toString(StringBuilder builder, int index) {
		builder.append(values[index]);
	}
}
