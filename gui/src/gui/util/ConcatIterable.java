package gui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ConcatIterable<T> implements Iterable<T> {
	private final List<Iterable<? extends T>> iterables;
	
	public ConcatIterable(List<Iterable<? extends T>> iterables) {
		this.iterables
				=Collections.unmodifiableList(new ArrayList<>(iterables));
	}
	
	@Override
	public Iterator<T> iterator() {
		List<Iterator<? extends T>> iterators
				=new ArrayList<>(iterables.size());
		iterables.forEach((iterable)->iterators.add(iterable.iterator()));
		return new ConcatIterator<>(iterators);
	}
}
