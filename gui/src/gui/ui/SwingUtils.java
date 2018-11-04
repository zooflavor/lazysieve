package gui.ui;

import gui.util.Consumer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class SwingUtils {
	private SwingUtils() {
	}
	
	public static void ask(Consumer<Boolean> handler,
			Consumer<Throwable> logger, Component owner, String question) {
		JDialog dialog=new JDialog(window(owner));
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		java.util.function.Consumer<Boolean> handler2=(result)->{
			try {
				try {
					dialog.dispose();
				}
				finally {
					handler.consume(result);
				}
			}
			catch (Throwable throwable) {
				try {
					logger.consume(throwable);
				}
				catch (Throwable throwable2) {
				}
			}
		};
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				handler2.accept(Boolean.FALSE);
			}

			@Override
			public void windowClosing(WindowEvent event) {
				handler2.accept(Boolean.FALSE);
			}
		});
		
		JPanel panel=new JPanel(new BorderLayout());
		dialog.getContentPane().add(panel);
		
		JLabel text=new JLabel(question);
		panel.add(new JScrollPane(text), BorderLayout.CENTER);
		
		JPanel buttonPanel=new JPanel(new FlowLayout());
		panel.add(buttonPanel, BorderLayout.SOUTH);
		JButton yesButton=new JButton("Yes");
		yesButton.setMnemonic('Y');
		yesButton.addActionListener((event)->handler2.accept(Boolean.TRUE));
		buttonPanel.add(yesButton);
		JButton noButton=new JButton("No");
		noButton.setMnemonic('N');
		noButton.addActionListener((event)->handler2.accept(Boolean.FALSE));
		buttonPanel.add(noButton);
		
		show(dialog);
	}
	
	public static String error(boolean message, Throwable throwable) {
		if (message
                && (throwable instanceof MessageException)) {
			return throwable.getMessage();
		}
		try (Writer sw=new StringWriter();
				PrintWriter pw=new PrintWriter(sw)) {
			throwable.printStackTrace(pw);
			pw.flush();
			return sw.toString();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void setupClose(Window window) {
		if (window instanceof JDialog) {
			((JDialog)window).setDefaultCloseOperation(
					WindowConstants.DISPOSE_ON_CLOSE);
		}
		else if (window instanceof JFrame) {
			((JFrame)window).setDefaultCloseOperation(
					WindowConstants.DISPOSE_ON_CLOSE);
		}
		else {
			throw new IllegalArgumentException(String.valueOf(window));
		}
	}
	
	public static void setupClosed(gui.util.Consumer<Void> handler,
			Consumer<Throwable> logger, Window window) {
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				try {
					handler.consume(null);
				}
				catch (Throwable throwable) {
					try {
						logger.consume(throwable);
					}
					catch (Throwable throwable2) {
					}
				}
			}
			
			@Override
			public void windowClosing(WindowEvent event) {
				windowClosed(event);
			}
		});
	}
	
	public static void show(Window window) {
		if (window instanceof Dialog) {
			((Dialog)window).setModal(true);
		}
		window.pack();
		Dimension dimension=Toolkit.getDefaultToolkit().getScreenSize();
		int maxWidth=9*dimension.width/10;
		int maxHeight=9*dimension.height/10;
		int windowWidth=Math.min(maxWidth, window.getWidth());
		int windowHeight=Math.min(maxHeight, window.getHeight());
		if ((window.getWidth()!=windowWidth)
				|| (window.getHeight()!=windowHeight)) {
			window.setSize(windowWidth, windowHeight);
		}
		window.setLocation(
				(dimension.width-window.getWidth())/2,
				(dimension.height-window.getHeight())/2);
		window.setVisible(true);
		window.requestFocus();
		SwingUtilities.invokeLater(window::requestFocus);
	}
	
	public static void showError(Component owner, Throwable throwable) {
		System.out.flush();
		throwable.printStackTrace(System.err);
		System.err.flush();
        
        if ((throwable instanceof MessageException)
                && (null!=throwable.getMessage())
                && (!throwable.getMessage().trim().isEmpty())) {
            JOptionPane.showMessageDialog(owner, throwable.getMessage());
        }
        else {
            JDialog dialog=new JDialog(window(owner));
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JPanel panel=new JPanel(new BorderLayout());
            dialog.getContentPane().add(panel);

            JTextArea text=new JTextArea(error(false, throwable));
            text.setEditable(false);
            panel.add(new JScrollPane(text), BorderLayout.CENTER);

            JPanel buttonPanel=new JPanel(new FlowLayout());
            panel.add(buttonPanel, BorderLayout.SOUTH);
            JButton button=new JButton("Ok");
            button.setMnemonic('O');
            button.addActionListener((event)->dialog.dispose());
            buttonPanel.add(button);

            show(dialog);
        }
	}
	
	public static Window window(Component component) {
		for (Component ii=component; null!=ii; ) {
			if (ii instanceof Window) {
				Window window=(Window)ii;
				if (window.isVisible()) {
					return window;
				}
				ii=window.getOwner();
			}
			else {
				ii=ii.getParent();
			}
		}
		return null;
	}
}
