package gui.plotter;

import gui.math.LinearCombinationFunction;
import gui.math.Regression;
import gui.math.Sum;
import gui.ui.GuiProcess;
import java.util.List;
import java.util.function.Function;
import javax.swing.JFrame;

class ApproximationProcess extends GuiProcess<JFrame, Plotter> {
	private double error;
	private LinearCombinationFunction function;
	private final List<Function<Double, Double>> functions;
	private final Sample sample;
	
	ApproximationProcess(List<Function<Double, Double>> functions,
			Sample sample) {
		super(true, sample.plotter, "Approximation");
		this.functions=functions;
		this.sample=sample;
	}
	
	@Override
	protected void background() throws Throwable {
		function=Regression.regression(
				functions,
				Sum::priority,
				progress.subProgress(0.0, null, 0.9),
				sample.sample().sample,
				Sum::priority);
		error=Regression.distanceSquared(
				function,
				progress.subProgress(0.9, null, 1.0),
				sample.sample().sample,
				Sum::priority);
	}
	
	@Override
	protected void foreground() throws Throwable {
		sample.approximation(error, function);
	}
}
