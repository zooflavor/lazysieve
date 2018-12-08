package gui.math;

import gui.ui.progress.Progress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class LeastSquares {
	static {
		new LeastSquares();
	}
	
	private LeastSquares() {
	}
	
	public static double distanceSquared(RealFunction function,
			Progress progress, Collection<Map.Entry<Double, Double>> sample,
			Supplier<Sum> sumFactory) throws Throwable {
		Sum sum=sumFactory.get();
		int ii=0;
		for (Map.Entry<Double, Double> entry: sample) {
			progress.progress(1.0*ii/sample.size());
			double xx=entry.getKey();
			double yy=entry.getValue();
			double dy=yy-function.valueAt(xx);
			sum.add(dy*dy);
			++ii;
		}
		progress.finished();
		return sum.sum();
	}
	
	public static LinearCombinationFunction regression(
			List<RealFunction> functions,
			Function<RealFunction, RealFunction> functionTransform,
			Supplier<Sum> functionSumFactory, Progress progress,
			Supplier<Sum> regressionSumFactory,
			Collection<Map.Entry<Double, Double>> sample,
			Solver solver) throws Throwable {
		List<RealFunction> transformedFunctions
				=new ArrayList<>(functions.size());
		for (int ii=0; functions.size()>ii; ++ii) {
			transformedFunctions.add(
					functionTransform.apply(functions.get(ii)));
		}
		double[][] xx=Matrix.create(sample.size(), functions.size());
		double[][] yy=Matrix.create(sample.size(), 1);
		Progress subProgress=progress.subProgress(0.0, null, 0.5);
		int rr=0;
		for (Map.Entry<Double, Double> entry: sample) {
			subProgress.progress(1.0*rr/sample.size());
			double sampleX=entry.getKey();
			double sampleY=entry.getValue();
			yy[rr][0]=sampleY;
			for (int cc=functions.size()-1; 0<=cc; --cc) {
				xx[rr][cc]=transformedFunctions.get(cc).valueAt(sampleX);
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
		double[][] coefficients=solver.solve(
				xxtxx,
				xxtyy,
				progress.subProgress(0.95, null, 1.0),
				sum);
		List<Double> coefficients2=new ArrayList<>(coefficients.length);
		for (int ii=0; coefficients.length>ii; ++ii) {
			coefficients2.add(coefficients[ii][0]);
		}
		progress.finished();
		return new LinearCombinationFunction(
				coefficients2, functions, null, functionSumFactory);
	}
}
