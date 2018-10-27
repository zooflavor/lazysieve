package gui.graph;

import gui.util.DoubleList;

public class FunctionSample implements IterableSample {
	private final DoubleList xs;
	private final DoubleList ys;
	
	public FunctionSample(int expectedSize) {
		this.xs=new DoubleList(expectedSize);
		this.ys=new DoubleList(expectedSize);
	}
	
	public FunctionSample() {
		this(16);
	}
	
	public void add(double xx, double yy) {
		xs.add(xx);
		ys.add(yy);
	}
	
	public void clear() {
		xs.clear();
		ys.clear();
	}
	
	@Override
	public void forEachDouble(DoubleConsumer consumer) {
		for (int ii=0; xs.size()>ii; ++ii) {
			consumer.next(xs.get(ii), ys.get(ii));
		}
	}
	
	@Override
	public void forEachLong(LongConsumer consumer) {
		for (int ii=0; xs.size()>ii; ++ii) {
			consumer.next((long)xs.get(ii), ys.get(ii));
		}
	}
	
	@Override
	public int size() {
		return xs.size();
	}
	
	public double xx(int index) {
		return xs.get(index);
	}
	
	public double yy(int index) {
		return ys.get(index);
	}
}
