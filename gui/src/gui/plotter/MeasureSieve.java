package gui.plotter;

import gui.graph.Sample2D;
import gui.math.UnsignedLong;
import gui.sieve.Measure;
import gui.sieve.SieveMeasure;
import gui.sieve.SieveMeasureFactory;
import gui.sieve.Sieves;
import gui.ui.CloseButton;
import gui.ui.Color;
import gui.ui.GuiParent;
import gui.ui.SwingUtils;
import gui.ui.UnsignedLongSpinner;
import gui.ui.progress.Progress;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;

public class MeasureSieve implements GuiParent<JDialog> {
	public static final String TITLE="Measure sieve";
	
	private class MeasureModel implements ComboBoxModel<Measure> {
		private Measure selected=Measure.values()[0];
		
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
	
	private class SieveModel implements ComboBoxModel<SieveMeasureFactory> {
		private SieveMeasureFactory selected=Sieves.MEASURES.get(0);
		
		@Override
		public void addListDataListener(ListDataListener listener) {
		}
		
		@Override
		public SieveMeasureFactory getElementAt(int index) {
			return Sieves.MEASURES.get(index);
		}
		
		@Override
		public Object getSelectedItem() {
			return selected;
		}
		
		@Override
		public int getSize() {
			return Sieves.MEASURES.size();
		}
		
		@Override
		public void removeListDataListener(ListDataListener listener) {
		}
		
		@Override
		public void setSelectedItem(Object anItem) {
			selected=(SieveMeasureFactory)anItem;
		}
	}
	
	private final JDialog dialog;
	private final JComboBox<Measure> measure;
	private final Plotter plotter;
	private final UnsignedLongSpinner segments;
	private final JSpinner segmentSize;
	private final JLabel segmentSizeLabel;
	private final JComboBox<SieveMeasureFactory> sieve;
	private final UnsignedLongSpinner startSegment;
	
	public MeasureSieve(Plotter plotter) throws Throwable {
		this.plotter=plotter;
		
		dialog=new JDialog(plotter.component(), TITLE);
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
		segmentSizeLabel=new JLabel(" 9999999999 = 2^99");
		segmentSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		segmentSizeLabel.setFont(new Font(
				Font.MONOSPACED,
				Font.PLAIN,
				segmentSizeLabel.getFont().getSize()));
		segmentSizeLabel.setPreferredSize(segmentSizeLabel.getPreferredSize());
		segmentSize=new JSpinner();
		segmentSize.setEditor(segmentSizeLabel);
		segmentSize.addChangeListener(this::segmentSizeChanged);
		segmentSizePanel.add(segmentSize);
		
		JPanel startSegmentPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(startSegmentPanel);
		startSegment=new UnsignedLongSpinner(1l<<30, 0l, 0l, 1l);
		startSegmentPanel.add(startSegment);
		
		JPanel segmentsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(segmentsPanel);
		segments=new UnsignedLongSpinner(1l<<30, 1l, 100l, 1l);
		segmentsPanel.add(segments);
		
		JPanel measurePanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(measurePanel);
		measure=new JComboBox<>(new MeasureModel());
		measurePanel.add(measure);
		
		sieveChanged(null);
		
		dialog.pack();
	}
	
	@Override
	public JDialog component() {
		return dialog;
	}
	
	private void measureButton(ActionEvent event) throws Throwable {
		SieveMeasureFactory sieveFactory
				=Sieves.MEASURES.get(sieve.getSelectedIndex());
		long segmentSize2=1l<<((Integer)segmentSize.getValue());
		long segments2=segments.getNumber();
		long startSegment2=startSegment.getNumber();
		Measure measure2=Measure.values()[measure.getSelectedIndex()];
		Color color=plotter.selectNewColor();
		SieveMeasure sieve2=sieveFactory.create(color, measure2,
				plotter.gui.database, segments2, segmentSize2, startSegment2);
		new AddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Color color, Progress progress)
					throws Throwable {
				return sieve2.measure(progress);
			}
		}.start(plotter.gui.executor);
		dialog.dispose();
	}
	
	private void segmentSizeChanged(ChangeEvent event) {
		long value=UnsignedLong.unsignedInt((Integer)segmentSize.getValue());
		segmentSizeLabel.setText(String.format(
				"%1$s = 2^%2$2s",
				UnsignedLong.format(1l<<value),
				UnsignedLong.format(value)));
	}
	
	public void show() {
		SwingUtils.show(dialog);
	}
	
	private void sieveChanged(ActionEvent event) throws Throwable {
		SieveMeasureFactory factory
				=Sieves.MEASURES.get(sieve.getSelectedIndex());
		segmentSize.setModel(new SpinnerNumberModel(
				factory.smallSegmentSizeSuggestedLog2(),
				factory.smallSegmentSizeMinLog2(),
				factory.smallSegmentSizeMaxLog2(),
				1));
		segmentSizeChanged(null);
	}
}
