package gui.util;

import java.util.NoSuchElementException;

public abstract class BinaryMinHeap {
	protected int heapSize;
	
	protected void checkNotEmpty() {
		if (isEmpty()) {
			throw new NoSuchElementException();
		}
	}
	
	public void clear() {
		heapSize=0;
	}
	
	protected abstract int compare(int index0, int index1);
	
	protected void fixDown(int index) {
		while (true) {
			int leftChild=leftChild(index);
			if (heapSize<=leftChild) {
				return;
			}
			int lic=compare(leftChild, index);
			int rightChild=rightChild(index);
			if (heapSize>rightChild) {
				int ric=compare(rightChild, index);
				if ((0<=lic)
						&& (0<=ric)) {
					return;
				}
				if ((0>lic)
						&& (0>ric)) {
					if (0>=compare(leftChild, rightChild)) {
						swap(index, leftChild);
						index=leftChild;
					}
					else {
						swap(index, rightChild);
						index=rightChild;
					}
				}
				else if (0>lic) {
					swap(index, leftChild);
					index=leftChild;
				}
				else {
					swap(index, rightChild);
					index=rightChild;
				}
			}
			else {
				if (0>lic) {
					swap(index, leftChild);
				}
				return;
			}
		}
	}
	
	protected void fixUp(int index) {
		while (0<index) {
			int parent=parent(index);
			if (0>=compare(parent, index)) {
				return;
			}
			swap(parent, index);
			index=parent;
		}
	}
	
	public boolean isEmpty() {
		return 0>=heapSize;
	}
	
	protected static int leftChild(int index) {
		return (index<<1)+1;
	}
	
	protected static int parent(int index) {
		return (index-1)>>>1;
	}
	
	protected static int rightChild(int index) {
		return (index+1)<<1;
	}
	
	public int size() {
		return heapSize;
	}
	
	protected abstract void swap(int index0, int index1);
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		for (int ii=0; heapSize>ii; ++ii) {
			if (0!=ii) {
				sb.append(", ");
			}
			toString(ii, sb);
		}
		sb.append("]");
		return sb.toString();
	}
	
	protected abstract void toString(int index, StringBuilder stringBuilder);
}
