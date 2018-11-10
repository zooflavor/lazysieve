package gui.plotter;

import gui.graph.PlotType;
import gui.graph.Sample;
import gui.io.Aggregates;
import gui.io.AggregatesReader;
import gui.ui.Color;
import gui.ui.progress.Progress;
import gui.util.Maps;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public abstract class AggregatesAddSampleProcess extends AddSampleProcess {
	public AggregatesAddSampleProcess(Plotter parent) {
		super(parent);
	}
	
	public static void addMaxPrimeGapsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Maps.toSample(
							Aggregates.maxPrimeGaps(progress, reader))
						.create("max. prime gaps",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addNewPrimeGapsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Maps.toSample(
							Aggregates.newPrimeGaps(progress, reader))
						.create("new prime gaps",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrime12Z11CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.prime12Z11Counts(progress, reader)
						.create("prime 12Z+11 counts",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrime4Z1CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.prime4Z1Counts(progress, reader)
						.create("prime 4Z+1 counts",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrime4Z3CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.prime4Z3Counts(progress, reader)
						.create("prime 4Z+3 counts",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrime6Z1CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.prime6Z1Counts(progress, reader)
						.create("prime 6Z+1 counts",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeCountsAbsoluteErrorSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.primeCountsAbsoluteError(progress, reader)
						.create("prime counts abs. error",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeCountsExpectedSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.primeCountsExpectedValue(progress, reader)
						.create("expected prime counts",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeCountsRelativeErrorSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.primeCountsRelativeError(progress, reader)
						.create("prime counts rel. error",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeCountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.primeCounts(progress, reader)
						.create("prime counts",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeGapFrequenciesSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Maps.toSample(
							Aggregates.primeGapFrequencies(progress, reader))
						.create("prime gap frequencies",
								Colors.INTERPOLATION,
								PlotType.BARS,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeGapMeritsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Maps.toSample(
							Aggregates.primeGapMerits(progress, reader))
						.create("prime gap merits",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeGapStartsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Maps.toSample(
							Aggregates.primeGapStarts(progress, reader))
						.create("prime gap starts",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	public static void addSieveNanosSample(Plotter plotter, boolean sum) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.sieveNanos(progress, reader, sum)
						.create(String.format("sieve nanos (%1$s)",
										sum?"sum":"segments"),
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static JMenuItem menu(Plotter plotter) {
		JMenu menu=new JMenu("Aggregates");
		
        JMenuItem addPrime12Z11CountsItem
                =new JMenuItem("Add prime 12Z11 counts");
		addPrime12Z11CountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrime12Z11CountsSample(plotter));
		menu.add(addPrime12Z11CountsItem);
		
		JMenuItem addPrime4Z1CountsItem
				=new JMenuItem("Add prime 4Z1 counts sample");
		addPrime4Z1CountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrime4Z1CountsSample(plotter));
		menu.add(addPrime4Z1CountsItem);
		
		JMenuItem addPrime4Z3CountsItem
				=new JMenuItem("Add prime 4Z3 counts sample");
		addPrime4Z3CountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrime4Z3CountsSample(plotter));
		menu.add(addPrime4Z3CountsItem);
		
		JMenuItem addPrime6Z1CountsItem
				=new JMenuItem("Add prime 6Z1 counts sample");
		addPrime6Z1CountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrime6Z1CountsSample(plotter));
		menu.add(addPrime6Z1CountsItem);
		
		JMenuItem addPrimeCountsItem
				=new JMenuItem("Add prime counts sample");
		addPrimeCountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrimeCountsSample(plotter));
		menu.add(addPrimeCountsItem);
		
		JMenuItem addPrimeCountsExcpectedItem
				=new JMenuItem("Add expected prime counts sample");
		addPrimeCountsExcpectedItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addPrimeCountsExpectedSample(plotter));
		menu.add(addPrimeCountsExcpectedItem);
		
		JMenuItem addPrimeCountsAbsoluteErrorItem
				=new JMenuItem("Add prime counts abs. error sample");
		addPrimeCountsAbsoluteErrorItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addPrimeCountsAbsoluteErrorSample(plotter));
		menu.add(addPrimeCountsAbsoluteErrorItem);
		
		JMenuItem addPrimeCountsRelativeErrorItem
				=new JMenuItem("Add prime counts rel. error sample");
		addPrimeCountsRelativeErrorItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addPrimeCountsRelativeErrorSample(plotter));
		menu.add(addPrimeCountsRelativeErrorItem);
		
		JMenuItem addPrimeGapFrequenciesItem
				=new JMenuItem("Add prime gap frequencies sample");
		addPrimeGapFrequenciesItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addPrimeGapFrequenciesSample(plotter));
		menu.add(addPrimeGapFrequenciesItem);
		
		JMenuItem addPrimeGapMeritsItem
				=new JMenuItem("Add prime gap merits sample");
		addPrimeGapMeritsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrimeGapMeritsSample(plotter));
		menu.add(addPrimeGapMeritsItem);
		
		JMenuItem addPrimeGapStartsItem
				=new JMenuItem("Add prime gap starts sample");
		addPrimeGapStartsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrimeGapStartsSample(plotter));
		menu.add(addPrimeGapStartsItem);
		
		JMenuItem addMaxPrimeGapsItem
				=new JMenuItem("Add max. prime gaps sample");
		addMaxPrimeGapsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addMaxPrimeGapsSample(plotter));
		menu.add(addMaxPrimeGapsItem);
		
		JMenuItem addNewPrimeGapsItem
				=new JMenuItem("Add new prime gaps sample");
		addNewPrimeGapsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addNewPrimeGapsSample(plotter));
		menu.add(addNewPrimeGapsItem);
		
		JMenuItem addSieveNanosItem
				=new JMenuItem("Add sieve nanos sample (segments)");
		addSieveNanosItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addSieveNanosSample(plotter, false));
		menu.add(addSieveNanosItem);
		
		JMenuItem addSieveNanosSumItem
				=new JMenuItem("Add sieve nanos sample (sum)");
		addSieveNanosSumItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addSieveNanosSample(plotter, true));
		menu.add(addSieveNanosSumItem);
		
		return menu;
	}

	@Override
	protected Sample sample(Color color, Progress progress)
			throws Throwable {
		try (AggregatesReader reader
				=parent.session.database.aggregatesReader()) {
			return sample(color, progress, reader);
		}
	}
	
	protected abstract Sample sample(Color color, Progress progress,
			AggregatesReader reader) throws Throwable;
}
