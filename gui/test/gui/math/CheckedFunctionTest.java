package gui.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class CheckedFunctionTest {
	@Test
	public void test() throws Throwable {
		RealFunction function0=new RealFunction() {
			private final RealFunction function=Functions.ONE_PER_LNX;
			
			@Override
			public boolean isDefined(double fromX, double toX) {
				return function.isDefined(fromX, toX);
			}
			
			@Override
			public String toString() {
				return function.toString();
			}
			
			@Override
			public double valueAt(double xx) {
				if (0.0==xx) {
					return Double.NaN;
				}
				if (1.0==xx) {
					throw new ArithmeticException();
				}
				return function.valueAt(xx);
			}
		};
		RealFunction function1
				=new CheckedFunction<>(ArrayStoreException::new, function0);
		assertEquals(function0.toString(), function1.toString());
		for (RealFunction function: new RealFunction[]{function0, function1}) {
			double ulp=Math.ulp(1.0);
			assertFalse(function.isDefined(-Double.MAX_VALUE, 0.0));
			assertTrue(function.isDefined(Double.MIN_VALUE, 1.0-ulp));
			assertFalse(function.isDefined(-Double.MAX_VALUE, 1.0));
			assertTrue(function.isDefined(1.0+ulp, Double.MAX_VALUE));
			assertEquals(0.5, function0.valueAt(Math.E*Math.E), 0.01);
			assertEquals(0.5, function1.valueAt(Math.E*Math.E), 0.01);
		}
		for (double xx: new double[]{0.0, 1.0}) {
			try {
				assertFalse(Double.isFinite(function0.valueAt(xx)));
			}
			catch (ArithmeticException ex) {
			}
			try {
				function1.valueAt(xx);
				fail();
			}
			catch (ArrayStoreException ex) {
			}
		}
	}
}
