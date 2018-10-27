package gui.plotter;

import gui.math.LinearCombinationFunction;
import gui.math.RealFunction;
import gui.math.Regression;
import gui.math.Sum;
import gui.ui.GuiProcess;
import java.util.List;
import javax.swing.JFrame;

class ApproximationProcess extends GuiProcess<Plotter, JFrame> {
	private double error;
	private LinearCombinationFunction function;
	private final List<RealFunction> functions;
	private final SamplePanel samplePanel;
	
	ApproximationProcess(List<RealFunction> functions,
			SamplePanel samplePanel) {
		super(true, samplePanel.plotter, "Approximation");
		this.functions=functions;
		this.samplePanel=samplePanel;
	}
	
	@Override
	protected void background() throws Throwable {
		function=Regression.regression(
				functions,
				Sum::priority,
				progress.subProgress(0.0, null, 0.9),
				samplePanel.sample().asList(),
				Sum::priority);
		error=Regression.distanceSquared(
				function,
				progress.subProgress(0.9, null, 1.0),
				samplePanel.sample().asList(),
				Sum::priority);
	}
	
	@Override
	protected void foreground() throws Throwable {
		samplePanel.approximation(error, function);
	}
}
