package gui.ui;

import gui.ui.progress.GuiProgress;
import java.awt.Component;
import java.util.concurrent.Executor;
import javax.swing.SwingUtilities;

public abstract class GuiProcess<C extends Component, P extends GuiParent<C>> {
	private class RunnableImpl implements Runnable {
		@Override
		public void run() {
			try {
				try {
					progress.progress(0.0);
					SwingUtilities.invokeLater(()->{
						progress.show();
						parent.setAllEnabled(false);
					});
					background();
					progress.finished();
				}
				finally {
					SwingUtilities.invokeLater(progress::dispose);
				}
				SwingUtilities.invokeLater(()->{
					try {
						parent.setAllEnabled(true);
						foreground();
					}
					catch (Throwable throwable) {
						SwingUtils.showError(parent.component(), throwable);
					}
				});
			}
			catch (Throwable throwable) {
				SwingUtilities.invokeLater(()->{
					parent.setAllEnabled(true);
                    SwingUtils.showError(parent.component(), throwable);
				});
			}
		}
	}
	
	protected final P parent;
	protected final GuiProgress progress;
	
	public GuiProcess(boolean cancellable, P parent, String title) {
		this.parent=parent;
		progress=new GuiProgress(cancellable, parent.component(), title);
	}
	
	protected abstract void background() throws Throwable;
	
	protected abstract void foreground() throws Throwable;
	
	public void start(Executor executor) {
		executor.execute(new RunnableImpl());
	}
}
