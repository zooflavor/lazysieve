package gui.check;

import gui.Gui;
import gui.io.Segment;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.Sieve;
import gui.sieve.SieveCheckFactory;
import gui.sieve.Sieves;
import gui.ui.CloseButton;
import gui.ui.GuiParent;
import gui.ui.GuiProcess;
import gui.ui.SwingUtils;
import gui.ui.UnsignedLongSpinner;
import gui.ui.progress.Progress;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataListener;

public class CheckSieves implements GuiParent<JFrame> {
	public static final char MNEMONIC='c';
	public static final String TITLE="Check sieves";
	
	private class CheckProcess extends GuiProcess<JFrame, CheckSieves> {
		private final long end;
		private final long endSegment;
		private Segment referenceSegment;
		private Sieve sieve;
		private final SieveCheckFactory sieveFactory;
		private Segment sieveSegment;
		private final long start;
		private final long startSegment;
		
		public CheckProcess(long end, long endSegment,
				SieveCheckFactory sieveFactory, long start,
				long startSegment) {
			super(true, CheckSieves.this, TITLE);
			this.end=end;
			this.endSegment=endSegment;
			this.sieveFactory=sieveFactory;
			this.start=start;
			this.startSegment=startSegment;
		}
		
		@Override
		protected void background() throws Throwable {
			referenceSegment=new Segment();
			sieve=sieveFactory.create();
			sieveSegment=new Segment();
			sieve.reset(
					gui.database,
					progress.subProgress(0.0, "init sieve", 0.05),
					1l<<sieveFactory.smallSegmentSizeLog2(),
					start);
			Progress subProgress=progress.subProgress(0.05, null, 1.0);
			for (long ss=startSegment; endSegment>ss; ++ss) {
				Progress subProgress2=subProgress.subProgress(
						1.0*(ss-startSegment)/(endSegment-startSegment),
						null,
						1.0*(ss-startSegment+1l)/(endSegment-startSegment));
				subProgress2.progress(0.0);
				long segmentStart=ss*Segment.NUMBERS+1l;
				referenceSegment.clear(0l, 0l, 0l, true, segmentStart);
				sieveSegment.clear(
						0l, 0l, 0l, sieve.defaultPrime(), segmentStart);
				SieveProcess.generateReference(
						gui.database,
						subProgress2.subProgress(0.0, "reference", 0.3333),
						referenceSegment);
				long sieveStart=sieve.start();
				long sieveEnd=UnsignedLong.min(end, sieveSegment.segmentEnd);
				Progress subProgress3
						=subProgress2.subProgress(0.3333, "sieve", 0.6667);
				while (0<Long.compareUnsigned(sieveEnd, sieve.start())) {
					subProgress3.progress(
							1.0*(sieve.start()-sieveStart)
									/(sieveEnd-sieveStart));
					sieve.sieve(OperationCounter.NOOP, sieveSegment);
				}
				sieveSegment.compare(
						sieveEnd,
						referenceSegment,
						subProgress2.subProgress(0.6667, "check", 1.0),
						sieveStart);
				subProgress2.finished();
			}
		}
		
		@Override
		protected void foreground() throws Throwable {
			parent.showMessage("all segments checked out.");
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
			implements ComboBoxModel<SieveCheckFactory> {
		private Object selected;
		
		@Override
		public void addListDataListener(ListDataListener listener) {
		}
		
		@Override
		public SieveCheckFactory getElementAt(int index) {
			return Sieves.CHECKS.get(index);
		}
		
		@Override
		public Object getSelectedItem() {
			return selected;
		}
		
		@Override
		public int getSize() {
			return Sieves.CHECKS.size();
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
	
	private final JComboBox<SieveCheckFactory> sievesCombo;
	private final UnsignedLongSpinner endSpinner;
	private final JFrame frame;
	final Gui gui;
	private final UnsignedLongSpinner startSpinner;

	public CheckSieves(Gui gui) {
		this.gui=gui;
		
		this.frame=new JFrame(TITLE);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		
		JPanel buttons=new JPanel(new FlowLayout(FlowLayout.CENTER));
		frame.getContentPane().add(buttons, BorderLayout.NORTH);
		
		JButton checkButton=new JButton("Check");
		checkButton.setMnemonic('c');
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
		JLabel startLabel=new JLabel("Start:");
		startPanel.add(startLabel);
		startSpinner=new UnsignedLongSpinner(
				Segment.MAX, Segment.MIN, Segment.MIN, 2l);
		startSpinner.addListener(new StartListener());
		startPanel.add(startSpinner);
		
		JPanel endPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(endPanel);
		JLabel endLabel=new JLabel("End:");
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
		if (0<=Long.compare(start, end)) {
			JOptionPane.showMessageDialog(frame, "empty selection");
			return;
		}
		long startSegment=Long.divideUnsigned(start-1l, Segment.NUMBERS);
		long endSegment=Long.divideUnsigned(end-1l, Segment.NUMBERS);
		if (0<Long.compareUnsigned(end, endSegment*Segment.NUMBERS+1l)) {
			++endSegment;
		}
		new CheckProcess(
						end,
						endSegment,
						Sieves.CHECKS.get(sievesCombo.getSelectedIndex()),
						start,
						startSegment)
				.start(gui.executor);
	}
	
	@Override
	public JFrame component() {
		return frame;
	}
	
	public static void start(Gui gui) {
		CheckSieves checkSieves=new CheckSieves(gui);
		SwingUtils.show(checkSieves.frame);
	}
}
