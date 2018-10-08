package gui;

import gui.io.Database;
import gui.ui.GuiProcess;
import gui.ui.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DatabaseInfo {
	public static final char MNEMONIC='i';
	public static final String TITLE="DB info";
	
	public static class Process extends GuiProcess<JFrame, Gui> {
		private Database.Info info;
		
		public Process(Gui gui) {
			super(true, gui, TITLE);
		}
		
		@Override
		protected void background() throws Throwable {
			info=parent.database.info(progress);
		}
		
		@Override
		protected void foreground() throws Throwable {
			new DatabaseInfo(parent, info)
					.show();
		}
	}
	
	private final JDialog dialog;
	
	private DatabaseInfo(Gui gui, Database.Info info) {
		dialog=new JDialog(SwingUtils.window(gui.component()), TITLE);
		dialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel center=new JPanel();
		center.setLayout(new GridLayout(0, 2));
		dialog.getContentPane().add(center, BorderLayout.CENTER);
		
		boolean first=true;
		for (Map.Entry<String, Database.TypeInfo> entry: Arrays.asList(
				new AbstractMap.SimpleEntry<>(
						"aggregates", info.aggregates),
				new AbstractMap.SimpleEntry<>(
						"segments", info.segments))) {
			if (first) {
				first=false;
			}
			else {
				center.add(new JLabel());
				center.add(new JLabel());
			}
			center.add(new JLabel(entry.getKey()));
			center.add(new JLabel());
			Database.TypeInfo typeInfo=entry.getValue();
			center.add(new JLabel("number of segments"));
			center.add(new JLabel(
					String.format("%1$,d", typeInfo.numberOfSegments)));
			if (null!=typeInfo.firstSegmentStart) {
				center.add(new JLabel("start of the first segment"));
				center.add(new JLabel(
					String.format("%1$,d", typeInfo.firstSegmentStart)));
			}
			if (null!=typeInfo.lastSegmentStart) {
				center.add(new JLabel("start of the last segment"));
				center.add(new JLabel(
					String.format("%1$,d", typeInfo.lastSegmentStart)));
			}
			if (null!=typeInfo.missingSegmentStart) {
				center.add(new JLabel("start of the first missing segment"));
				center.add(new JLabel(
					String.format("%1$,d", typeInfo.missingSegmentStart)));
			}
			if (null!=typeInfo.missingSegments) {
				center.add(new JLabel("number of missing segments"));
				center.add(new JLabel(
					String.format("%1$,d", typeInfo.missingSegments)));
			}
			if (null!=typeInfo.missingSegmentStart) {
				center.add(new JLabel());
				String text;
                if ((0>=typeInfo.numberOfSegments)
                        || (null==typeInfo.firstSegmentStart)
                        || (1l!=typeInfo.firstSegmentStart)) {
					text=String.format(
							"init.bin %1$s",
							gui.database.rootDirectory,
							typeInfo.missingSegmentStart);
                }
                else if (null==typeInfo.missingSegments) {
					text=String.format(
							"generator.bin %1$s start 0x%2$x reserve-space 0x100000000",
							gui.database.rootDirectory,
							typeInfo.missingSegmentStart);
				}
				else {
					text=String.format(
							"generator.bin %1$s start 0x%2$x segments 0x%3$x",
							gui.database.rootDirectory,
							typeInfo.missingSegmentStart,
							typeInfo.missingSegments);
				}
                JTextField textField=new JTextField(text);
				textField.setEditable(false);
				center.add(textField);
			}
		}
		if (0l<info.numberOfNewSegments) {
			center.add(new JLabel());
			center.add(new JLabel());
			
			center.add(new JLabel("new segments"));
			center.add(new JLabel(
					String.format("%1$,d", info.numberOfNewSegments)));
			center.add(new JLabel());
			JLabel warning=new JLabel("you should reaggregate the database!");
			warning.setForeground(Color.RED);
			center.add(warning);
		}
		
		JPanel south=new JPanel();
		south.setLayout(new FlowLayout());
		dialog.getContentPane().add(south, BorderLayout.SOUTH);
		
		JButton okButton=new JButton("Ok");
		okButton.setMnemonic('o');
		okButton.addActionListener((event)->dialog.dispose());
		south.add(okButton);
	}
	
	private void show() {
		SwingUtils.show(dialog);
	}
    
    public static void start(Gui gui) throws Throwable {
		new Process(gui).start(gui.executor);
    }
}
