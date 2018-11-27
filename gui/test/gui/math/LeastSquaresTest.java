package gui.math;

import gui.ui.progress.Progress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class LeastSquaresTest {
	private static final Set<RealFunction> BLACKLIST
			=new HashSet<>(Arrays.asList(
					Functions.ONE_PER_LNLNX,
					Functions.ONE_PER_LNX,
					Functions.ONE_PER_X,
					Functions.ONE_PER_X2,
					Functions.X_PER_LNLNX,
					Functions.X_PER_LNLNX_LNX,
					Functions.X_PER_LNX,
					Functions.X_PER_LN2X));
	
	@Test
	public void test() throws Throwable {
		List<RealFunction> functions=new ArrayList<>();
		for (RealFunction function0: Functions.FUNCTIONS) {
			if (!Functions.ONE.equals(function0)) {
				functions.clear();
				functions.add(function0);
				functions.add(Functions.ONE);
				test(functions);
				for (RealFunction function1: Functions.FUNCTIONS) {
					if ((!Functions.ONE.equals(function1))
							&& (!function0.equals(function1))) {
						functions.clear();
						functions.add(function0);
						functions.add(function1);
						functions.add(Functions.ONE);
						test(functions);
					}
				}
			}
		}
	}
	
	private void test(List<RealFunction> functions) throws Throwable {
		List<Double> coefficients=new ArrayList<>(functions.size());
		for (int ii=0; functions.size()>ii; ++ii) {
			coefficients.add(ii+4.0);
		}
		RealFunction real=new LinearCombinationFunction(
				coefficients, functions, null, Sum::priority);
		Random random=new Random(1234l);
		final double error=1.0;
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
		LinearCombinationFunction regression=LeastSquares.regression(functions,
				Function.identity(), Sum::priority, Progress.NULL,
				Sum::priority, sample.entrySet(), true);
		boolean backlist=false;
		for (RealFunction function: functions) {
			if (BLACKLIST.contains(function)) {
				backlist=true;
				break;
			}
		}
		if (!backlist) {
			for (int ii=0; coefficients.size()>ii; ++ii) {
				Assert.assertEquals(
						coefficients.get(ii),
						regression.coefficients.get(ii),
						0.5);
			}
		}
		assertTrue(
				LeastSquares.distanceSquared(regression, Progress.NULL,
						sample.entrySet(), Sum::priority)
				<=sample.size()*error*error);
	}
}
