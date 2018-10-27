package gui.util;

@FunctionalInterface
public interface DoubleConsumer {
	boolean next(double value) throws Throwable;
}
