package gui.util;

@FunctionalInterface
public interface Consumer<T> {
	void consume(T value) throws Throwable;
}
