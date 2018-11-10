package gui.graph;

import gui.ui.Color;
import gui.util.BinarySearch;
import gui.util.DoubleList;
import gui.util.LongList;
import gui.util.QuickSort;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class Sample {
	public static class Builder {
		private LongList xs;
		private DoubleList ys;
		
		public Builder(int expectedSize) {
			xs=new LongList(expectedSize);
			ys=new DoubleList(expectedSize);
		}
		
		public Builder add(long xx, double yy) {
			checkCreated();
			xs.add(xx);
			ys.add(yy);
			return this;
		}
		
		private void checkCreated() {
			if ((null==xs)
					|| (null==ys)) {
				throw new RuntimeException("already created");
			}
		}
		
		public Sample create(String label, Color lineColor,
			PlotType plotType, Color pointColor, Color toolTipColor) {
			checkCreated();
			if (xs.isEmpty()) {
				throw new RuntimeException("empty sample");
			}
			LongList xs2=xs;
			DoubleList ys2=ys;
			xs=null;
			ys=null;
			xs2=xs2.compact();
			ys2=ys2.compact();
			LongList xs3=xs2;
			DoubleList ys3=ys2;
			QuickSort.sort(
					(index0, index1)->{
						return Long.compareUnsigned(
								xs3.get(index0),
								xs3.get(index1));
					},
					0,
					(index0, index1)->{
						xs3.swap(index0, index1);
						ys3.swap(index0, index1);
					},
					xs2.size());
			long sampleMaxX=xs2.get(xs2.size()-1);
			double sampleMaxY=ys2.get(0);
			long sampleMinX=xs2.get(0);
			double sampleMinY=sampleMaxY;
			for (int ii=ys2.size()-1; 0<ii; --ii) {
				double yy=ys2.get(ii);
				if (yy>sampleMaxY) {
					sampleMaxY=yy;
				}
				if (yy<sampleMinY) {
					sampleMinY=yy;
				}
			}
			Sample result=new Sample(label, lineColor, plotType, pointColor,
					sampleMaxX, sampleMaxY, sampleMinX, sampleMinY,
					toolTipColor, xs2, ys2);
			return result;
		}
		
		public int size() {
			return xs.size();
		}
	}
	
	private class SampleIterable implements IterableSample {
		private final int from;
		private final int size;
		private final int to;
		
		public SampleIterable(int from, int to) {
			this.from=from;
			this.to=to;
			size=to-from;
		}
		
		@Override
		public void forEachDouble(SampleIterable.DoubleConsumer consumer) {
			for (int ii=from; to>ii; ++ii) {
				consumer.next(xx(ii), yy(ii));
			}
		}
		
		@Override
		public void forEachLong(SampleIterable.LongConsumer consumer) {
			for (int ii=from; to>ii; ++ii) {
				consumer.next(xx(ii), yy(ii));
			}
		}
		
		@Override
		public int size() {
			return size;
		}
	}
	
	private class SampleList extends AbstractList<Map.Entry<Long, Double>> {
		private final int from;
		private final int size;
		
		public SampleList(int from, int to) {
			this.from=from;
			size=to-from;
		}
		
		@Override
		public Map.Entry<Long, Double> get(int index) {
			if ((0>index)
					|| (size<=index)) {
				throw new IndexOutOfBoundsException();
			}
			return new AbstractMap.SimpleImmutableEntry<>(
					xx(from+index), yy(from+index));
		}
		
		@Override
		public int size() {
			return size;
		}
	}
	
	public final String label;
	public final Color lineColor;
	public final PlotType plotType;
	public final Color pointColor;
	public final long sampleMaxX;
	public final double sampleMaxY;
	public final long sampleMinX;
	public final double sampleMinY;
	public final Color toolTipColor;
	private final LongList xs;
	private final DoubleList ys;

	private Sample(String label, Color lineColor, PlotType plotType,
			Color pointColor, long sampleMaxX, double sampleMaxY,
			long sampleMinX, double sampleMinY, Color toolTipColor,
			LongList xs, DoubleList ys) {
		if (xs.isEmpty()) {
			throw new IllegalArgumentException("empty sample");
		}
		if (xs.size()!=ys.size()) {
			throw new IllegalArgumentException("sample size mismatch");
		}
		this.label=label;
		this.lineColor=lineColor;
		this.plotType=plotType;
		this.pointColor=pointColor;
		this.sampleMaxX=sampleMaxX;
		this.sampleMaxY=sampleMaxY;
		this.sampleMinX=sampleMinX;
		this.sampleMinY=sampleMinY;
		this.toolTipColor=toolTipColor;
		this.xs=xs;
		this.ys=ys;
	}
	
	public IterableSample asIterableSample() {
		return asIterableSample(0, size());
	}
	
	public IterableSample asIterableSample(int from, int to) {
		return new SampleIterable(from, to);
	}
	
	public List<Map.Entry<Long, Double>> asList() {
		return new SampleList(0, size());
	}
	
	public List<Map.Entry<Long, Double>> asList(int from, int to) {
		if (0>from) {
			from=0;
		}
		if (size()<from) {
			from=size();
		}
		if (from>to) {
			to=from;
		}
		if (size()<to) {
			to=size();
		}
		return new SampleList(from, to);
	}
	
	public static Sample.Builder builder() {
		return new Sample.Builder(16);
	}
	
	public static Sample.Builder builder(int expectedSize) {
		return new Sample.Builder(expectedSize);
	}
	
	public int ceilingIndex(long xx) {
		return ceilingIndex(0, size(), xx);
	}
	
	public int ceilingIndex(int from, int to, long xx) {
		return validateIndex(
				from,
				BinarySearch.search(
						0,
						(index)->0>=Long.compareUnsigned(xx, xx(index)),
						size()),
				to);
	}
	
	public int floorIndex(long xx) {
		return floorIndex(0, size(), xx);
	}
	
	public int floorIndex(int from, int to, long xx) {
		return validateIndex(
				from,
				BinarySearch.search(
						0,
						(index)->0>Long.compareUnsigned(xx, xx(index)),
						size())-1,
				to);
	}
	
	public int headToIndex(boolean inclusive, long xx) {
		return headToIndex(0, inclusive, size(), xx);
	}
	
	public int headToIndex(int from, boolean inclusive, int to, long xx) {
		return BinarySearch.search(
				from,
				inclusive
						?(index)->0>Long.compareUnsigned(xx, xx(index))
						:(index)->0>=Long.compareUnsigned(xx, xx(index)),
				to);
	}
	
	public int higherIndex(long xx) {
		return higherIndex(0, size(), xx);
	}
	
	public int higherIndex(int from, int to, long xx) {
		return validateIndex(
				from,
				BinarySearch.search(
						0,
						(index)->0>Long.compareUnsigned(xx, xx(index)),
						size()),
				to);
	}
	
	public int lowerIndex(long xx) {
		return lowerIndex(0, size(), xx);
	}
	
	public int lowerIndex(int from, int to, long xx) {
		return validateIndex(
				from,
				BinarySearch.search(
						0,
						(index)->0>=Long.compareUnsigned(xx, xx(index)),
						size())-1,
				to);
	}
	
	public Sample setColors(Color lineColor, Color pointColor,
			Color toolTipColor) {
		return new Sample(label, lineColor, plotType, pointColor, sampleMaxX,
				sampleMaxY, sampleMinX, sampleMinY, toolTipColor, xs, ys);
	}
	
	public int size() {
		return xs.size();
	}
	
	public int tailFromIndex(boolean inclusive, long xx) {
		return tailFromIndex(0, inclusive, size(), xx);
	}
	
	public int tailFromIndex(int from, boolean inclusive, int to, long xx) {
		return BinarySearch.search(
				from,
				inclusive
						?(index)->0>=Long.compareUnsigned(xx, xx(index))
						:(index)->0>Long.compareUnsigned(xx, xx(index)),
				to);
	}

	@Override
	public String toString() {
		return "Sample2D("+label+": "+xs+"-"+ys+")";
	}
	
	private int validateIndex(int from, int index, int to) {
		return ((from<=index) && (index<to))?index:-1;
	}
	
	public long xx(int index) {
		return xs.get(index);
	}
	
	public double yy(int index) {
		return ys.get(index);
	}
}
