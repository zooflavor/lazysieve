package gui;

import gui.ui.SwingUtils;
import java.awt.Component;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public class GuiLogger implements Consumer<Throwable> {
	private final Component owner;
	
	public GuiLogger(Component owner) {
		this.owner=owner;
	}
	
	@Override
	public void accept(Throwable throwable) {
		SwingUtilities.invokeLater(()->{
			SwingUtils.showError(owner, throwable);
		});
	}
}
