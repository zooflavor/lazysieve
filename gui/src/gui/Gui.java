package gui;

import gui.check.CheckSegments;
import gui.check.CheckSieves;
import gui.io.Database;
import gui.plotter.Plotter;
import gui.ui.CloseButton;
import gui.ui.GuiParent;
import gui.ui.SwingUtils;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Gui implements GuiParent<JFrame> {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("gui"),
									Command.Argument.PATH),
							Gui::main,
							"Main gui [database-directory]")));
	
	private class WindowListenerImpl implements WindowListener {
		@Override
		public void windowActivated(WindowEvent event) {
		}
		
		@Override
		public void windowClosed(WindowEvent event) {
			executor.shutdown();
		}
		
		@Override
		public void windowClosing(WindowEvent event) {
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
	
	public final Database database;
	public final ScheduledExecutorService executor;
	public final JFrame frame;
	
	public Gui(Database database, ScheduledExecutorService executor)
			throws Throwable {
		this.database=database;
		this.executor=executor;
		frame=new JFrame("Zooflavor");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowListenerImpl());
		
		frame.getContentPane().setLayout(new FlowLayout());
		
		JButton databaseInfoButton=new JButton(DatabaseInfo.TITLE);
		databaseInfoButton.setMnemonic(DatabaseInfo.MNEMONIC);
		databaseInfoButton.addActionListener(
				actionListener((event)->DatabaseInfo.start(this)));
		frame.getContentPane().add(databaseInfoButton);
		
		JButton databaseImportButton=new JButton(DatabaseImport.TITLE);
		databaseImportButton.setMnemonic(DatabaseImport.MNEMONIC);
		databaseImportButton.addActionListener(
				actionListener((event)->DatabaseImport.start(this)));
		frame.getContentPane().add(databaseImportButton);
		
		JButton databaseReaggregateButton=new JButton(DatabaseReaggregate.TITLE);
		databaseReaggregateButton.setMnemonic(DatabaseReaggregate.MNEMONIC);
		databaseReaggregateButton.addActionListener(
				actionListener((event)->DatabaseReaggregate.start(this)));
		frame.getContentPane().add(databaseReaggregateButton);
		
		JButton checkSegmentsButton=new JButton(CheckSegments.TITLE);
		checkSegmentsButton.setMnemonic(CheckSegments.MNEMONIC);
		checkSegmentsButton.addActionListener(
				actionListener((event)->CheckSegments.start(this)));
		frame.getContentPane().add(checkSegmentsButton);
		
		JButton checkSievesButton=new JButton(CheckSieves.TITLE);
		checkSievesButton.setMnemonic(CheckSieves.MNEMONIC);
		checkSievesButton.addActionListener(
				actionListener((event)->CheckSieves.start(this)));
		frame.getContentPane().add(checkSievesButton);
		
		JButton plotterButton=new JButton(Plotter.TITLE);
		plotterButton.setMnemonic(Plotter.MNEMONIC);
		plotterButton.addActionListener(
				actionListener((event)->Plotter.start(this)));
		frame.getContentPane().add(plotterButton);
		
		frame.getContentPane().add(CloseButton.create(frame));
		
		//frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.pack();
	}
	
	@Override
	public JFrame component() {
		return frame;
	}
	
	public static void main(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		boolean error=true;
		ScheduledExecutorService executor=Executors.newScheduledThreadPool(
				Runtime.getRuntime().availableProcessors());
		try {
			new Gui(database, executor)
					.show();
			error=false;
		}
		finally {
			if (error) {
				executor.shutdown();
			}
		}
	}
	
	private void show() throws Throwable {
		SwingUtils.show(frame);
	}
}
