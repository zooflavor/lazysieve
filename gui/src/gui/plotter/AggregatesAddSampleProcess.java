package gui.plotter;

import gui.graph.Sample2D;
import gui.io.Aggregates;
import gui.ui.Color;
import gui.ui.progress.Progress;
import gui.util.Maps;

public abstract class AggregatesAddSampleProcess extends AddSampleProcess {
	public AggregatesAddSampleProcess(Plotter parent) {
		super(parent);
	}
	
	public static void addPrime12Z11CountsSample(Plotter plotter) {
		new AggregatesAddSampleProcess(plotter) {
			@Override
			protected Sample2D sample(Aggregates aggregates, Color color,
					Object id) throws Throwable {
				return new Sample2D(
						id,
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
			protected Sample2D sample(Aggregates aggregates, Color color,
					Object id) throws Throwable {
				return new Sample2D(
						id,
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
			protected Sample2D sample(Aggregates aggregates, Color color,
					Object id) throws Throwable {
				return new Sample2D(
						id,
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
			protected Sample2D sample(Aggregates aggregates, Color color,
					Object id) throws Throwable {
				return new Sample2D(
						id,
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
			protected Sample2D sample(Aggregates aggregates, Color color,
					Object id) throws Throwable {
				return new Sample2D(
						id,
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
			protected Sample2D sample(Aggregates aggregates, Color color,
					Object id) throws Throwable {
				return new Sample2D(
						id,
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
			protected Sample2D sample(Aggregates aggregates, Color color,
					Object id) throws Throwable {
				return new Sample2D(
						id,
						"sieve nanos",
						Colors.INTERPOLATION,
						color,
						Maps.toDouble(aggregates.sieveNanos()),
						color);
			}
		}.start(plotter.gui.executor);
	}

	@Override
	protected Sample2D sample(Color color, Object id, Progress progress)
			throws Throwable {
		return sample(parent.gui.database.readAggregates(progress), color, id);
	}
	
	protected abstract Sample2D sample(Aggregates aggregates, Color color,
			Object id) throws Throwable;
}
