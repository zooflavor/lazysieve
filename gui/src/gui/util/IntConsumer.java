package gui.util;

@FunctionalInterface
public interface IntConsumer {
	boolean next(int value) throws Throwable;
}
