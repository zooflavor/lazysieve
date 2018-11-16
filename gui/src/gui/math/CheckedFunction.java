package gui.math;

import gui.ui.MessageException;

public class CheckedFunction implements RealFunction {
	private final RealFunction function;
	
	public CheckedFunction(RealFunction function) {
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
		throw new MessageException(String.format(
				"A %1$s függvény nincs értelmezve a %2$,g pontban.",
				function,
				xx));
	}
}
