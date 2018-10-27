package gui.math;

public interface RealFunction {
	boolean isDefined(double fromX, double toX);
	double valueAt(double xx);
}
