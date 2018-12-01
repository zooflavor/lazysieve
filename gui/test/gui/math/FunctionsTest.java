package gui.math;

import static gui.math.Functions.COMPARATOR;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class FunctionsTest {
	@Test
	public void test() throws Throwable {
		testEverywhere(Functions.ONE);
		testEverywhere(Functions.X);
		testEverywhere(Functions.X2);
		testEverywhere(Functions.X3);
		testEverywhere(Functions.X4);
		testEverywhere(Functions.X5);
		testEverywhere(Functions.X6);
		
		testEverywhereExceptZero(Functions.ONE_PER_X);
		testEverywhereExceptZero(Functions.ONE_PER_X2);
		
		testFrom(0.0, Functions.SQRT_X, true);
		
		testFrom(0.0, Functions.LN2X, false);
		testFrom(0.0, Functions.LNX, false);
		testFrom(0.0, Functions.X_LN2X, false);
		testFrom(0.0, Functions.X_LNX, false);
		
		testFrom(1.0, Functions.LNLNX, false);
		testFrom(1.0, Functions.LNX_LNLNX, false);
		testFrom(1.0, Functions.X_LNLNX, false);
		testFrom(1.0, Functions.X_LNX_LNLNX, false);
		
		testPerLnX(1.0, 0.0, Functions.X2_PER_LNX);
		testPerLnX(1.0, 0.0, Functions.X_PER_LN2X);
		testPerLnX(1.0, 0.0, Functions.ONE_PER_LNX);
		testPerLnX(1.0, 0.0, Functions.X_PER_LNX);
		
		testPerLnX(Math.E, 1.0, Functions.ONE_PER_LNLNX);
		testPerLnX(Math.E, 1.0, Functions.X2_PER_LNLNX);
		testPerLnX(Math.E, 1.0, Functions.X_PER_LNLNX);
		testPerLnX(Math.E, 1.0, Functions.X_PER_LNLNX_LNX);
	}
	
	@Test
	public void testCollections() throws Throwable {
		List<RealFunction> functions=new ArrayList<>();
		int modifiers=Modifier.FINAL|Modifier.PUBLIC|Modifier.STATIC;
		for (Field field: Functions.class.getDeclaredFields()) {
			if ((field.getModifiers()==modifiers)
					&& RealFunction.class.equals(field.getType())) {
				RealFunction function=(RealFunction)field.get(null);
				functions.add(function);
			}
		}
		functions.sort(COMPARATOR);
		assertEquals(functions.size(), Functions.FUNCTIONS.size());
		for (int ii=0; functions.size()>ii; ++ii) {
			assertSame(functions.get(ii), Functions.FUNCTIONS.get(ii));
		}
	}
	
	private void testEverywhere(RealFunction function) throws Throwable {
		assertTrue(function.isDefined(-Double.MAX_VALUE, Double.MAX_VALUE));
		assertTrue(Double.isFinite(function.valueAt(0.0)));
		assertTrue(Double.isFinite(function.valueAt(1000.0)));
		assertTrue(Double.isFinite(function.valueAt(-1000.0)));
	}
	
	private void testEverywhereExceptZero(RealFunction function) throws Throwable {
		assertTrue(function.isDefined(-Double.MAX_VALUE, -Double.MIN_VALUE));
		assertTrue(function.isDefined(Double.MIN_VALUE, Double.MAX_VALUE));
		assertFalse(function.isDefined(0.0, 0.0));
		assertTrue(Double.isFinite(function.valueAt(1000.0)));
		assertTrue(Double.isFinite(function.valueAt(-1000.0)));
		try {
			assertFalse(Double.isFinite(function.valueAt(0.0)));
		}
		catch (ArithmeticException ex) {
		}
	}
	
	private void testFrom(double from, RealFunction function,
			boolean definedAtFrom) throws Throwable {
		double ulp=Math.ulp(from);
		if (definedAtFrom) {
			assertTrue(function.isDefined(from, Double.MAX_VALUE));
			assertFalse(function.isDefined(-Double.MAX_VALUE, from-ulp));
			assertTrue(Double.isFinite(function.valueAt(from)));
			try {
				assertFalse(Double.isFinite(function.valueAt(from-ulp)));
			}
			catch (ArithmeticException ex) {
			}
		}
		else {
			assertTrue(function.isDefined(from+ulp, Double.MAX_VALUE));
			assertFalse(function.isDefined(-Double.MAX_VALUE, from));
			assertTrue(Double.isFinite(function.valueAt(from+ulp)));
			try {
				assertFalse(Double.isFinite(function.valueAt(from)));
			}
			catch (ArithmeticException ex) {
			}
		}
	}
	
	private void testPerLnX(double except, double from, RealFunction function) throws Throwable {
		double ulp0=Math.ulp(from);
		double ulp1=Math.ulp(except);
		assertFalse(function.isDefined(-Double.MAX_VALUE, from));
		assertTrue(function.isDefined(from+ulp0, except-ulp1));
		assertFalse(function.isDefined(except, except));
		assertTrue(function.isDefined(except+ulp1, Double.MAX_VALUE));
		try {
			assertFalse(Double.isFinite(function.valueAt(from)));
		}
		catch (ArithmeticException ex) {
		}
		assertTrue(Double.isFinite(function.valueAt(0.5*(from+except))));
		try {
			assertFalse(Double.isFinite(function.valueAt(except)));
		}
		catch (ArithmeticException ex) {
		}
		assertTrue(Double.isFinite(function.valueAt(except+1.0)));
	}
}
