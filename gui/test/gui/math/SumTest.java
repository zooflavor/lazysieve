package gui.math;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class SumTest {
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
			simple.add(1.0);
			if (value==simple.sum()) {
				break;
			}
		}
		Sum priority=Sum.priority();
		priority.add(value);
		priority.add(1.0);
		priority.add(1.0);
		assertTrue(value!=priority.sum());
	}
	
	@Test
	public void testSum() throws Throwable {
		for (Sum sum: new Sum[]{Sum.simple(), Sum.priority()}) {
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
