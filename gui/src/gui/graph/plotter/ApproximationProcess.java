package gui.graph.plotter;

import gui.math.CheckedFunction;
import gui.math.LeastSquares;
import gui.math.LinearCombinationFunction;
import gui.math.RealFunction;
import gui.math.Solver;
import gui.math.Sum;
import gui.ui.GuiProcess;
import gui.ui.MessageException;
import java.util.List;
import javax.swing.JFrame;

class ApproximationProcess extends GuiProcess<Plotter, JFrame> {
	private double error;
	private LinearCombinationFunction function;
	private final List<RealFunction> functions;
	private final SamplePanel samplePanel;
	
	ApproximationProcess(List<RealFunction> functions,
			SamplePanel samplePanel) {
		super(true, samplePanel.plotter, "Közelítés");
		this.functions=functions;
		this.samplePanel=samplePanel;
	}
	
	@Override
	protected void background() throws Throwable {
		function=LeastSquares.regression(
				functions,
				(realFunction)->new CheckedFunction<>(
						MessageException::new, realFunction),
				Sum::preferred,
				progress.subProgress(0.0, null, 0.9),
				Sum::preferred,
				samplePanel.sample().asDoubles(),
				Solver.preferred());
		error=LeastSquares.distanceSquared(
				function,
				progress.subProgress(0.9, null, 1.0),
				samplePanel.sample().asDoubles(),
				Sum::preferred);
	}
	
	@Override
	protected void foreground() throws Throwable {
		samplePanel.approximation(error, function);
	}
}
