package gui.plotter;

import gui.graph.Sample2D;
import gui.io.Aggregates;
import gui.ui.Color;
import gui.ui.progress.Progress;
import gui.util.Maps;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public abstract class AggregatesAddSampleProcess extends AddSampleProcess {
	public AggregatesAddSampleProcess(Plotter parent) {
		super(parent);
	}
	
	public static void addPrime12Z11CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Aggregates aggregates, Color color)
					throws Throwable {
				return new Sample2D(
						new Object(),
						"prime 12Z+11 counts",
						Colors.INTERPOLATION,
						color,
						Maps.toDouble(aggregates.prime12Z11Counts()),
						color);
			}
		}.start(plotter.gui.executor);
	}
	
	public static void addPrime4Z1CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Aggregates aggregates, Color color)
					throws Throwable {
				return new Sample2D(
						new Object(),
						"prime 4Z+1 counts",
						Colors.INTERPOLATION,
						color,
						Maps.toDouble(aggregates.prime4Z1Counts()),
						color);
			}
		}.start(plotter.gui.executor);
	}
	
	public static void addPrime4Z3CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Aggregates aggregates, Color color)
					throws Throwable {
				return new Sample2D(
						new Object(),
						"prime 4Z+3 counts",
						Colors.INTERPOLATION,
						color,
						Maps.toDouble(aggregates.prime4Z3Counts()),
						color);
			}
		}.start(plotter.gui.executor);
	}
	
	public static void addPrime6Z1CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Aggregates aggregates, Color color)
					throws Throwable {
				return new Sample2D(
						new Object(),
						"prime 6Z+1 counts",
						Colors.INTERPOLATION,
						color,
						Maps.toDouble(aggregates.prime6Z1Counts()),
						color);
			}
		}.start(plotter.gui.executor);
	}
	
	public static void addPrimeCountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Aggregates aggregates, Color color)
					throws Throwable {
				return new Sample2D(
						new Object(),
						"prime counts",
						Colors.INTERPOLATION,
						color,
						Maps.toDouble(aggregates.primeCounts()),
						color);
			}
		}.start(plotter.gui.executor);
	}
	
	public static void addPrimeGapStartsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Aggregates aggregates, Color color)
					throws Throwable {
				return new Sample2D(
						new Object(),
						"prime gap starts",
						Colors.INTERPOLATION,
						color,
						Maps.toDouble(aggregates.primeGapStarts()),
						color);
			}
		}.start(plotter.gui.executor);
	}
	
	public static void addSieveNanosSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Aggregates aggregates, Color color)
					throws Throwable {
				return new Sample2D(
						new Object(),
						"sieve nanos",
						Colors.INTERPOLATION,
						color,
						Maps.toDouble(aggregates.sieveNanos()),
						color);
			}
		}.start(plotter.gui.executor);
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
		
		JMenuItem addPrimeGapStartsItem
				=new JMenuItem("Add prime gap starts sample");
		addPrimeGapStartsItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addPrimeGapStartsSample(plotter));
		menu.add(addPrimeGapStartsItem);
		
		JMenuItem addSieveNanosItem
				=new JMenuItem("Add sieve nanos sample");
		addSieveNanosItem.addActionListener((event2)->
				AggregatesAddSampleProcess.addSieveNanosSample(plotter));
		menu.add(addSieveNanosItem);
		
		return menu;
	}

	@Override
	protected Sample2D sample(Color color, Progress progress)
			throws Throwable {
		return sample(parent.gui.database.readAggregates(progress), color);
	}
	
	protected abstract Sample2D sample(Aggregates aggregates, Color color)
			throws Throwable;
}
