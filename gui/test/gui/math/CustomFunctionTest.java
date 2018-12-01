package gui.math;

import javax.script.ScriptException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class CustomFunctionTest {
	@Test
	public void test() throws Throwable {
		String script="var r=NaN;"
				+"if ((0.0>x) || (1.0<x)) {r=x;}"
				+"else if (0.375===x) {r=\"abc\";}"
				+"else if (0.625==x) {throw \"abc\";}"
				+"r";
		try {
			CustomFunction.create(
					"efgh",
					"abcd",
					script);
			fail();
		}
		catch (ScriptException ex) {
		}
		test(CustomFunction.create(
				"JavaScript",
				"abcd",
				script));
		test(CustomFunction.create(
				TestScriptEngineFactory.NAME,
				"abcd",
				script));
	}
	
	private void test(RealFunction function) {
		assertEquals("abcd", function.toString());
		assertTrue(function.isDefined(-Double.MAX_VALUE, -Double.MIN_VALUE));
		assertFalse(function.isDefined(0.0, 1.0));
		assertTrue(function.isDefined(1.0+Math.ulp(1.0), Double.MAX_VALUE));
		assertFalse(function.isDefined(-1.0, 1.0));
		assertFalse(function.isDefined(0.0, 2.0));
		assertFalse(function.isDefined(-1.0, 2.0));
		assertFalse(function.isDefined(-1.0, 2.25));
		assertFalse(function.isDefined(-1.0, 4.0));
		assertFalse(function.isDefined(-3.0, 2.0));
		assertTrue(function.isDefined(-1.0, 1000.0));
		try {
			assertFalse(Double.isFinite(function.valueAt(0.0)));
		}
		catch (ArithmeticException ex) {
		}
		try {
			assertFalse(Double.isFinite(function.valueAt(0.625)));
		}
		catch (ArithmeticException ex) {
		}
		try {
			assertFalse(Double.isFinite(function.valueAt(0.375)));
		}
		catch (ArithmeticException ex) {
		}
		assertEquals(-2.0, function.valueAt(-2.0), 0.01);
		assertEquals(2.0, function.valueAt(2.0), 0.01);
	}
}
