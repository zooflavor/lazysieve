package gui.check;

import gui.Gui;
import gui.io.Segment;
import gui.io.Segments;
import gui.ui.CloseButton;
import gui.ui.GuiParent;
import gui.ui.SwingUtils;
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

public class CheckSegments implements GuiParent<JFrame> {
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
	final Gui gui;
	private final List<Segment.Info> segmentInfos;
	private final JTable table;
	
	public CheckSegments(Gui gui, Segments segments) {
		this.gui=gui;
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
						.start(gui.executor));
		panel.add(sieveButton);
		
		JButton testButton=new JButton("Test");
		testButton.setMnemonic('t');
		testButton.addActionListener((event)->
				new TestProcess(this, segmentInfos, table.getSelectedRows())
						.start(gui.executor));
		panel.add(testButton);
		
		panel.add(CloseButton.create(frame));
		
		frame.pack();
	}
	
	@Override
	public JFrame component() {
		return frame;
	}
	
	@Override
	public void setAllEnabled(boolean enabled) {
		frame.setEnabled(enabled);
	}
	
	void show() {
		SwingUtils.show(frame);
	}
	
	public static void start(Gui gui) {
		new StartProcess(gui)
				.start(gui.executor);
	}
}

/*
	private final Database database=new Database(Paths.get("../db"));
	
	private void checkSegment(long segment) throws Throwable {
		System.out.println("check1 segment "+segment);
		Check1 check=new Check1(database);
		check.check(segment*Segment.NUMBERS+1l);
	}
	
    public void main() throws Throwable {
		for (int ii=0; ; ++ii) {
			checkSegment(ii);
		}
	}
*/
/*public class Check1 {
	public final Database database;
	private final Segment fileSegment=new Segment();
	private final Segment memSegment=new Segment();
	
	public Check1(Database database) {
		this.database=database;
	}
	
	public void check(long segmentStart) throws IOException {
		Segment.checkSegmentStart(segmentStart);
		fileSegment.read(database, segmentStart);
		if (segmentStart!=fileSegment.segmentStart) {
			throw new IllegalArgumentException(String.format(
					"%1$,d!=%2$,d", segmentStart, fileSegment.segmentStart));
		}
		if (1l==segmentStart) {
			generateFirst();
		}
		else {
			generateRest(segmentStart);
		}
		fileSegment.compare(memSegment);
	}
	
	private void generateFirst() {
		LongList primes=new LongList();
		if (fileSegment.isPrime(fileSegment.bitIndex(1))) {
			throw new RuntimeException("1 shouldn't be a prime");
		}
		for (long nn=3; fileSegment.segmentEnd>nn*nn; nn+=2) {
			boolean prime=true;
			for (long dd=3; nn>=dd*dd; dd+=2) {
				if (0l==(nn%dd)) {
					prime=false;
					break;
				}
			}
			if (prime) {
				primes.add(nn);
			}
		}
		memSegment.clear(0l, 0l, 0l, 1l);
		memSegment.setNotPrime(0);
		primes.foreach((prime)->{
			for (int ii=(int)((prime*prime-1))>>>1;
					Segment.BITS>ii;
					ii+=prime) {
				memSegment.setNotPrime(ii);
			}
		});
	}
	
	private void generateRest(long segmentStart) throws IOException {
		LongList primes=database.readPrimes(segmentStart+Segment.NUMBERS-1);
		memSegment.clear(0l, 0l, 0l, segmentStart);
		primes.foreach((prime)->{
			long position=prime*Long.divideUnsigned(segmentStart, prime);
			if (0l==(position&1l)) {
				position+=prime;
			}
			if (0<Long.compareUnsigned(segmentStart, position)) {
				position+=prime<<1;
			}
			long squared=prime*prime;
			if (0<Long.compareUnsigned(squared, position)) {
				position=squared;
			}
			if (position<memSegment.segmentEnd) {
				for (int ii=memSegment.bitIndex(position);
						Segment.BITS>ii;
						ii+=prime) {
					memSegment.setNotPrime(ii);
				}
			}
		});
	}
}
*/
