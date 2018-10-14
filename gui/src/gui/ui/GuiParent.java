package gui.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

public interface GuiParent<C extends Component> {
	default ActionListener actionListener(EventHandler<ActionEvent> handler) {
		return (event)->{
			try {
				handler.handle(event);
			}
			catch (Throwable throwable) {
				SwingUtils.showError(component(), throwable);
			}
		};
	}
	
	C component();
	
	default void setAllEnabled(boolean enabled) {
		component().setEnabled(enabled);
	}
	
	default void showMessage(String message) {
		JOptionPane.showMessageDialog(component(), message);
	}
}
