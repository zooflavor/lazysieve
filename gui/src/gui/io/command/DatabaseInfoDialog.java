package gui.io.command;

import gui.Gui;
import gui.io.DatabaseInfo;
import gui.ui.GuiProcess;
import gui.ui.GuiWindow;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DatabaseInfoDialog extends GuiWindow<JDialog> {
	public static final char MNEMONIC='i';
	public static final String TITLE="DB info";
	
	public static class Process extends GuiProcess<Gui, JFrame> {
		private List<DatabaseInfo.Output> info;
		
		public Process(Gui gui) {
			super(true, gui, TITLE);
		}
		
		@Override
		protected void background() throws Throwable {
			info=parent.session.database
					.info(progress)
					.output(null, parent.session.database, false);
		}
		
		@Override
		protected void foreground() throws Throwable {
			new DatabaseInfoDialog(parent, info)
					.show();
		}
	}
	
	private final JDialog dialog;
	
	private DatabaseInfoDialog(Gui gui, List<DatabaseInfo.Output> info) {
		super(gui.session);
		dialog=new JDialog(gui.window(), TITLE);
		dialog.getContentPane().setLayout(new BorderLayout());
		
		DefaultTableModel model=new DefaultTableModel() {
			private static final long serialVersionUID=0l;
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		model.addColumn("");
		model.addColumn("");
		JTable table=new JTable(model);
		DefaultTableCellRenderer renderer0=new DefaultTableCellRenderer();
		table.getColumnModel().getColumn(0).setCellRenderer(renderer0);
		DefaultTableCellRenderer renderer1=new DefaultTableCellRenderer();
		renderer1.setHorizontalAlignment(JLabel.RIGHT);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer1);
		table.setTableHeader(null);
		JScrollPane tableScroll=new JScrollPane(table);
		dialog.getContentPane().add(tableScroll, BorderLayout.CENTER);
		info.forEach((output)->{
			if (null==output) {
				model.addRow(new Object[]{"", ""});
			}
			else {
				model.addRow(new Object[]{output.key, output.value});
			}
		});
		
		int[] columnWidths=new int[table.getColumnCount()];
		Arrays.fill(columnWidths, 64);
		for (int rr=model.getRowCount()-1; 0<=rr; --rr) {
			for (int cc=columnWidths.length-1; 0<=cc; --cc) {
				columnWidths[cc]=Math.max(columnWidths[cc],
						2+table.getColumnModel().getColumn(cc)
								.getCellRenderer()
								.getTableCellRendererComponent(table,
										model.getValueAt(rr, cc), false, false,
										rr, cc)
								.getPreferredSize()
								.width);
			}
		}
		int width=3*columnWidths.length;
		for (int cc=columnWidths.length-1; 0<=cc; --cc) {
			table.getColumnModel().getColumn(cc)
					.setPreferredWidth(columnWidths[cc]);
			width+=columnWidths[cc];
		}
		tableScroll.setPreferredSize(
				new Dimension(
						width,
						5+model.getRowCount()*(2+table.getRowHeight())));
		
		JPanel south=new JPanel();
		south.setLayout(new FlowLayout());
		dialog.getContentPane().add(south, BorderLayout.SOUTH);
		
		JButton okButton=new JButton("Ok");
		okButton.setMnemonic('o');
		okButton.addActionListener((event)->dialog.dispose());
		south.add(okButton);
	}
    
    public static void start(Gui gui) throws Throwable {
		new Process(gui).start(gui.session.executor);
    }
	
	@Override
	public JDialog window() {
		return dialog;
	}
}
