package gui.ui;

import gui.Session;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;

public abstract class GuiWindow<W extends java.awt.Window> {
	private Session.KeepAlive keepAlive;
	public final Session session;
	
	public GuiWindow(Session session) {
		this.session=session;
	}
	
	public ActionListener actionListener(EventHandler<ActionEvent> handler) {
		return (event)->{
			try {
				handler.handle(event);
			}
			catch (Throwable throwable) {
				SwingUtils.showError(window(), throwable);
			}
		};
	}
	
	public void setAllEnabled(boolean enabled) {
		window().setEnabled(enabled);
	}
	
	public void show() {
		if (null==keepAlive) {
			W window=window();
			window.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent event) {
					try {
						super.windowClosed(event);
					}
					finally {
						if (null!=keepAlive) {
							keepAlive.close();
						}
					}
				}
			});
			keepAlive=session.keepAlive();
			SwingUtils.show(window);
		}
	}
	
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(window(), message);
	}
	
	public abstract W window();
}