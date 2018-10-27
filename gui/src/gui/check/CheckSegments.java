package gui.check;

import gui.Gui;
import gui.io.Segment;
import gui.io.Segments;
import gui.ui.CloseButton;
import gui.ui.GuiWindow;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class CheckSegments extends GuiWindow<JFrame> {
	public static final char MNEMONIC='s';
	public static final String TITLE="Check segments";
	
	private class Model implements TableModel {
		@Override
		public void addTableModelListener(TableModelListener listener) {
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		
		@Override
		public int getColumnCount() {
			return 1;
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return "Segment start";
		}
		
		@Override
		public int getRowCount() {
			return segmentInfos.size();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return String.format("%1$,d",
					segmentInfos.get(rowIndex).segmentStart);
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}
		
		@Override
		public void removeTableModelListener(TableModelListener listener) {
		}
	}
	
	private final JFrame frame;
	private final List<Segment.Info> segmentInfos;
	private final JTable table;
	
	public CheckSegments(Gui gui, Segments segments) {
		super(gui.session);
		segmentInfos=new ArrayList<>(segments.segments.values());
		
		frame=new JFrame(TITLE);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		
		table=new JTable(new Model());
		DefaultTableCellRenderer renderer=new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.RIGHT);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		table.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.selectAll();
		frame.getContentPane().add(new JScrollPane(table),
				BorderLayout.CENTER);
		
		JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		
		JButton sieveButton=new JButton("Sieve");
		sieveButton.setMnemonic('s');
		sieveButton.addActionListener((event)->
				new SieveProcess(this, segmentInfos, table.getSelectedRows())
						.start(session.executor));
		panel.add(sieveButton);
		
		JButton testButton=new JButton("Test");
		testButton.setMnemonic('t');
		testButton.addActionListener((event)->
				new TestProcess(this, segmentInfos, table.getSelectedRows())
						.start(session.executor));
		panel.add(testButton);
		
		panel.add(CloseButton.create(frame));
		
		frame.pack();
	}
	
	public static void start(Gui gui) {
		new StartProcess(gui)
				.start(gui.session.executor);
	}
	
	@Override
	public JFrame window() {
		return frame;
	}
}
