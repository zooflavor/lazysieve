package gui.math;

import gui.ui.progress.Progress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class LeastSquaresTest {
	@Test
	public void test() throws Throwable {
		for (Solver solver: new Solver[]{
					Solver.gaussianElimination(false),
					Solver.gaussianElimination(true),
					Solver.preferred(),
					Solver.qrDecomposition()}) {
			test(solver, Functions.ONE);
			List<RealFunction> functions=new ArrayList<>(Functions.FUNCTIONS);
			functions.remove(Functions.ONE);
			for (int ii=0; functions.size()>ii; ++ii) {
				RealFunction function0=functions.get(ii);
				test(solver, Functions.ONE, function0);
				for (int jj=ii+1; functions.size()>jj; ++jj) {
					test(solver, Functions.ONE, function0, functions.get(jj));
				}
			}
		}
	}
	
	private void test(Solver solver, RealFunction... functions)
			throws Throwable {
		List<Double> coefficients=new ArrayList<>(functions.length);
		for (int ii=0; functions.length>ii; ++ii) {
			coefficients.add(ii+4.0);
		}
		RealFunction real=new LinearCombinationFunction(
				coefficients, Arrays.asList(functions), null, Sum::preferred);
		Random random=new Random(1234l);
		final double error=1000.0;
		RealFunction noisy=new RealFunction() {
			@Override
			public boolean isDefined(double fromX, double toX) {
				return real.isDefined(fromX, toX);
			}

			@Override
			public double valueAt(double xx) {
				return real.valueAt(xx)+2.0*error*(random.nextDouble()-0.5);
			}
		};
		Map<Double, Double> sample=new HashMap<>();
		for (double xx=4.0; 64.0>xx; xx+=1.0/16.0) {
			sample.put(xx, noisy.valueAt(xx));
		}
		LinearCombinationFunction regression
				=LeastSquares.regression(
						Arrays.asList(functions),
						Function.identity(),
						Sum::preferred,
						Progress.NULL,
						Sum::preferred,
						sample.entrySet(),
						solver);
		assertTrue(
				LeastSquares.distanceSquared(regression, Progress.NULL,
						sample.entrySet(), Sum::preferred)
				<=sample.size()*error*error);
	}
}
