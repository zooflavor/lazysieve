package gui;

import gui.check.CheckSegments;
import gui.check.CheckSieves;
import gui.io.Database;
import gui.plotter.Plotter;
import gui.ui.CloseButton;
import gui.ui.GuiWindow;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Gui extends GuiWindow<JFrame> {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("gui"),
									Command.Argument.PATH),
							Gui::main,
							"Main gui [database-directory]")));
	
	private final JFrame frame;
	
	public Gui(Session session) {
		super(session);
		frame=new JFrame("Zooflavor");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
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
