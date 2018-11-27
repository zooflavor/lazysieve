package gui.plotter;

import gui.Command;
import gui.graph.PlotType;
import gui.graph.Sample;
import gui.io.Database;
import gui.io.Segment;
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
import gui.ui.progress.PrintStreamProgress;
import gui.ui.progress.Progress;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;

public class MeasureSieve extends GuiWindow<JDialog> {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("measure"),
									Command.Argument.constant("sieve"),
									Command.Argument.PATH,
									Command.Argument.STRING,
									Command.Argument.LONG,
									Command.Argument.LONG,
									Command.Argument.LONG,
									Command.Argument.LONG,
									Command.Argument.LONG,
									Command.Argument.STRING,
									Command.Argument.STRING,
									Command.Argument.PATH),
							MeasureSieve::measureSieve1,
							"Main measure sieve [adatbázis könyvtár] [szita] [kezdet] [vég] [szegmens méret] [mérések száma] [minták száma] [nanosecs|operations] [segment|sum] [kimenet fájl]",
							null),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("measure"),
									Command.Argument.constant("sieve"),
									Command.Argument.PATH,
									Command.Argument.STRING,
									Command.Argument.LONG,
									Command.Argument.LONG,
									Command.Argument.LONG,
									Command.Argument.LONG,
									Command.Argument.LONG,
									Command.Argument.PATH,
									Command.Argument.PATH,
									Command.Argument.PATH,
									Command.Argument.PATH),
							MeasureSieve::measureSieveAll,
							"Main measure sieve [adatbázis könyvtár] [szita] [kezdet] [vég] [szegmens méret] [mérések száma] [minták száma] [szeg-ns fájl] [szum-ops fájl] [szeg-ns fájl] [szum-ops fájl]",
							null)));
    public static final long MAX_MEASUREMENTS=100l;
    public static final long MAX_SAMPLES=100000l;
	public static final String TITLE="Szita mérése";
	
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
	
	public static class SegmentsMeasure {
		public final long measurements;
		public final long[] nanosecs;
		public final long[] operations;
		public final long[] xs;
		
		public SegmentsMeasure(long measurements, long[] nanosecs,
				long[] operations, long[] xs) {
			this.measurements=measurements;
			this.nanosecs=nanosecs;
			this.operations=operations;
			this.xs=xs;
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
	private final UnsignedLongSpinner endSpinner;
	private final JComboBox<Measure> measure;
	private final UnsignedLongSpinner measurements;
	private final Plotter plotter;
	private final UnsignedLongSpinner samples;
	private final JSpinner segmentSize;
	private final JLabel segmentSizeEditor;
	private final JComboBox<Sieve.Descriptor> sieve;
	private final UnsignedLongSpinner startSpinner;
	private final JCheckBox sum;
	
	public MeasureSieve(Plotter plotter) throws Throwable {
		super(plotter.session);
		this.plotter=plotter;
		
		dialog=new JDialog(plotter.window(), TITLE);
		dialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel buttons=new JPanel(new FlowLayout(FlowLayout.CENTER));
		dialog.getContentPane().add(buttons, BorderLayout.NORTH);
		
		JButton measureButton=new JButton("Mérés");
		measureButton.setMnemonic('m');
		measureButton.addActionListener(actionListener(this::measureButton));
		buttons.add(measureButton);
		
		buttons.add(CloseButton.create(dialog));
		
		JPanel panel=new JPanel();
        SpringLayout layout=new SpringLayout();
		panel.setLayout(layout);
		dialog.getContentPane().add(panel, BorderLayout.CENTER);
		
		sieve=new JComboBox<>(new SieveModel());
		sieve.addActionListener(actionListener(this::sieveChanged));
		panel.add(sieve);
		
		JLabel startLabel=new JLabel("Kezdőszám:");
		panel.add(startLabel);
		startSpinner=new UnsignedLongSpinner(
				Segment.END_NUMBER, 3l, Segment.GENERATOR_START_NUMBER, 2l);
		panel.add(startSpinner);
		
		JLabel endLabel=new JLabel("Végszám:");
		panel.add(endLabel);
		endSpinner=new UnsignedLongSpinner(
				Segment.END_NUMBER, 3l, 1l<<34, 2l);
		panel.add(endSpinner);
		
		startSpinner.addListener(
				new UnsignedLongSpinner.StartListener(endSpinner));
		endSpinner.addListener(
				new UnsignedLongSpinner.EndListener(startSpinner));
		
		JLabel segmentSizeLabel=new JLabel("Szegmens méret:");
		panel.add(segmentSizeLabel);
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
		panel.add(segmentSize);
        
        JLabel samplesLabel=new JLabel("Minták száma:");
        panel.add(samplesLabel);
        samples=new UnsignedLongSpinner(MAX_SAMPLES, 1l, 1000l, 1l);
        panel.add(samples);
        
        JLabel measurementsLabel=new JLabel("Mérések száma:");
        panel.add(measurementsLabel);
        measurements=new UnsignedLongSpinner(MAX_MEASUREMENTS, 1l, 3l, 1l);
        panel.add(measurements);
		
		JLabel measureLabel=new JLabel("Mérték:");
        panel.add(measureLabel);
        measure=new JComboBox<>(new MeasureModel());
		panel.add(measure);
		
		sum=new JCheckBox("Összesítve", true);
		panel.add(sum);
        
        layout.putConstraint(SpringLayout.NORTH, sieve,
                5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, startSpinner,
                5, SpringLayout.SOUTH, sieve);
        layout.putConstraint(SpringLayout.NORTH, endSpinner,
                5, SpringLayout.SOUTH, startSpinner);
        layout.putConstraint(SpringLayout.NORTH, segmentSize,
                5, SpringLayout.SOUTH, endSpinner);
        layout.putConstraint(SpringLayout.NORTH, samples,
                5, SpringLayout.SOUTH, segmentSize);
        layout.putConstraint(SpringLayout.NORTH, measurements,
                5, SpringLayout.SOUTH, samples);
        layout.putConstraint(SpringLayout.NORTH, measure,
                5, SpringLayout.SOUTH, measurements);
        layout.putConstraint(SpringLayout.NORTH, sum,
                5, SpringLayout.SOUTH, measure);
        layout.putConstraint(SpringLayout.SOUTH, panel,
                5, SpringLayout.SOUTH, sum);
        layout.putConstraint(SpringLayout.WEST, sieve,
                5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, panel,
                5, SpringLayout.EAST, sieve);
        layout.putConstraint(SpringLayout.EAST, startSpinner,
                -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.EAST, endSpinner,
                -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.WEST, segmentSize,
                0, SpringLayout.WEST, endSpinner);
        layout.putConstraint(SpringLayout.EAST, samples,
                -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.EAST, measurements,
                -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.WEST, measure,
                0, SpringLayout.WEST, endSpinner);
        layout.putConstraint(SpringLayout.WEST, sum,
                0, SpringLayout.WEST, endSpinner);
        layout.putConstraint(SpringLayout.BASELINE, startLabel,
                0, SpringLayout.BASELINE, startSpinner);
        layout.putConstraint(SpringLayout.BASELINE, endLabel,
                0, SpringLayout.BASELINE, endSpinner);
        layout.putConstraint(SpringLayout.BASELINE, segmentSizeLabel,
                0, SpringLayout.BASELINE, segmentSize);
        layout.putConstraint(SpringLayout.BASELINE, samplesLabel,
                0, SpringLayout.BASELINE, samples);
        layout.putConstraint(SpringLayout.BASELINE, measurementsLabel,
                0, SpringLayout.BASELINE, measurements);
        layout.putConstraint(SpringLayout.BASELINE, measureLabel,
                0, SpringLayout.BASELINE, measure);
        layout.putConstraint(SpringLayout.EAST, startLabel,
                -5, SpringLayout.WEST, startSpinner);
        layout.putConstraint(SpringLayout.EAST, endLabel,
                -5, SpringLayout.WEST, endSpinner);
        layout.putConstraint(SpringLayout.EAST, segmentSizeLabel,
                -5, SpringLayout.WEST, segmentSize);
        layout.putConstraint(SpringLayout.EAST, samplesLabel,
                -5, SpringLayout.WEST, samples);
        layout.putConstraint(SpringLayout.EAST, measurementsLabel,
                -5, SpringLayout.WEST, measurements);
        layout.putConstraint(SpringLayout.EAST, measureLabel,
                -5, SpringLayout.WEST, measure);
		
		sieveChanged(null);
		
		dialog.pack();
	}
	
	private void measureButton(ActionEvent event) throws Throwable {
		long end2=endSpinner.getNumber();
        long measurements2=measurements.getNumber();
        long samples2=samples.getNumber();
		long segmentSize2=1l<<((Integer)segmentSize.getValue());
		Sieve.Descriptor sieve2=Sieves.SIEVES.get(sieve.getSelectedIndex());
		long start2=startSpinner.getNumber();
		Measure measure2=Measure.values()[measure.getSelectedIndex()];
		boolean sum2=sum.isSelected();
		new AddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress)
					throws Throwable {
				return MeasureSieve.measureSieve(session.database, end2,
								measure2, measurements2, progress, samples2,
                                segmentSize2, sieve2, start2, sum2)
						.create(sieve2.longName+"-"+measure2
										+"-2^"+(63-Long.numberOfLeadingZeros(
												segmentSize2))
										+"-("+start2
										+", "+end2
										+")-"+(sum2?"összesen":"szegmens"),
								Colors.INTERPOLATION,
								PlotType.LINE,
								color);
			}
		}.start(session.executor);
		dialog.dispose();
	}
	
	public static Sample.Builder measureSieve(Database database, long end,
			Measure measure, long measurements, Progress progress,
            long samples, long segmentSize, Sieve.Descriptor sieveDescriptor,
            long start, boolean sum) throws Throwable {
		SegmentsMeasure result=measureSieveSegments(database, end,
				measurements, progress, samples, segmentSize, sieveDescriptor,
				start);
		long[] segments;
		switch (measure) {
			case NANOSECS:
				segments=result.nanosecs;
				break;
			case OPERATIONS:
				segments=result.operations;
				break;
			default:
				throw new IllegalStateException(String.format(
						"ismeretlen mérték %1$s", measure));
		}
		if (sum) {
			sum(segments);
		}
		return sample(result.measurements, result.xs, segments);
	}
    
    public static void measureSieve1(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(2));
		Sieve.Descriptor sieveDescriptor
				=Sieves.parse((String)arguments.get(3));
		Long start=(Long)arguments.get(4);
		Long end=(Long)arguments.get(5);
		Long segmentSize=(Long)arguments.get(6);
		Long measurements=(Long)arguments.get(7);
		Long samples=(Long)arguments.get(8);
        Measure measure;
        switch ((String)arguments.get(9)) {
            case "nanosecs":
                measure=Measure.NANOSECS;
                break;
            case "operations":
                measure=Measure.OPERATIONS;
                break;
            default:
                throw new IllegalArgumentException(String.format(
                        "ismeretlen mérték %1$s",
                        arguments.get(9)));
        }
        boolean sum;
        switch ((String)arguments.get(10)) {
            case "segment":
                sum=false;
                break;
            case "sum":
                sum=true;
                break;
            default:
                throw new IllegalArgumentException(String.format(
                        "ismeretlen összesítés %1$s",
                        arguments.get(10)));
        }
        Path outputPath=(Path)arguments.get(11);
        Progress progress=new PrintStreamProgress(false, System.out);
        Sample sample=measureSieve(database, end, measure, measurements,
                        progress.subProgress(0.0, "mérés", 0.99),
                        samples, segmentSize, sieveDescriptor, start, sum)
                .create("", Color.BLACK, PlotType.LINE, Color.WHITE);
        SaveSampleProcess.save(outputPath,
                progress.subProgress(0.99, "mentés", 1.0),
                sample);
        progress.finished();
    }
    
    public static void measureSieveAll(List<Object> arguments)
			throws Throwable {
		Database database=new Database((Path)arguments.get(2));
		Sieve.Descriptor sieveDescriptor
				=Sieves.parse((String)arguments.get(3));
		Long start=(Long)arguments.get(4);
		Long end=(Long)arguments.get(5);
		Long segmentSize=(Long)arguments.get(6);
		Long measurements=(Long)arguments.get(7);
		Long samples=(Long)arguments.get(8);
        Path outputPathSegNss=(Path)arguments.get(9);
        Path outputPathSegOps=(Path)arguments.get(10);
        Path outputPathSumNss=(Path)arguments.get(11);
        Path outputPathSumOps=(Path)arguments.get(12);
        Progress progress=new PrintStreamProgress(false, System.out);
        SegmentsMeasure result=measureSieveSegments(
				database, end, measurements,
				progress.subProgress(0.0, "mérés", 0.96),
					samples, segmentSize, sieveDescriptor, start);
        SaveSampleProcess.save(outputPathSegNss,
                progress.subProgress(0.96, "mentés", 0.97),
                sample(result.measurements, result.xs, result.nanosecs)
		                .create("", Color.BLACK, PlotType.LINE, Color.WHITE));
        SaveSampleProcess.save(outputPathSegOps,
                progress.subProgress(0.97, "mentés", 0.98),
                sample(result.measurements, result.xs, result.operations)
		                .create("", Color.BLACK, PlotType.LINE, Color.WHITE));
		sum(result.nanosecs);
		sum(result.operations);
        SaveSampleProcess.save(outputPathSumNss,
                progress.subProgress(0.98, "mentés", 0.99),
                sample(result.measurements, result.xs, result.nanosecs)
		                .create("", Color.BLACK, PlotType.LINE, Color.WHITE));
        SaveSampleProcess.save(outputPathSumOps,
                progress.subProgress(0.99, "mentés", 1.0),
                sample(result.measurements, result.xs, result.operations)
		                .create("", Color.BLACK, PlotType.LINE, Color.WHITE));
        progress.finished();
    }
	
	public static SegmentsMeasure measureSieveSegments(
			Database database, long end, long measurements, Progress progress,
            long samples, long segmentSize, Sieve.Descriptor sieveDescriptor,
            long start) throws Throwable {
		progress.progress(0.0);
		if (0<=Long.compareUnsigned(start, end)) {
			throw new IllegalArgumentException("üres intervallum");
		}
		if (0>Long.compareUnsigned(Segment.END_NUMBER, end)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s végszám nagyobb, mint a maximum %2$s",
					UnsignedLong.format(end),
					UnsignedLong.format(Segment.END_NUMBER)));
		}
		if (0<Long.compareUnsigned(1l, start)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s kezdő szám kisebb, mint a minimum 1",
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
        if (0<Long.compareUnsigned(1l, measurements)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s mérés kisebb, mint a minimum 1",
					UnsignedLong.format(measurements)));
        }
        if (0>Long.compareUnsigned(MAX_MEASUREMENTS, measurements)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s mérés nagyobb, mint a maxumim %2$s",
					UnsignedLong.format(measurements),
                    UnsignedLong.format(MAX_MEASUREMENTS)));
        }
        if (0<Long.compareUnsigned(1l, samples)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s minta kisebb, mint a minimum 1",
					UnsignedLong.format(samples)));
        }
        if (0>Long.compareUnsigned(MAX_SAMPLES, samples)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s minta nagyobb, mint a maxumim %2$s",
					UnsignedLong.format(samples),
                    UnsignedLong.format(MAX_SAMPLES)));
        }
        if (segmentSize!=Long.highestOneBit(segmentSize)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s szegmens méret nem kettő hatvány",
					UnsignedLong.format(segmentSize)));
        }
        if (0>Long.compareUnsigned(
                segmentSize, 1l<<sieveDescriptor.smallSegmentSizeMinLog2)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s szegmens méret kisebb, mint a minimum %2$s",
					UnsignedLong.format(segmentSize),
                    UnsignedLong.format(
                            1l<<sieveDescriptor.smallSegmentSizeMinLog2)));
        }
        if (0<Long.compareUnsigned(
                segmentSize, 1l<<sieveDescriptor.smallSegmentSizeMaxLog2)) {
			throw new IllegalArgumentException(String.format(
					"a %1$s szegmens méret nagyobb, mint a maximum %2$s",
					UnsignedLong.format(segmentSize),
                    UnsignedLong.format(
                            1l<<sieveDescriptor.smallSegmentSizeMaxLog2)));
        }
		long startSegment=Long.divideUnsigned(start-1l, segmentSize);
		long endSegment=Long.divideUnsigned(end-1l, segmentSize);
		if (0<Long.compareUnsigned(end, endSegment*segmentSize+1l)) {
			++endSegment;
		}
		long segments=endSegment-startSegment;
        if (0<Long.compareUnsigned(samples, segments)) {
            samples=segments;
        }
        long[] sampleXs=new long[(int)samples];
        long[] sampleNanosecs=new long[sampleXs.length];
        long[] sampleOperations=new long[sampleXs.length];
        long segmentsPerSample=Long.divideUnsigned(segments, samples);
        long lastSegment=endSegment;
        for (int ii=sampleXs.length-1;
                0<=ii;
                --ii, lastSegment-=segmentsPerSample) {
            sampleXs[ii]=lastSegment*segmentSize+1l;
        }
        
        Sieve sieve=sieveDescriptor.factory.get();
        SieveTable table=new LongTable();
        OperationCounter counter=OperationCounter.COUNTER;
        for (long mm=0; measurements>mm; ++mm) {
            Progress subProgress=progress.subProgress(
                    1.0*mm/measurements,
                    null,
                    1.0*(mm+1l)/measurements);
            sieve.reset(
                    database.largePrimes(),
                    subProgress.subProgress(0.0, "init", 0.05),
                    segmentSize,
                    startSegment*segmentSize+1l);
            table.clear(sieve.defaultPrime());
            Progress subProgress2
					=subProgress.subProgress(0.05, "szitálás", 1.0);
            for (int ss=0; sampleXs.length>ss; ++ss) {
				counter.reset();
				long nanosecs=0l;
                while (0<Long.compareUnsigned(sampleXs[ss], sieve.start())) {
                    subProgress2.progress(
                            1.0*(sieve.start()-start)/(end-start));
                    long startTime=System.nanoTime();
                    sieve.sieve(counter, table);
                    long endTime=System.nanoTime();
                    nanosecs+=endTime-startTime;
                }
                sampleNanosecs[ss]+=nanosecs;
                sampleOperations[ss]+=counter.get();
            }
            subProgress.finished();
        }
		progress.finished();
		return new SegmentsMeasure(
				measurements, sampleNanosecs, sampleOperations, sampleXs);
	}
	
	private static Sample.Builder sample(long measurements, long[] xs,
			long[] ys) {
		Sample.Builder sample=Sample.builder(xs.length);
		for (int ii=0; xs.length>ii; ++ii) {
			sample.add(xs[ii], 1.0*ys[ii]/measurements);
		}
		return sample;
	}
	
	private void segmentSizeChanged(ChangeEvent event) {
		long value=UnsignedLong.unsignedInt((Integer)segmentSize.getValue());
		segmentSizeEditor.setText(String.format(
				"%1$s = 2^%2$2s",
				UnsignedLong.format(1l<<value),
				UnsignedLong.format(value)));
	}
	
	private static void sum(long[] values) {
		long sum=0l;
		for (int ii=0; values.length>ii; ++ii) {
			sum+=values[ii];
			values[ii]=sum;
		}
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
