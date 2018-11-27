package gui.math;

import gui.ui.progress.Progress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class SumTest {
	public static void main(String[] args) throws Throwable {
		List<Supplier<Sum>> factories=Arrays.asList(
				Sum::array, Sum::priority, Sum::simple);
		for (boolean hash: new boolean[]{false, true}) {
			for (boolean totalPivoting: new boolean[]{false, true}) {
				for (Supplier<Sum> sumFactory: factories) {
					functionsLoop: for (int ii=0; ; ++ii) {
						List<RealFunction> functions=new ArrayList<>();
						switch (ii) {
							case 0:
								functions.add(Functions.ONE);
								functions.add(Functions.X);
								functions.add(Functions.X_LNLNX);
								functions.add(Functions.X_LNX);
								break;
							case 1:
								functions.add(Functions.ONE);
								functions.add(Functions.X);
								functions.add(Functions.X_LNLNX);
								break;
							case 2:
								functions.add(Functions.ONE);
								functions.add(Functions.X);
								functions.add(Functions.X_LNX);
								break;
							case 3:
								functions.add(Functions.ONE);
								functions.add(Functions.X);
								functions.add(Functions.LNX);
								break;
							case 4:
								functions.add(Functions.ONE);
								functions.add(Functions.X);
								functions.add(Functions.LNLNX);
								functions.add(Functions.LNX);
								break;
							case 5:
								functions.add(Functions.ONE);
								functions.add(Functions.X);
								functions.add(Functions.LNLNX);
								break;
							case 6:
								functions.add(Functions.ONE);
								functions.add(Functions.X);
								functions.add(Functions.SQRT_X);
								break;
							case 7:
								functions.add(Functions.ONE);
								functions.add(Functions.X);
								functions.add(Functions.X2);
								break;
							default:
								break functionsLoop;
						}
						Map<Double, Double> sample
								=hash?new HashMap<>():new TreeMap<>();
						double sumYs=0.0;
						for (long xx=1000000l; 1l<<38>xx; xx+=1000001l) {
							double yy=0.0;
							for (RealFunction function: functions) {
								yy+=function.valueAt(xx);
							}
							sumYs+=yy;
							sample.put(1.0*xx, yy);
						}
						long start=System.nanoTime();
						LinearCombinationFunction function
								=LeastSquares.regression(
										functions,
										Function.identity(),
										sumFactory,
										Progress.NULL,
										sumFactory,
										sample.entrySet(),
										totalPivoting);
						long end=System.nanoTime();
						double distance=LeastSquares.distanceSquared(
								function,
								Progress.NULL,
								sample.entrySet(),
								Sum::priority);
						System.out.println(String.format(
								//"%1$s,%2$s,%3$s,%4$g,%5$d,\"%6$s\"",
								"%1$s - t.p. %2$5s - sum %3$8s - error %4$,30g - time %5$,20d ns - %6$s",
								name(sample),
								totalPivoting,
								name(sumFactory.get()),
								distance,
								end-start,
								functions));
					}
				}
			}
		}
	}
	
	private static String name(Object object) {
		if (object instanceof HashMap) {
			return "hash";
		}
		if (object instanceof Sum.Array) {
			return "array";
		}
		if (object instanceof Sum.Priority) {
			return "priority";
		}
		if (object instanceof Sum.Simple) {
			return "simple";
		}
		if (object instanceof TreeMap) {
			return "tree";
		}
		return String.valueOf(object);
	}
	
	@Test
	public void testPrecision() throws Throwable {
		Sum simple=Sum.simple();
		double value=2.0;
		for (int ii=0; ; ++ii, value*=2.0) {
			if (1000<=ii) {
				fail();
			}
			simple.clear();
			simple.add(value);
			simple.add(1.0);
			if (value==simple.sum()) {
				break;
			}
		}
		for (Sum sum: new Sum[]{Sum.array(), Sum.priority()}) {
			sum.add(value);
			sum.add(1.0);
			sum.add(1.0);
			assertTrue(value!=sum.sum());
		}
	}
	
	@Test
	public void testSum() throws Throwable {
		for (Sum sum: new Sum[]{Sum.array(), Sum.simple(), Sum.priority()}) {
			try {
				sum.add(Double.NaN);
				fail();
			}
			catch (ArithmeticException ex) {
			}
			try {
				sum.add(Double.NEGATIVE_INFINITY);
				fail();
			}
			catch (ArithmeticException ex) {
			}
			try {
				sum.add(Double.POSITIVE_INFINITY);
				fail();
			}
			catch (ArithmeticException ex) {
			}
			assertTrue(0.0==sum.sum());
			sum.add(0.0);
			sum.add(1.0);
			sum.add(2.0);
			sum.add(3.0);
			assertTrue(6.0==sum.sum());
			assertTrue(6.0==sum.sum());
			sum.clear();
			assertTrue(0.0==sum.sum());
			sum.add(1.0);
			sum.add(-2.0);
			sum.add(3.0);
			assertTrue(2.0==sum.sum());
			assertTrue(2.0==sum.sum());
			sum.clear();
			assertTrue(0.0==sum.sum());
			sum.add(1.0);
			sum.add(-1.0);
			assertTrue(0.0==sum.sum());
		}
	}
}
