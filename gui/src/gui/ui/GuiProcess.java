package gui.ui;

import gui.ui.progress.GuiProgress;
import java.awt.Window;
import java.util.concurrent.Executor;
import javax.swing.SwingUtilities;

public abstract class GuiProcess<G extends GuiWindow<W>, W extends Window> {
	private class RunnableImpl implements Runnable {
		private volatile boolean backgroundCompleted;
		
		//progress.show() and progess.dispose() switch the EventQueue
		@Override
		public void run() {
			try {
				try {
					progress.progress(0.0);
					SwingUtilities.invokeLater(()->{
						progress.show();
					});
					background();
					backgroundCompleted=true;
					progress.finished();
				}
				finally {
					SwingUtilities.invokeLater(()->{
						try {
							progress.dispose();
							SwingUtilities.invokeLater(()->{
								try {
									if (backgroundCompleted) {
										foreground();
									}
								}
								catch (Throwable throwable) {
									parent.showError(throwable);
								}
							});
						}
						catch (Throwable throwable) {
							parent.showError(throwable);
						}
					});
				}
			}
			catch (Throwable throwable) {
				SwingUtilities.invokeLater(()->{
					progress.dispose();
					SwingUtilities.invokeLater(
							()->parent.showError(throwable));
				});
			}
		}
	}
	
	protected final G parent;
	protected final GuiProgress progress;
	
	public GuiProcess(boolean cancellable, G parent, String title) {
		this.parent=parent;
		progress=new GuiProgress(cancellable, parent.window(), title);
	}
	
	protected abstract void background() throws Throwable;
	
	protected abstract void foreground() throws Throwable;
	
	public void start(Executor executor) {
		executor.execute(new RunnableImpl());
	}
}
