package gui.util;

@FunctionalInterface
public interface LongConsumer {
	boolean next(long value) throws Throwable;
}
