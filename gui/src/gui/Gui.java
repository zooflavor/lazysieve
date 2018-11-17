package gui;

import gui.check.CheckSegments;
import gui.check.CheckSieve;
import gui.io.Database;
import gui.plotter.Plotter;
import gui.ui.CloseButton;
import gui.ui.GuiWindow;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class Gui extends GuiWindow<JFrame> {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("gui"),
									Command.Argument.PATH),
							Gui::main,
							"Main gui [adatbázis könyvtár]"),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("gui"),
									Command.Argument.PATH,
									Command.Argument.constant("graph")),
							Gui::mainGraph,
							"Main gui [adatbázis könyvtár] graph")));
	
	private final JFrame frame;
	
	public Gui(Session session) {
		super(session);
		frame=new JFrame("Lusta szita");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		frame.getContentPane().setLayout(
				new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel closePanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(closePanel);
		closePanel.add(CloseButton.create(frame));
		
		JPanel databaseInfoPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(databaseInfoPanel);
		JButton databaseInfoButton=new JButton(DatabaseInfoDialog.TITLE);
		databaseInfoButton.setMnemonic(DatabaseInfoDialog.MNEMONIC);
		databaseInfoButton.addActionListener(
				actionListener((event)->DatabaseInfoDialog.start(this)));
		databaseInfoPanel.add(databaseInfoButton);
		
		JPanel databaseImportPanel
				=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(databaseImportPanel);
		JButton databaseImportButton=new JButton(DatabaseImport.TITLE);
		databaseImportButton.setMnemonic(DatabaseImport.MNEMONIC);
		databaseImportButton.addActionListener(
				actionListener((event)->DatabaseImport.start(this)));
		databaseImportPanel.add(databaseImportButton);
		
		JPanel databaseReaggregatePanel
				=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(databaseReaggregatePanel);
		JButton databaseReaggregateButton=new JButton(DatabaseReaggregate.TITLE);
		databaseReaggregateButton.setMnemonic(DatabaseReaggregate.MNEMONIC);
		databaseReaggregateButton.addActionListener(
				actionListener((event)->DatabaseReaggregate.start(this)));
		databaseReaggregatePanel.add(databaseReaggregateButton);
		
		JPanel checkSegmentsPanel
				=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(checkSegmentsPanel);
		JButton checkSegmentsButton=new JButton(CheckSegments.TITLE);
		checkSegmentsButton.setMnemonic(CheckSegments.MNEMONIC);
		checkSegmentsButton.addActionListener(
				actionListener((event)->CheckSegments.start(this)));
		checkSegmentsPanel.add(checkSegmentsButton);
		
		JPanel checkSievesPanel
				=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(checkSievesPanel);
		JButton checkSievesButton=new JButton(CheckSieve.TITLE);
		checkSievesButton.setMnemonic(CheckSieve.MNEMONIC);
		checkSievesButton.addActionListener(
				actionListener((event)->CheckSieve.start(this)));
		checkSievesPanel.add(checkSievesButton);
		
		JPanel plotterPanel
				=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(plotterPanel);
		JButton plotterButton=new JButton(Plotter.TITLE);
		plotterButton.setMnemonic(Plotter.MNEMONIC);
		plotterButton.addActionListener(
				actionListener((event)->Plotter.start(session)));
		plotterPanel.add(plotterButton);
		
		//frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.pack();
	}
	
	public static void mainGraph(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		boolean error=true;
		Session session=new Session(database);
		try {
			new Plotter(session)
					.show();
			error=false;
		}
		finally {
			if (error) {
				session.close();
			}
		}
	}
	
	public static void main(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		boolean error=true;
		Session session=new Session(database);
		try {
			new Gui(session)
					.show();
			error=false;
		}
		finally {
			if (error) {
				session.close();
			}
		}
	}
	
	@Override
	public JFrame window() {
		return frame;
	}
}
