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

public class RegressionTest {
	private static final Set<Function<Double, Double>> BLACKLIST
			=new HashSet<>(Arrays.asList(
					Functions.ONE_PER_X,
					Functions.ONE_PER_X2,
					Functions.X_PER_LNLNX,
					Functions.X_PER_LNX));
	
	@Test
	public void test() throws Throwable {
		List<Function<Double, Double>> functions=new ArrayList<>();
		for (Function<Double, Double> function0: Functions.FUNCTIONS) {
			if (!Functions.ONE.equals(function0)) {
				functions.clear();
				functions.add(function0);
				functions.add(Functions.ONE);
				test(functions);
				for (Function<Double, Double> function1:
						Functions.FUNCTIONS) {
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
	
	private void test(List<Function<Double, Double>> functions)
			throws Throwable {
		List<Double> coefficients=new ArrayList<>(functions.size());
		for (int ii=0; functions.size()>ii; ++ii) {
			coefficients.add(ii+4.0);
		}
		Function<Double, Double> real=new LinearCombinationFunction(
				coefficients, functions, null, Sum::priority);
		Random random=new Random(1234l);
		final double error=1.0;
		Function<Double, Double> noisy
				=(xx)->real.apply(xx)+2.0*error*(random.nextDouble()-0.5);
		Map<Double, Double> sample=new HashMap<>();
		for (double xx=4.0; 32.0>xx; xx+=1.0/16.0) {
			sample.put(xx, noisy.apply(xx));
		}
		LinearCombinationFunction regression=Regression.regression(functions,
				Sum::priority, Progress.NULL, sample, Sum::priority);
		boolean backlist=false;
		for (Function<Double, Double> function: functions) {
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
				Regression.distanceSquared(
						regression, Progress.NULL, sample, Sum::priority)
				<=sample.size()*error*error);
	}
}
