package gui.util;

public abstract class PrimitiveList<C, L extends PrimitiveList<C, L>> {
	protected int size;
	
	public PrimitiveList(int size) {
		this.size=size;
	}
	
	public abstract int capacity();
	
	protected abstract L cast();
	
	public void check(int index) {
		if ((0>index)
				|| (size<=index)) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}
	
	public void clear() {
		size=0;
	}
	
	public L compact() {
		return (capacity()==size())?cast():copy();
	}
	
	public abstract L copy();
	
	public void foreach(C consumer) throws Throwable {
		for (int ii=0, ss=size; ss>ii; ++ii) {
			if (!forEach(consumer, ii)) {
				return;
			}
		}
	}
	
	protected abstract boolean forEach(C consumer, int index) throws Throwable;
	
	public boolean isEmpty() {
		return 0>=size;
	}
	
	public int size() {
		return size;
	}
	
	public void swap(int index0, int index1) {
		check(index0);
		check(index1);
		if (index0!=index1) {
			swapImpl(index0, index1);
		}
	}
	
	protected abstract void swapImpl(int index0, int index1);
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("[");
		for (int ii=0; size>ii; ++ii) {
			if (0!=ii) {
				sb.append(", ");
			}
			toString(sb, ii);
		}
		sb.append("]");
		return sb.toString();
	}
	
	protected abstract void toString(StringBuilder builder, int index);
}
