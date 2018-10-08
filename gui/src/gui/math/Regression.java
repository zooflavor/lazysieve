package gui.math;

import gui.ui.progress.Progress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Regression {
	private Regression() {
	}
	
	public static double distanceSquared(Function<Double, Double> function,
			Progress progress, Map<Double, Double> sample,
			Supplier<Sum> sumFactory) throws Throwable {
		Sum sum=sumFactory.get();
		int ii=0;
		for (Map.Entry<Double, Double> entry: sample.entrySet()) {
			progress.progress(1.0*ii/sample.size());
			double xx=entry.getKey();
			double yy=entry.getValue();
			double dy=yy-function.apply(xx);
			sum.add(dy*dy);
			++ii;
		}
		progress.finished();
		return sum.sum();
	}
	
	public static LinearCombinationFunction regression(
			List<Function<Double, Double>> functions,
			Supplier<Sum> functionSumFactory, Progress progress,
			Map<Double, Double> sample, Supplier<Sum> regressionSumFactory)
			throws Throwable {
		double[][] xx=Matrix.create(sample.size(), functions.size());
		double[][] yy=Matrix.create(sample.size(), 1);
		Progress subProgress=progress.subProgress(0.0, null, 0.5);
		int rr=0;
		for (Map.Entry<Double, Double> entry: sample.entrySet()) {
			subProgress.progress(1.0*rr/sample.size());
			double sampleX=entry.getKey();
			double sampleY=entry.getValue();
			yy[rr][0]=sampleY;
			for (int cc=functions.size()-1; 0<=cc; --cc) {
				xx[rr][cc]=functions.get(cc).apply(sampleX);
			}
			++rr;
		}
		subProgress.finished();
		double[][] xxt=Matrix.transpose(xx);
		double[][] coefficients=Matrix.gaussianElimination(
				Matrix.multiply(xxt, xx, regressionSumFactory),
				Matrix.multiply(xxt, yy, regressionSumFactory),
				progress.subProgress(0.5, null, 1.0),
				true);
		List<Double> coefficients2=new ArrayList<>(coefficients.length);
		for (int ii=0; coefficients.length>ii; ++ii) {
			coefficients2.add(coefficients[ii][0]);
		}
		progress.finished();
		return new LinearCombinationFunction(
				coefficients2, functions, null, functionSumFactory);
	}
}
