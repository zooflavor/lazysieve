package gui.check;

import gui.Command;
import gui.Gui;
import gui.io.Database;
import gui.io.Segment;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.Sieve;
import gui.sieve.Sieves;
import gui.ui.CloseButton;
import gui.ui.GuiProcess;
import gui.ui.GuiWindow;
import gui.ui.UnsignedLongSpinner;
import gui.ui.progress.PrintStreamProgress;
import gui.ui.progress.Progress;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataListener;

public class CheckSieves extends GuiWindow<JFrame> {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("check"),
									Command.Argument.constant("sieve"),
									Command.Argument.PATH,
									Command.Argument.STRING,
									Command.Argument.LONG,
									Command.Argument.LONG),
							CheckSieves::checkSieve,
							"Main check sieve [szita] [kezdet] [vég]",
							null)));
	public static final char MNEMONIC='s';
	public static final String TITLE="Sziták ellenőrzése";
	
	private class CheckProcess extends GuiProcess<CheckSieves, JFrame> {
		private final long end;
		private final Sieve.Descriptor sieveDescriptor;
		private final long start;
		
		public CheckProcess(long end, Sieve.Descriptor sieveDescriptor,
				long start) {
			super(true, CheckSieves.this, TITLE);
			this.end=end;
			this.sieveDescriptor=sieveDescriptor;
			this.start=start;
		}
		
		@Override
		protected void background() throws Throwable {
			CheckSieves.checkSieve(
					session.database, end, progress, sieveDescriptor, start);
		}
		
		@Override
		protected void foreground() throws Throwable {
			showMessage("A szita helyes.");
		}
	}
	
	private class EndListener implements UnsignedLongSpinner.Listener {
		@Override
		public void changed(long value) {
			long start=startSpinner.getNumber();
			if (0<Long.compareUnsigned(start, value)) {
				startSpinner.setNumber(value);
			}
		}
	}
	
	private static class SievesModel
			implements ComboBoxModel<Sieve.Descriptor> {
		private Object selected;
		
		@Override
		public void addListDataListener(ListDataListener listener) {
		}
		
		@Override
		public Sieve.Descriptor getElementAt(int index) {
			return Sieves.SIEVES.get(index);
		}
		
		@Override
		public Object getSelectedItem() {
			return selected;
		}
		
		@Override
		public int getSize() {
			return Sieves.SIEVES.size();
		}
		
		@Override
		public void removeListDataListener(ListDataListener listener) {
		}
		
		@Override
		public void setSelectedItem(Object anItem) {
			selected=anItem;
		}
	}
	
	private class StartListener implements UnsignedLongSpinner.Listener {
		@Override
		public void changed(long value) {
			long end=endSpinner.getNumber();
			if (0>Long.compareUnsigned(end, value)) {
				endSpinner.setNumber(value);
			}
		}
	}
	
	private final JComboBox<Sieve.Descriptor> sievesCombo;
	private final UnsignedLongSpinner endSpinner;
	private final JFrame frame;
	private final UnsignedLongSpinner startSpinner;

	public CheckSieves(Gui gui) {
		super(gui.session);
		
		this.frame=new JFrame(TITLE);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		
		JPanel buttons=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(buttons, BorderLayout.NORTH);
		
		JButton checkButton=new JButton("Ellenőrzés");
		checkButton.setMnemonic('e');
		checkButton.addActionListener(actionListener(this::checkButton));
		buttons.add(checkButton);
		
		buttons.add(CloseButton.create(frame));
		
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		JPanel sievesPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(sievesPanel);
		sievesCombo=new JComboBox<>(new SievesModel());
		sievesCombo.setSelectedIndex(0);
		sievesPanel.add(sievesCombo);
		
		JPanel startPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(startPanel);
		JLabel startLabel=new JLabel("Kezdőszám:");
		startPanel.add(startLabel);
		startSpinner=new UnsignedLongSpinner(
				Segment.MAX, Segment.MIN, Segment.MIN, 2l);
		startSpinner.addListener(new StartListener());
		startPanel.add(startSpinner);
		
		JPanel endPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(endPanel);
		JLabel endLabel=new JLabel("Végszám:");
		endPanel.add(endLabel);
		endSpinner=new UnsignedLongSpinner(
				Segment.MAX, Segment.MIN, Segment.NUMBERS+1, 2l);
		endSpinner.addListener(new EndListener());
		endPanel.add(endSpinner);
		
		frame.pack();
	}
	
	private void checkButton(ActionEvent event) throws Throwable {
		long end=endSpinner.getNumber();
		long start=startSpinner.getNumber();
		new CheckProcess(
						end,
						Sieves.SIEVES.get(sievesCombo.getSelectedIndex()),
						start)
				.start(session.executor);
	}
	
	public static void checkSieve(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(2));
		Sieve.Descriptor sieveDescriptor
				=Sieves.parse((String)arguments.get(3));
		Long start=(Long)arguments.get(4);
		Long end=(Long)arguments.get(5);
		checkSieve(database, end,
				new PrintStreamProgress(false, System.out),
				sieveDescriptor, start);
	}
	
	public static void checkSieve(Database database, long end,
			Progress progress, Sieve.Descriptor sieveDescriptor, long start)
			throws Throwable {
		if (0<=Long.compareUnsigned(start, end)) {
			throw new IllegalArgumentException("üres intervallum");
		}
		if (0>Long.compareUnsigned(Segment.END_NUMBER, end)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s végszám nagyobb, mint a maximum %2$s",
					UnsignedLong.format(end),
					UnsignedLong.format(Segment.END_NUMBER)));
		}
		if (0<Long.compareUnsigned(3l, start)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s kezdő szám kisebb, mint a minimum 3",
					UnsignedLong.format(start)));
		}
		if (0l==(start&1l)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s kezdő szám páros",
					UnsignedLong.format(start)));
		}
		if (0l==(end&1l)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s végszám páros",
					UnsignedLong.format(end)));
		}
		long startSegment=Long.divideUnsigned(start-1l, Segment.NUMBERS);
		long endSegment=Long.divideUnsigned(end-1l, Segment.NUMBERS);
		if (0<Long.compareUnsigned(end, endSegment*Segment.NUMBERS+1l)) {
			++endSegment;
		}
		Segment referenceSegment=new Segment();
		Segment sieveSegment=new Segment();
		Sieve sieve=sieveDescriptor.factory.get();
		sieve.reset(
				database,
				progress.subProgress(0.0, "szita inicializálása", 0.05),
				1l<<sieveDescriptor.smallSegmentSizeSuggestedLog2,
				start);
		Progress subProgress=progress.subProgress(0.05, null, 1.0);
		for (long ss=startSegment; endSegment>ss; ++ss) {
			Progress subProgress2=subProgress.subProgress(
					1.0*(ss-startSegment)/(endSegment-startSegment),
					null,
					1.0*(ss-startSegment+1l)/(endSegment-startSegment));
			subProgress2.progress(0.0);
			long segmentStart=ss*Segment.NUMBERS+1l;
			referenceSegment.clear(0l, true, segmentStart);
			sieveSegment.clear(0l, sieve.defaultPrime(), segmentStart);
			ReferenceSegment.SIEVE.generate(
					database,
					subProgress2.subProgress(
							0.0, "referencia generálása", 0.4),
					referenceSegment);
			long sieveStart=sieve.start();
			long sieveEnd=UnsignedLong.min(end, sieveSegment.segmentEnd);
			Progress subProgress3
					=subProgress2.subProgress(0.4, "szitálás", 0.7);
			while (0<Long.compareUnsigned(sieveEnd, sieve.start())) {
				subProgress3.progress(
						1.0*(sieve.start()-sieveStart)
								/(sieveEnd-sieveStart));
				sieve.sieve(OperationCounter.NOOP, sieveSegment);
			}
			sieveSegment.compare(
					sieveEnd,
					referenceSegment,
					subProgress2.subProgress(0.7, "ellenőrzés", 1.0),
					sieveStart);
			subProgress2.finished();
		}
		progress.finished("A szita helyes.");
	}
	
	public static void start(Gui gui) {
		new CheckSieves(gui)
				.show();
	}
	
	@Override
	public JFrame window() {
		return frame;
	}
}
