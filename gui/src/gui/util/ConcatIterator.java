package gui.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConcatIterator<T> implements Iterator<T> {
	private final Deque<Iterator<? extends T>> iterators;
	
	public ConcatIterator(Collection<Iterator<? extends T>> iterators) {
		this.iterators=new ArrayDeque<>(iterators);
	}
	
	@Override
	public boolean hasNext() {
		while (true) {
			if (iterators.isEmpty()) {
				return false;
			}
			if (iterators.peekFirst().hasNext()) {
				return true;
			}
			iterators.pollFirst();
		}
	}
	
	@Override
	public T next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		return iterators.peekFirst().next();
	}
}
