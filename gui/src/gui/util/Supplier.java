package gui.util;

@FunctionalInterface
public interface Supplier<T> {
	T get() throws Throwable;
}
