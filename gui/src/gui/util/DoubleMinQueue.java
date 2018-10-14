package gui.util;

import java.util.Arrays;

public class DoubleMinQueue extends BinaryMinHeap {
	protected double[] heap;
	
	public DoubleMinQueue(int expectedSize) {
		heap=new double[Math.max(1, expectedSize)];
	}
	
	public void add(double value) {
		if (heapSize>=heap.length) {
			heap=Arrays.copyOf(heap, 2*heap.length);
		}
		int index=heapSize;
		heap[index]=value;
		++heapSize;
		fixUp(index);
	}
	
	@Override
	protected int compare(int index0, int index1) {
		return Double.compare(heap[index0], heap[index1]);
	}
	
	public double peek() {
		checkNotEmpty();
		return heap[0];
	}
	
	public double remove() {
		checkNotEmpty();
		double result=heap[0];
		--heapSize;
		heap[0]=heap[heapSize];
		fixDown(0);
		return result;
	}
	
	public double replace(double value) {
		checkNotEmpty();
		double result=heap[0];
		heap[0]=value;
		fixDown(0);
		return result;
	}
	
	@Override
	protected void swap(int index0, int index1) {
		double temp=heap[index0];
		heap[index0]=heap[index1];
		heap[index1]=temp;
	}
	
	@Override
	protected void toString(int index, StringBuilder stringBuilder) {
		stringBuilder.append(heap[index]);
	}
}
