package gui.math;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class LinearCombinationFunctionTest {
	@Test
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void test() throws Throwable {
		try {
			new LinearCombinationFunction(
					Arrays.asList(),
					Arrays.asList(Functions.ONE),
					null,
					Sum::preferred);
			fail();
		}
		catch (IllegalArgumentException ex) {
		}
		LinearCombinationFunction function=new LinearCombinationFunction(
				Arrays.asList(2.0, 3.0),
				Arrays.asList(Functions.ONE, Functions.LNX),
				null,
				Sum::preferred);
		assertEquals("2.0*1+3.0*ln(x)", function.toString());
		assertEquals("abcd",
				new LinearCombinationFunction(
								function.coefficients,
								function.functions,
								"abcd",
								Sum::preferred)
						.toString());
		assertFalse(function.isDefined(0.0, 1.0));
		assertTrue(function.isDefined(1.0, 2.0));
		try {
			assertFalse(Double.isFinite(function.valueAt(0.0)));
		}
		catch (ArithmeticException ex) {
		}
		assertEquals(2.0, function.valueAt(1.0), 0.01);
		assertEquals(5.0, function.valueAt(Math.E), 0.01);
	}
	
	@Test
	public void testEmpty() throws Throwable {
		LinearCombinationFunction function=new LinearCombinationFunction(
				Arrays.asList(),
				Arrays.asList(),
				null,
				Sum::preferred);
		assertEquals("0", function.toString());
		assertTrue(function.isDefined(-Double.MAX_VALUE, Double.MAX_VALUE));
		assertEquals(0.0, function.valueAt(0.0), 0.01);
		assertEquals(0.0, function.valueAt(1.0), 0.01);
	}
}
