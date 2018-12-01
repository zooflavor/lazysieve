package gui.math;

import java.util.function.Function;

public class CheckedFunction<T extends RuntimeException>
		implements RealFunction {
	private final Function<String, T> exceptionFactory;
	private final RealFunction function;
	
	public CheckedFunction(Function<String, T> exceptionFactory,
			RealFunction function) {
		this.exceptionFactory=exceptionFactory;
		this.function=function;
	}
	
	@Override
	public boolean isDefined(double fromX, double toX) {
		return function.isDefined(fromX, toX);
	}
	
	@Override
	public String toString() {
		return function.toString();
	}

	@Override
	public double valueAt(double xx) {
		try {
			double result=function.valueAt(xx);
			if (Double.isFinite(result)) {
				return result;
			}
		}
		catch (ArithmeticException ex) {
		}
		throw exceptionFactory.apply(String.format(
				"A %1$s függvény nincs értelmezve a %2$,g pontban.",
				function,
				xx));
	}
}
