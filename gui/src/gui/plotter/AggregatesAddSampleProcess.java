package gui.plotter;

import gui.graph.PlotType;
import gui.graph.Sample;
import gui.io.Aggregates;
import gui.ui.Color;
import gui.ui.progress.Progress;
import gui.util.Maps;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public abstract class AggregatesAddSampleProcess extends AddSampleProcess {
	public AggregatesAddSampleProcess(Plotter parent) {
		super(parent);
	}
	
	public static void addPrime12Z11CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				return Maps.toSample(aggregates.prime12Z11Counts())
						.create(new Object(),
								"prime 12Z+11 counts",
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
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				return Maps.toSample(aggregates.prime4Z1Counts())
						.create(new Object(),
								"prime 4Z+1 counts",
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
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				return Maps.toSample(aggregates.prime4Z3Counts())
						.create(new Object(),
								"prime 4Z+3 counts",
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
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				return Maps.toSample(aggregates.prime6Z1Counts())
						.create(new Object(),
								"prime 6Z+1 counts",
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
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				NavigableMap<Long, Long> primeCounts=aggregates.primeCounts();
				Sample.Builder sample=Sample.builder(primeCounts.size());
				primeCounts.forEach(
						(key, value)->{
							if (Math.E>=key) {
								return;
							}
							sample.add(
									key,
									value-key/Math.log(key));
						});
				return sample.create(
						new Object(),
						"prime counts abs. error",
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
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				NavigableMap<Long, Long> primeCounts=aggregates.primeCounts();
				Sample.Builder sample=Sample.builder(primeCounts.size());
				primeCounts.forEach(
						(key, value)->{
							if (Math.E>=key) {
								return;
							}
							sample.add(
									key,
									key/Math.log(key));
						});
				return sample.create(
						new Object(),
						"expected prime counts",
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
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				NavigableMap<Long, Long> primeCounts=aggregates.primeCounts();
				Sample.Builder sample=Sample.builder(primeCounts.size());
				primeCounts.forEach(
						(key, value)->{
							if (Math.E>=key) {
								return;
							}
							sample.add(
									key,
									(value-key/Math.log(key))/value);
						});
				return sample.create(
						new Object(),
						"prime counts rel. error",
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
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				return Maps.toSample(aggregates.primeCounts())
						.create(new Object(),
								"prime counts",
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
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				return Maps.toSample(
						aggregates.aggregates
								.lastEntry()
								.getValue()
								.primeGapFrequencies)
						.create(new Object(),
								"prime gap distribution",
								Colors.INTERPOLATION,
								PlotType.BARS,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addPrimeGapStartsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				return Maps.toSample(aggregates.primeGapStarts())
						.create(new Object(),
								"prime gap starts",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addMaxPrimeGapsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				NavigableMap<Long, Long> primeGapStarts
						=aggregates.primeGapStarts();
				NavigableMap<Long, Long> maxPrimeGaps=new TreeMap<>();
				primeGapStarts.forEach(
						(key, value)->maxPrimeGaps.put(value, key));
				Long max=0l;
                Map<Long, Long> counterPoints=new HashMap<>();
                for (Iterator<Map.Entry<Long, Long>> iterator
                                =maxPrimeGaps.entrySet().iterator();
                        iterator.hasNext(); ) {
                    Map.Entry<Long, Long> entry=iterator.next();
                    Long gap=entry.getValue();
                    if (gap>max) {
                        if (0l<max) {
                            counterPoints.put(entry.getKey()-1l, max);
                        }
                        max=gap;
                    }
                    else {
                        iterator.remove();
                    }
                }
                maxPrimeGaps.putAll(counterPoints);
				return Maps.toSample(maxPrimeGaps)
						.create(new Object(),
								"max. prime gaps",
								Colors.INTERPOLATION,
								PlotType.LINE,
								color,
								color);
			}
		}.start(plotter.session.executor);
	}
	
	public static void addSieveNanosSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample sample(Aggregates aggregates, Color color)
					throws Throwable {
				return Maps.toSample(aggregates.sieveNanos())
						.create(new Object(),
								"sieve nanos",
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
		
		JMenuItem addSieveNanosItem
				=new JMenuItem("Add sieve nanos sample");
		addSieveNanosItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addSieveNanosSample(plotter));
		menu.add(addSieveNanosItem);
		
		return menu;
	}

	@Override
	protected Sample sample(Color color, Progress progress)
			throws Throwable {
		return sample(parent.session.database.readAggregates(progress), color);
	}
	
	protected abstract Sample sample(Aggregates aggregates, Color color)
			throws Throwable;
}
