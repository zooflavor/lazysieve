package gui.plotter;

import gui.graph.Sample2D;
import gui.ui.Color;
import gui.ui.GuiProcess;
import gui.ui.progress.Progress;
import javax.swing.JFrame;

public abstract class AddSampleProcess extends GuiProcess<JFrame, Plotter> {
	private Sample2D sample;
	
	public AddSampleProcess(Plotter parent) {
		super(true, parent, Plotter.TITLE);
	}
	
	@Override
	protected void background() throws Throwable {
		sample=sample(parent.selectNewColor(), progress);
	}
	
	@Override
	protected void foreground() throws Throwable {
		parent.addSample(sample);
	}
	
	protected abstract Sample2D sample(Color color, Progress progress)
			throws Throwable;
}
