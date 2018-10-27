package gui.util;

import java.util.Arrays;

public class DoubleList extends PrimitiveList<DoubleConsumer, DoubleList> {
	private double[] values;
	
	private DoubleList(int size, double[] values) {
		super(size);
		this.values=values;
	}
	
	public DoubleList(int expectedSize) {
		super(0);
		values=new double[Math.max(1, expectedSize)];
	}
	
	public DoubleList() {
		this(16);
	}
	
	public void add(double value) {
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
	protected DoubleList cast() {
		return this;
	}
	
	@Override
	public DoubleList copy() {
		return new DoubleList(size, Arrays.copyOf(values, size));
	}
	
	@Override
	protected boolean forEach(DoubleConsumer consumer, int index)
			throws Throwable {
		return consumer.next(values[index]);
	}
	
	public double get(int index) {
		check(index);
		return values[index];
	}
	
	public double set(int index, double value) {
		check(index);
		double result=values[index];
		values[index]=value;
		return result;
	}
	
	@Override
	protected void swapImpl(int index0, int index1) {
		double temp=values[index0];
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
