package gui.test;

import gui.Command;
import gui.io.CSVWriter;
import gui.math.Functions;
import gui.math.LeastSquares;
import gui.math.LinearCombinationFunction;
import gui.math.RealFunction;
import gui.math.Solver;
import gui.math.Sum;
import gui.ui.progress.PrintStreamProgress;
import gui.ui.progress.Progress;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class MeasureSums {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("test"),
									Command.Argument.constant("measure"),
									Command.Argument.constant("sums"),
									Command.Argument.PATH),
							MeasureSums::measureSums,
							"Main test measure sums [kimeneti fájl]",
							null)));

	public static void measureSums(List<Object> arguments) throws Throwable {
		boolean[] hashes=new boolean[]{false, true};
		List<Map.Entry<String, Solver>> solvers=Arrays.asList(
				new AbstractMap.SimpleEntry<>("1-GE-partial", Solver.gaussianElimination(false)),
				new AbstractMap.SimpleEntry<>("2-GE-total", Solver.gaussianElimination(true)),
				new AbstractMap.SimpleEntry<>("3-QR", Solver.qrDecomposition()));
		List<Map.Entry<String, Supplier<Sum>>> sums=Arrays.asList(
				new AbstractMap.SimpleEntry<>("2-array", Sum::array),
				new AbstractMap.SimpleEntry<>("3-priority", Sum::priority),
				new AbstractMap.SimpleEntry<>("1-simple", Sum::simple));
		List<List<RealFunction>> functions=Arrays.asList(
				Arrays.asList(Functions.ONE, Functions.X, Functions.X_LNLNX, Functions.X_LNX),
				Arrays.asList(Functions.ONE, Functions.X, Functions.X_LNLNX),
				Arrays.asList(Functions.ONE, Functions.X, Functions.X_LNX),
				Arrays.asList(Functions.ONE, Functions.X, Functions.LNX),
				Arrays.asList(Functions.ONE, Functions.X, Functions.LNLNX, Functions.LNX),
				Arrays.asList(Functions.ONE, Functions.X, Functions.LNLNX),
				Arrays.asList(Functions.ONE, Functions.X, Functions.SQRT_X),
				Arrays.asList(Functions.ONE, Functions.X, Functions.X2),
				Arrays.asList(Functions.ONE, Functions.X),
				Arrays.asList(Functions.ONE, Functions.X, Functions.X_PER_LNLNX),
				Arrays.asList(Functions.ONE, Functions.X, Functions.X_PER_LNX),
				Arrays.asList(Functions.ONE, Functions.X_LNLNX, Functions.X_LNX),
				Arrays.asList(Functions.ONE, Functions.X_LNLNX),
				Arrays.asList(Functions.ONE, Functions.X_LNX),
				Arrays.asList(Functions.ONE, Functions.LNX),
				Arrays.asList(Functions.ONE, Functions.LNLNX, Functions.LNX),
				Arrays.asList(Functions.ONE, Functions.LNLNX),
				Arrays.asList(Functions.ONE, Functions.SQRT_X),
				Arrays.asList(Functions.ONE, Functions.X_PER_LNLNX),
				Arrays.asList(Functions.ONE, Functions.X_PER_LNX));

		Progress progress0=new PrintStreamProgress(false, System.out);
		progress0.progress(0.0);
		try (CSVWriter writer=CSVWriter.open((Path)arguments.get(3))) {
			writer.write(Arrays.asList(
					"ordered", "SLE", "sum", "error", "time(ns)"));
			for (int hi=0; hashes.length>hi; ++hi) {
				boolean hash=hashes[hi];
				Progress progress1=progress0.subProgress(
						1.0*hi/hashes.length, null, (hi+1.0)/hashes.length);
				for (int pi=0; solvers.size()>pi; ++pi) {
					String solverName=solvers.get(pi).getKey();
					Solver solver=solvers.get(pi).getValue();
					Progress progress2=progress1.subProgress(
							1.0*pi/solvers.size(), null, (pi+1.0)/solvers.size());
					for (int si=0; sums.size()>si; ++si) {
						String sumName=sums.get(si).getKey();
						Supplier<Sum> sumFactory=sums.get(si).getValue();
						Progress progress3=progress2.subProgress(
								1.0*si/sums.size(), null, (si+1.0)/sums.size());
						Sum distanceSum=Sum.preferred();
						long timeSum=0l;
						for (int fi=0; functions.size()>fi; ++fi) {
							List<RealFunction> functions2=functions.get(fi);
							Progress progress4=progress3.subProgress(
									1.0*fi/functions.size(), null, (fi+1.0)/functions.size());
							Map<Double, Double> sample
									=hash?new HashMap<>():new TreeMap<>();
							for (long xx=1000000l; 1l<<38>xx; xx+=1000001l) {
								double yy=0.0;
								for (RealFunction function: functions2) {
									yy+=function.valueAt(xx);
								}
								sample.put(1.0*xx, yy);
							}
							long start=System.nanoTime();
							LinearCombinationFunction function
									=LeastSquares.regression(
											functions2,
											Function.identity(),
											sumFactory,
											progress4.subProgress(0.05, null, 0.75),
											sumFactory,
											sample.entrySet(),
											solver);
							long end=System.nanoTime();
							double distance=LeastSquares.distanceSquared(
									function,
									progress4.subProgress(0.75, null, 1.0),
									sample.entrySet(),
									Sum::priority);
							long time=end-start;
							distanceSum.add(distance);
							timeSum+=time;
							progress4.finished();
						}
						writer.write(Arrays.asList(
								hash?"2-hash":"1-tree",
								solverName,
								sumName,
								Double.toString(distanceSum.sum()),
								Long.toUnsignedString(timeSum)));
						progress3.finished();
					}
					progress2.finished();
				}
				progress1.finished();
			}
		}
		progress0.finished();
	}
}
