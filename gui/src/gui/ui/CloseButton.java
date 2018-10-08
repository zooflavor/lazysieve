package gui.ui;

import java.awt.Window;
import javax.swing.JButton;

public class CloseButton {
	private CloseButton() {
	}
	
	public static JButton create(Window window) {
		JButton exitButton=new JButton("Close");
		exitButton.setMnemonic('c');
		exitButton.addActionListener((event)->window.dispose());
		return exitButton;
	}
}
