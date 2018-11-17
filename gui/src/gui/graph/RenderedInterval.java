package gui.graph;

import gui.util.DoubleList;
import gui.util.QuickSort;

public class RenderedInterval {
	public static class Builder {
		private DoubleList leftValues;
		private DoubleList maxValues;
		private DoubleList minValues;
		private DoubleList rightValues;
		private DoubleList xs;
		
		public Builder(int expectedSize) {
			leftValues=new DoubleList(expectedSize);
			maxValues=new DoubleList(expectedSize);
			minValues=new DoubleList(expectedSize);
			rightValues=new DoubleList(expectedSize);
			xs=new DoubleList(expectedSize);
		}
		
		public Builder add(double xx, double leftValue, double maxValue,
				double minValue, double rightValue) {
			checkCreated();
			leftValues.add(leftValue);
			maxValues.add(maxValue);
			minValues.add(minValue);
			rightValues.add(rightValue);
			xs.add(xx);
			return this;
		}
		
		private void checkCreated() {
			if ((null==leftValues)
					|| (null==maxValues)
					|| (null==minValues)
					|| (null==rightValues)
					|| (null==xs)) {
				throw new RuntimeException("már létre lett hozva");
			}
		}
		
		public RenderedInterval create() {
			checkCreated();
			DoubleList leftValues2=leftValues;
			DoubleList maxValues2=maxValues;
			DoubleList minValues2=minValues;
			DoubleList rightValues2=rightValues;
			DoubleList xs2=xs;
			leftValues=null;
			maxValues=null;
			minValues=null;
			rightValues=null;
			xs=null;
			leftValues2=leftValues2.compact();
			maxValues2=maxValues2.compact();
			minValues2=minValues2.compact();
			rightValues2=rightValues2.compact();
			xs2=xs2.compact();
			DoubleList leftValues3=leftValues2;
			DoubleList maxValues3=maxValues2;
			DoubleList minValues3=minValues2;
			DoubleList rightValues3=rightValues2;
			DoubleList xs3=xs2;
			QuickSort.sort(
					(index0, index1)->{
						return Double.compare(
								xs3.get(index0),
								xs3.get(index1));
					},
					0,
					(index0, index1)->{
						leftValues3.swap(index0, index1);
						maxValues3.swap(index0, index1);
						minValues3.swap(index0, index1);
						rightValues3.swap(index0, index1);
						xs3.swap(index0, index1);
					},
					xs2.size());
			return new RenderedInterval(
					leftValues2, maxValues2, minValues2, rightValues2, xs2);
		}
		
		public int size() {
			checkCreated();
			return xs.size();
		}
	}
	
	private final DoubleList leftValues;
	private final DoubleList maxValues;
	private final DoubleList minValues;
	private final DoubleList rightValues;
	private final DoubleList xs;
	
	private RenderedInterval(DoubleList leftValues, DoubleList maxValues,
			DoubleList minValues, DoubleList rightValues, DoubleList xs) {
		int size=leftValues.size();
		if (0>=size) {
			throw new IllegalArgumentException("üres");
		}
		if (maxValues.size()!=size) {
			throw new IllegalArgumentException(String.format(
					"%1$,d != %2$,d", maxValues.size(), size));
		}
		if (minValues.size()!=size) {
			throw new IllegalArgumentException(String.format(
					"%1$,d != %2$,d", minValues.size(), size));
		}
		if (rightValues.size()!=size) {
			throw new IllegalArgumentException(String.format(
					"%1$,d != %2$,d", rightValues.size(), size));
		}
		if (xs.size()!=size) {
			throw new IllegalArgumentException(String.format(
					"%1$,d != %2$,d", xs.size(), size));
		}
		this.leftValues=leftValues;
		this.maxValues=maxValues;
		this.minValues=minValues;
		this.rightValues=rightValues;
		this.xs=xs;
	}
	
	public static RenderedInterval.Builder builder() {
		return new RenderedInterval.Builder(16);
	}
	
	public static RenderedInterval.Builder builder(int expectedSize) {
		return new RenderedInterval.Builder(expectedSize);
	}
	
	public double leftValue(int index) {
		return leftValues.get(index);
	}
	
	public double maxValue(int index) {
		return maxValues.get(index);
	}
	
	public double minValue(int index) {
		return minValues.get(index);
	}
	
	public double rightValue(int index) {
		return rightValues.get(index);
	}
	
	public int size() {
		return xs.size();
	}
	
	public double xx(int index) {
		return xs.get(index);
	}
}
