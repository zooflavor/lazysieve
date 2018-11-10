package gui.plotter;

import gui.graph.PlotType;
import gui.graph.Sample;
import gui.io.Database;
import gui.math.UnsignedLong;
import gui.sieve.LongTable;
import gui.sieve.Measure;
import gui.sieve.OperationCounter;
import gui.sieve.Sieve;
import gui.sieve.SieveTable;
import gui.sieve.Sieves;
import gui.ui.CloseButton;
import gui.ui.Color;
import gui.ui.GuiWindow;
import gui.ui.UnsignedLongSpinner;
import gui.ui.progress.Progress;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;

public class MeasureSieve extends GuiWindow<JDialog> {
	public static final String TITLE="Measure sieve";
	
	private class MeasureModel implements ComboBoxModel<Measure> {
		private Measure selected=Measure.NANOSECS;
		
		@Override
		public void addListDataListener(ListDataListener listener) {
		}
		
		@Override
		public Measure getElementAt(int index) {
			return Measure.values()[index];
		}
		
		@Override
		public Object getSelectedItem() {
			return selected;
		}
		
		@Override
		public int getSize() {
			return Measure.values().length;
		}
		
		@Override
		public void removeListDataListener(ListDataListener listener) {
		}
		
		@Override
		public void setSelectedItem(Object anItem) {
			selected=(Measure)anItem;
		}
	}
	
	private class SieveModel implements ComboBoxModel<Sieve.Descriptor> {
		private Sieve.Descriptor selected=Sieves.SIEVES.get(0);
		
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
			selected=(Sieve.Descriptor)anItem;
		}
	}
	
	private final JDialog dialog;
	private final JComboBox<Measure> measure;
	private final Plotter plotter;
	private final UnsignedLongSpinner segments;
	private final JSpinner segmentSize;
	private final JLabel segmentSizeEditor;
	private final JComboBox<Sieve.Descriptor> sieve;
	private final UnsignedLongSpinner startSegment;
	private final JCheckBox sum;
	
	public MeasureSieve(Plotter plotter) throws Throwable {
		super(plotter.session);
		this.plotter=plotter;
		
		dialog=new JDialog(plotter.window(), TITLE);
		dialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel buttons=new JPanel(new FlowLayout(FlowLayout.CENTER));
		dialog.getContentPane().add(buttons, BorderLayout.NORTH);
		
		JButton measureButton=new JButton("Measure");
		measureButton.setMnemonic('m');
		measureButton.addActionListener(actionListener(this::measureButton));
		buttons.add(measureButton);
		
		buttons.add(CloseButton.create(dialog));
		
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		
		JPanel sievePanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(sievePanel);
		sieve=new JComboBox<>(new SieveModel());
		sieve.addActionListener(actionListener(this::sieveChanged));
		sievePanel.add(sieve);
		
		JPanel segmentSizePanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(segmentSizePanel);
		JLabel segmentSizeLabel=new JLabel("Segment size:");
		segmentSizePanel.add(segmentSizeLabel);
		segmentSizeEditor=new JLabel(" 9,999,999,999 = 2^99");
		segmentSizeEditor.setHorizontalAlignment(SwingConstants.RIGHT);
		segmentSizeEditor.setFont(new Font(
				Font.MONOSPACED,
				Font.PLAIN,
				segmentSizeEditor.getFont().getSize()));
		segmentSizeEditor.setPreferredSize(segmentSizeEditor.getPreferredSize());
		segmentSize=new JSpinner();
		segmentSize.setEditor(segmentSizeEditor);
		segmentSize.addChangeListener(this::segmentSizeChanged);
		segmentSizePanel.add(segmentSize);
		
		JPanel startSegmentPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(startSegmentPanel);
		JLabel startSegmentLabel=new JLabel("Start segment:");
		startSegmentPanel.add(startSegmentLabel);
		startSegment=new UnsignedLongSpinner(1l<<50, 0l, 0l, 1l);
		startSegmentPanel.add(startSegment);
		
		JPanel segmentsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(segmentsPanel);
		JLabel segmentsLabel=new JLabel("Segments:");
		segmentsPanel.add(segmentsLabel);
		segments=new UnsignedLongSpinner(1l<<30, 1l, 100l, 1l);
		segmentsPanel.add(segments);
		
		JPanel measurePanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(measurePanel);
		measure=new JComboBox<>(new MeasureModel());
		measurePanel.add(measure);
		
		JPanel sumPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(sumPanel);
		sum=new JCheckBox("sum", true);
		sumPanel.add(sum);
		
		sieveChanged(null);
		
		dialog.pack();
	}
	
	private void measureButton(ActionEvent event) throws Throwable {
		long segmentSize2=1l<<((Integer)segmentSize.getValue());
		long segments2=segments.getNumber();
		Sieve.Descriptor sieve2=Sieves.SIEVES.get(sieve.getSelectedIndex());
		long startSegment2=startSegment.getNumber();
		Measure measure2=Measure.values()[measure.getSelectedIndex()];
		boolean sum2=sum.isSelected();
		new AddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress)
					throws Throwable {
				return MeasureSieve.measureSieve(session.database, measure2,
								progress, segments2, segmentSize2, sieve2,
								startSegment2, sum2)
						.create(sieve2.longName+"-"+measure2
										+"-2^"+(63-Long.numberOfLeadingZeros(
												segmentSize2))
										+"x("+startSegment2
										+"+"+segments2
										+")-"+(sum2?"összesen":"szegmens"),
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(session.executor);
		dialog.dispose();
	}
	
	public static Sample.Builder measureSieve(Database database,
			Measure measure, Progress progress, long segments,
			long segmentSize, Sieve.Descriptor sieveDescriptor,
			long startSegment, boolean sum) throws Throwable {
		progress.progress(0.0);
		Sieve sieve=sieveDescriptor.factory.get();
		sieve.reset(
				database,
				progress.subProgress(0.0, "init", 0.05),
				segmentSize,
				startSegment*segmentSize+1l);
		SieveTable table=new LongTable();
		table.clear(sieve.defaultPrime());
		Sample.Builder sample=Sample.builder((int)segments);
		boolean time=Measure.NANOSECS.equals(measure);
		OperationCounter counter
				=time?OperationCounter.NOOP:OperationCounter.COUNTER;
		counter.reset();
		long sieveTime=0l;
		Progress subProgress=progress.subProgress(0.05, "sieve", 1.0);
		for (long ss=0; segments>ss; ++ss) {
			subProgress.progress(1.0*ss/segments);
			long start=(startSegment+ss)*segmentSize+1l;
			long end=start+segmentSize;
			long startTime=System.nanoTime();
			sieve.sieve(counter, table);
			long endTime=System.nanoTime();
			long measure2;
			if (time) {
				if (sum) {
					sieveTime+=endTime-startTime;
					measure2=sieveTime;
				}
				else {
					measure2=endTime-startTime;
				}
			}
			else {
				measure2=counter.get();
				if (!sum) {
					counter.reset();
				}
			}
			sample.add(end, measure2);
		}
		subProgress.finished();
		return sample;
	}
	
	private void segmentSizeChanged(ChangeEvent event) {
		long value=UnsignedLong.unsignedInt((Integer)segmentSize.getValue());
		segmentSizeEditor.setText(String.format(
				"%1$s = 2^%2$2s",
				UnsignedLong.format(1l<<value),
				UnsignedLong.format(value)));
	}
	
	private void sieveChanged(ActionEvent event) throws Throwable {
		Sieve.Descriptor sieveDescriptor
				=Sieves.SIEVES.get(sieve.getSelectedIndex());
		segmentSize.setModel(new SpinnerNumberModel(
				sieveDescriptor.smallSegmentSizeSuggestedLog2,
				sieveDescriptor.smallSegmentSizeMinLog2,
				sieveDescriptor.smallSegmentSizeMaxLog2,
				1));
		segmentSizeChanged(null);
	}
	
	@Override
	public JDialog window() {
		return dialog;
	}
}
