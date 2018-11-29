package gui.graph.plotter;

import gui.graph.PlotType;
import gui.graph.Sample;
import gui.io.Aggregates;
import gui.io.AggregatesReader;
import gui.ui.Color;
import gui.ui.progress.Progress;
import java.util.Map;
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
				return toSample(Aggregates.maxPrimeGaps(progress, reader))
						.create("Legnagyobb prímhézag",
								Color.INTERPOLATION,
								PlotType.LINE,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addNewPrimeGapsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return toSample(Aggregates.newPrimeGaps(progress, reader))
						.create("Prímhézagok első előfordulása^(-1)",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create("12Z+11 prímek száma",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create("4Z+1 prímek száma",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create("4Z+3 prímek száma",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create("6Z+1 prímek száma",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create("Prímszámtétel abszolút hiba",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create("Prímszámtétel",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create("Prímszámtétel relatív hiba",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create("Prímek száma",
								Color.INTERPOLATION,
								PlotType.LINE,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeGapFrequenciesSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return toSample(
							Aggregates.primeGapFrequencies(progress, reader))
						.create("Prímhézagok gyakoriság",
								Color.INTERPOLATION,
								PlotType.BARS,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeGapMeritsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return toSample(Aggregates.primeGapMerits(progress, reader))
						.create("Prímhézagok jósága",
								Color.INTERPOLATION,
								PlotType.LINE,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeGapStartsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return toSample(Aggregates.primeGapStarts(progress, reader))
						.create("Prímhézagok első előfordulása",
								Color.INTERPOLATION,
								PlotType.LINE,
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
						.create(String.format("Szitálás ideje (ns)(%1$s)",
										sum?"összesen":"szegmensenként"),
								Color.INTERPOLATION,
								PlotType.LINE,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addTwinPrimes(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Color color, Progress progress,
					AggregatesReader reader) throws Throwable {
				return Aggregates.twinPrimes(progress, reader)
						.create("Ikerprímek száma",
								Color.INTERPOLATION,
								PlotType.LINE,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static JMenuItem menu(Plotter plotter) {
		JMenu menu=new JMenu("Prímstatisztikák");
		
        JMenuItem addPrime12Z11CountsItem
                =new JMenuItem("12Z+11 prímek száma");
		addPrime12Z11CountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrime12Z11CountsSample(plotter));
		menu.add(addPrime12Z11CountsItem);
		
		JMenuItem addPrime4Z1CountsItem
				=new JMenuItem("4Z+1 prímek száma");
		addPrime4Z1CountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrime4Z1CountsSample(plotter));
		menu.add(addPrime4Z1CountsItem);
		
		JMenuItem addPrime4Z3CountsItem
				=new JMenuItem("4Z+3 prímek száma");
		addPrime4Z3CountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrime4Z3CountsSample(plotter));
		menu.add(addPrime4Z3CountsItem);
		
		JMenuItem addPrime6Z1CountsItem
				=new JMenuItem("6Z+1 prímek száma");
		addPrime6Z1CountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrime6Z1CountsSample(plotter));
		menu.add(addPrime6Z1CountsItem);
		
		JMenuItem addPrimeCountsItem
				=new JMenuItem("Prímek száma");
		addPrimeCountsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrimeCountsSample(plotter));
		menu.add(addPrimeCountsItem);
		
		JMenuItem addPrimeCountsExcpectedItem
				=new JMenuItem("Prímszámtétel");
		addPrimeCountsExcpectedItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addPrimeCountsExpectedSample(plotter));
		menu.add(addPrimeCountsExcpectedItem);
		
		JMenuItem addPrimeCountsAbsoluteErrorItem
				=new JMenuItem("Prímszámtétel abszolút hiba");
		addPrimeCountsAbsoluteErrorItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addPrimeCountsAbsoluteErrorSample(plotter));
		menu.add(addPrimeCountsAbsoluteErrorItem);
		
		JMenuItem addPrimeCountsRelativeErrorItem
				=new JMenuItem("Prímszámtétel relatív hiba");
		addPrimeCountsRelativeErrorItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addPrimeCountsRelativeErrorSample(plotter));
		menu.add(addPrimeCountsRelativeErrorItem);
		
		JMenuItem addPrimeGapFrequenciesItem
				=new JMenuItem("Prímhézagok gyakoriság");
		addPrimeGapFrequenciesItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addPrimeGapFrequenciesSample(plotter));
		menu.add(addPrimeGapFrequenciesItem);
		
		JMenuItem addPrimeGapMeritsItem
				=new JMenuItem("Prímhézagok jósága");
		addPrimeGapMeritsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrimeGapMeritsSample(plotter));
		menu.add(addPrimeGapMeritsItem);
		
		JMenuItem addPrimeGapStartsItem
				=new JMenuItem("Prímhézagok első előfordulása");
		addPrimeGapStartsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrimeGapStartsSample(plotter));
		menu.add(addPrimeGapStartsItem);
		
		JMenuItem addMaxPrimeGapsItem
				=new JMenuItem("Legnagyobb prímhézag");
		addMaxPrimeGapsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addMaxPrimeGapsSample(plotter));
		menu.add(addMaxPrimeGapsItem);
		
		JMenuItem addNewPrimeGapsItem
				=new JMenuItem("<html>Prímhézagok első előfordulása<sup>-1</sup></html>");
		addNewPrimeGapsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addNewPrimeGapsSample(plotter));
		menu.add(addNewPrimeGapsItem);
		
		JMenuItem addTwinPrimesItem
				=new JMenuItem("Ikerprímek száma");
		addTwinPrimesItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addTwinPrimes(plotter));
		menu.add(addTwinPrimesItem);
		
		JMenuItem addSieveNanosItem
				=new JMenuItem("Szitálás ideje (szegmensenként)");
		addSieveNanosItem.addActionListener((event2)->
				AggregatesAddSampleProcess
						.addSieveNanosSample(plotter, false));
		menu.add(addSieveNanosItem);
		
		JMenuItem addSieveNanosSumItem
				=new JMenuItem("Szitálás ideje (összesen)");
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
	
	private static Sample.Builder toSample(Map<Long, ? extends Number> map) {
		Sample.Builder result=Sample.builder(map.size());
		map.forEach((key, value)->
				result.add(key, value.doubleValue()));
		return result;
	}
}
