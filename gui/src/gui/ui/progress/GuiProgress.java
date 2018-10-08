package gui.ui.progress;

import gui.ui.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class GuiProgress implements Progress {
	private class ShowState implements Runnable {
		@Override
		public void run() {
			String message2;
			double progress2;
			synchronized (lock) {
				message2=message;
				progress2=progress;
				enqueued=false;
			}
			progressBar.setString(
					(null==message2)
							?String.format("%1$5.1f%%",
									Math.floor(1000.0*progress2)/10.0)
							:String.format("%1$5.1f%%: %2$s",
									Math.floor(1000.0*progress2)/10.0,
									message2));
			progressBar.setValue(Math.max(0, Math.min(1000,
					(int)Math.floor(1000.0*progress2))));
		}
	}
	
	private class WindowListenerImpl implements WindowListener {
		@Override
		public void windowActivated(WindowEvent event) {
		}
		
		@Override
		public void windowClosed(WindowEvent event) {
            synchronized (lock) {
    			cancelled=true;
            }
		}
		
		@Override
		public void windowClosing(WindowEvent event) {
            synchronized (lock) {
    			cancelled=true;
            }
		}
		
		@Override
		public void windowDeactivated(WindowEvent event) {
		}
		
		@Override
		public void windowDeiconified(WindowEvent event) {
		}
		
		@Override
		public void windowIconified(WindowEvent event) {
		}
		
		@Override
		public void windowOpened(WindowEvent event) {
		}
	}
	
	private final JButton cancelButton;
    private boolean cancellable;
	private boolean cancelled;
	private final JDialog dialog;
	private boolean enqueued;
    private final Object lock=new Object();
	private String message;
	private double progress;
	private final JProgressBar progressBar;
	private final Runnable showState=new ShowState();
	
	public GuiProgress(boolean cancellable, Component owner, String title) {
        this.cancellable=cancellable;
        
		dialog=new JDialog(SwingUtils.window(owner), title);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new WindowListenerImpl());
		dialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel north=new JPanel();
		north.setLayout(new FlowLayout());
		dialog.getContentPane().add(north, BorderLayout.NORTH);
		
		north.add(new JLabel(title), BorderLayout.NORTH);
		
		JPanel center=new JPanel();
		center.setLayout(new FlowLayout());
		dialog.getContentPane().add(center, BorderLayout.CENTER);
		
		progressBar=new JProgressBar(0, 1000);
		progressBar.setStringPainted(true);
		FontMetrics fontMetrics
				=progressBar.getFontMetrics(progressBar.getFont());
		progressBar.setPreferredSize(new Dimension(
				20*fontMetrics.getMaxAdvance(), 5+fontMetrics.getHeight()));
		center.add(progressBar, BorderLayout.CENTER);
		
		JPanel south=new JPanel();
		south.setLayout(new FlowLayout());
		dialog.getContentPane().add(south, BorderLayout.SOUTH);
		
		cancelButton=new JButton("Cancel");
        cancelButton.setEnabled(cancellable);
		cancelButton.setMnemonic('c');
		cancelButton.addActionListener(this::cancelButton);
		south.add(cancelButton);
		
		dialog.pack();
	}
    
    private void cancelButton(ActionEvent event) {
        synchronized (lock) {
            cancelled=true;
        }
    }
    
    @Override
    public boolean cancellable() {
        synchronized (lock) {
            return cancellable;
        }
    }

    @Override
    public void cancellable(boolean cancellable) {
        synchronized (lock) {
            if (this.cancellable==cancellable) {
                return;
            }
            this.cancellable=cancellable;
        }
        SwingUtilities.invokeLater(()->cancelButton.setEnabled(cancellable));
    }
	
	@Override
	public boolean cancelled() {
        synchronized (lock) {
    		return cancellable
                    && cancelled;
        }
	}
	
	public void dispose() {
		dialog.dispose();
	}
	
	@Override
	public void progress(String message, double progress) throws Throwable {
		synchronized (lock) {
			checkCancelled();
			this.message=message;
			this.progress=progress;
			if (enqueued) {
				return;
			}
			enqueued=true;
		}
		SwingUtilities.invokeLater(showState);
	}
	
	public void show() {
		SwingUtils.show(dialog);
		SwingUtilities.invokeLater(showState);
	}
}
