package gui.check;

import gui.Command;
import gui.Gui;
import gui.io.Database;
import gui.io.LargePrimesProducer;
import gui.io.PrimesProducer;
import gui.io.Segment;
import gui.io.Segments;
import gui.io.SmallPrimesProducer;
import gui.math.UnsignedLong;
import gui.ui.CloseButton;
import gui.ui.GuiWindow;
import gui.ui.progress.PrintStreamProgress;
import gui.ui.progress.Progress;
import gui.util.Consumer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class CheckSegments extends GuiWindow<JFrame> {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("check"),
									Command.Argument.constant("segments"),
									Command.Argument.PATH,
									Command.Argument.constant("sieve")),
							CheckSegments::checkSegments,
							"Main check segments [adatbázis könytár] sieve [szegmens kezdetek...]",
							Command.Argument.LONG),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("check"),
									Command.Argument.constant("segments"),
									Command.Argument.PATH,
									Command.Argument.constant("test")),
							CheckSegments::checkSegments,
							"Main check segments [adatbázis könytár] test [szegmens kezdetek...]",
							Command.Argument.LONG)));
	public static final char MNEMONIC='f';
	public static final String TITLE="Szegmensfájlok ellenőrzése";
	
	private class ActionListener implements Consumer<ActionEvent> {
		@Override
		public void consume(ActionEvent value) throws Throwable {
			ReferenceSegment referenceSegment
					=(ReferenceSegment)referenceSegments.getSelectedItem();
			int[] selectedRows=table.getSelectedRows();
			List<Long> segmentStarts=new ArrayList<>(selectedRows.length);
			for (int ii=0; selectedRows.length>ii; ++ii) {
				segmentStarts.add(
						segmentInfos.get(selectedRows[ii]).segmentStart);
			}
			new CheckProcess(CheckSegments.this, referenceSegment,
							segmentStarts)
					.start(session.executor);
		}
	}
	
	private class ReferenceSegmentsModel
			implements ComboBoxModel<ReferenceSegment> {
		private Object selected;
		
		@Override
		public void addListDataListener(ListDataListener listener) {
		}
		
		@Override
		public ReferenceSegment getElementAt(int index) {
			return ReferenceSegment.REFERENCE_SEGMENTS.get(index);
		}
		
		@Override
		public Object getSelectedItem() {
			return selected;
		}
		
		@Override
		public int getSize() {
			return ReferenceSegment.REFERENCE_SEGMENTS.size();
		}
		
		@Override
		public void removeListDataListener(ListDataListener listener) {
		}
		
		@Override
		public void setSelectedItem(Object anItem) {
			selected=anItem;
		}
	}
	
	private class SegmentInfosModel implements TableModel {
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
			return UnsignedLong.format(
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
	private final JComboBox<ReferenceSegment> referenceSegments;
	private final List<Segment.Info> segmentInfos;
	private final JTable table;
	
	public CheckSegments(Gui gui, Segments segments) {
		super(gui.session);
		segmentInfos=new ArrayList<>(segments.segments.values());
		
		frame=new JFrame(TITLE);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		
		table=new JTable(new SegmentInfosModel());
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
		
		JButton sieveButton=new JButton("Ellenőrzés");
		sieveButton.setMnemonic('E');
		sieveButton.addActionListener(actionListener(new ActionListener()));
		panel.add(sieveButton);
		
		referenceSegments=new JComboBox<>(new ReferenceSegmentsModel());
		referenceSegments.setSelectedIndex(0);
		panel.add(referenceSegments);
		
		panel.add(CloseButton.create(frame));
		
		frame.pack();
	}
	
	public static void checkSegments(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(2));
		String type=(String)arguments.get(3);
		List<Long> segments=new ArrayList<>();
		for (int ii=4; arguments.size()>ii; ++ii) {
			segments.add((Long)arguments.get(ii));
		}
		Progress progress=new PrintStreamProgress(false, System.out);
		if (segments.isEmpty()) {
			Segments segments2=database.readSegments(
					progress.subProgress(0.0, null, 0.01));
			segments.addAll(segments2.segments.keySet());
		}
		checkSegments(database,
				progress.subProgress(0.01, null, 1.0),
				ReferenceSegment.parse(type),
				segments);
		progress.finished("A szegmensek helyesek.");
	}

	public static void checkSegments(Database database, Progress progress,
			ReferenceSegment referenceSegment, List<Long> segmentStarts)
			throws Throwable {
		if (segmentStarts.isEmpty()) {
			progress.finished();
			return;
		}
		segmentStarts.forEach(Segment::checkSegmentStart);
		PrimesProducer largePrimes=new LargePrimesProducer(database);
		PrimesProducer smallPrimes=new SmallPrimesProducer();
		Segment readSegment=new Segment();
		Segment generatedSegment=new Segment();
		for (int ii=0; segmentStarts.size()>ii; ++ii) {
			Progress subProgress=progress.subProgress(
					1.0*ii/segmentStarts.size(),
					String.format("szegmens %1$,d/%2$,d",
							ii+1, segmentStarts.size()),
					1.0*(ii+1)/segmentStarts.size());
			subProgress.progress(0.0);
			readSegment.read(database, segmentStarts.get(ii));
			subProgress.progress(0.01);
			generatedSegment.clear(0l, true, readSegment.segmentStart);
			gui.io.PrimesProducer primes2;
			if (1l==readSegment.segmentStart) {
				generatedSegment.setComposite(0);
				primes2=smallPrimes;
			}
			else {
				primes2=largePrimes;
			}
			referenceSegment.generate(primes2,
					subProgress.subProgress(
							0.01, "referencia generálása", 0.8),
					generatedSegment);
			subProgress.progress(0.9);
			readSegment.compare(generatedSegment,
					subProgress.subProgress(0.8, "ellenőrzés", 1.0));
			subProgress.finished();
		}
		progress.finished("A szegmensek helyesek.");
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
