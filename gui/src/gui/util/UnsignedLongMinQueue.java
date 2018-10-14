package gui.util;

import java.util.Arrays;

public class UnsignedLongMinQueue extends BinaryMinHeap {
	private long[] heap;
	
	public UnsignedLongMinQueue(int expectedSize) {
		heap=new long[Math.max(1, expectedSize)];
	}
	
	public void add(long value) {
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
		return Long.compareUnsigned(heap[index0], heap[index1]);
	}
	
	public long peek() {
		checkNotEmpty();
		return heap[0];
	}
	
	public long remove() {
		checkNotEmpty();
		long result=heap[0];
		--heapSize;
		heap[0]=heap[heapSize];
		fixDown(0);
		return result;
	}
	
	public long replace(long value) {
		checkNotEmpty();
		long result=heap[0];
		heap[0]=value;
		fixDown(0);
		return result;
	}
	
	@Override
	protected void swap(int index0, int index1) {
		long temp=heap[index0];
		heap[index0]=heap[index1];
		heap[index1]=temp;
	}
	
	@Override
	protected void toString(int index, StringBuilder stringBuilder) {
		stringBuilder.append(heap[index]);
	}
}
