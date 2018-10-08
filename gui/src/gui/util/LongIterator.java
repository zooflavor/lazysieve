package gui.util;

import java.util.NoSuchElementException;

public interface LongIterator {
	boolean hasNext();
	long next() throws NoSuchElementException;
}
