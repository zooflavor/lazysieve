package gui.math;

import gui.ui.progress.Progress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Regression {
	private Regression() {
	}
	
	public static <X extends Number, Y extends Number> double distanceSquared(
			RealFunction function, Progress progress,
			Collection<Map.Entry<X, Y>> sample, Supplier<Sum> sumFactory)
			throws Throwable {
		Sum sum=sumFactory.get();
		int ii=0;
		for (Map.Entry<X, Y> entry: sample) {
			progress.progress(1.0*ii/sample.size());
			double xx=entry.getKey().doubleValue();
			double yy=entry.getValue().doubleValue();
			double dy=yy-function.valueAt(xx);
			sum.add(dy*dy);
			++ii;
		}
		progress.finished();
		return sum.sum();
	}
	
	public static <X extends Number, Y extends Number>
			LinearCombinationFunction regression(List<RealFunction> functions,
					Supplier<Sum> functionSumFactory, Progress progress,
					Collection<Map.Entry<X, Y>> sample,
					Supplier<Sum> regressionSumFactory) throws Throwable {
		double[][] xx=Matrix.create(sample.size(), functions.size());
		double[][] yy=Matrix.create(sample.size(), 1);
		Progress subProgress=progress.subProgress(0.0, null, 0.5);
		int rr=0;
		for (Map.Entry<X, Y> entry: sample) {
			subProgress.progress(1.0*rr/sample.size());
			double sampleX=entry.getKey().doubleValue();
			double sampleY=entry.getValue().doubleValue();
			yy[rr][0]=sampleY;
			for (int cc=functions.size()-1; 0<=cc; --cc) {
				xx[rr][cc]=functions.get(cc).valueAt(sampleX);
			}
			++rr;
		}
		subProgress.finished();
		double[][] xxt=Matrix.transpose(xx);
		Sum sum=regressionSumFactory.get();
		double[][] xxtxx=Matrix.multiply(xxt, xx,
				progress.subProgress(0.5, null,
						0.5+0.45*(functions.size()-1)/functions.size()),
				sum);
		double[][] xxtyy=Matrix.multiply(xxt, yy,
				progress.subProgress(0.95-0.45/functions.size(), null, 0.95),
				sum);
		double[][] coefficients=Matrix.gaussianElimination(
				xxtxx, xxtyy, progress.subProgress(0.95, null, 1.0), true);
		List<Double> coefficients2=new ArrayList<>(coefficients.length);
		for (int ii=0; coefficients.length>ii; ++ii) {
			coefficients2.add(coefficients[ii][0]);
		}
		progress.finished();
		return new LinearCombinationFunction(
				coefficients2, functions, null, functionSumFactory);
	}
}
