package gui.math;

import java.math.BigInteger;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.LongConsumer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class UnsignedLongTest {
	private static void numbers(LongConsumer consumer) {
		for (long ii=0l; 16>ii; ++ii) {
			consumer.accept(ii);
		}
		for (long ii=3; Long.SIZE>ii; ii+=6) {
			for (long jj=-2; 2l>=jj; ++jj) {
				consumer.accept((1l<<ii)+jj);
			}
		}
		for (long ii=-1l, ee=ii-8l; ee!=ii; --ii) {
			consumer.accept(ii);
		}
	}
	
	@Test
	public void testFirstSievePosition() throws Throwable {
		for (long ii=0l; 49l>=ii; ++ii) {
			assertEquals(49l,
					UnsignedLong.firstSievePosition(7l, ii));
		}
		for (long ii=50l; 63l>=ii; ++ii) {
			assertEquals(63l,
					UnsignedLong.firstSievePosition(7l, ii));
		}
		for (long ii=64l; 77l>=ii; ++ii) {
			assertEquals(77l,
					UnsignedLong.firstSievePosition(7l, ii));
		}
	}
	
	@Test
	public void testFormatParse() throws Throwable {
		Locale.setDefault(Locale.GERMAN);
		assertEquals("0", UnsignedLong.format(0l));
		assertEquals(0l, UnsignedLong.parse("0"));
		assertEquals("123.456", UnsignedLong.format(123456l));
		assertEquals(123456l, UnsignedLong.parse("123.456"));
		assertEquals("9.223.372.036.854.775.808", UnsignedLong.format(1l<<63));
		assertEquals(1l<<63, UnsignedLong.parse("9.223.372.036.854.775.808"));
		NumberFormat format=NumberFormat.getIntegerInstance();
		DecimalFormatSymbols symbols=DecimalFormatSymbols.getInstance();
		format.setGroupingUsed(false);
		assertEquals("9223372036854775808",
				UnsignedLong.format(format, symbols, 1l<<63));
		assertEquals(1l<<63,
				UnsignedLong.parse(format, symbols,
						"9223372036854775808"));
	}
	
	@Test
	public void testMax() throws Throwable {
		assertEquals(0x2l,
				UnsignedLong.max(
						0x1l,
						0x2l));
		assertEquals(0x2l,
				UnsignedLong.max(
						0x2l,
						0x1l));
		assertEquals(0x8000000000000001l,
				UnsignedLong.max(
						0x8000000000000001l,
						0x2l));
		assertEquals(0x8000000000000001l,
				UnsignedLong.max(
						0x2l,
						0x8000000000000001l));
		assertEquals(0x8000000000000002l,
				UnsignedLong.max(
						0x8000000000000001l,
						0x8000000000000002l));
		assertEquals(0x8000000000000002l,
				UnsignedLong.max(
						0x8000000000000002l,
						0x8000000000000001l));
	}
	
	@Test
	public void testMin() throws Throwable {
		assertEquals(0x1l,
				UnsignedLong.min(
						0x1l,
						0x2l));
		assertEquals(0x1l,
				UnsignedLong.min(
						0x2l,
						0x1l));
		assertEquals(0x2l,
				UnsignedLong.min(
						0x8000000000000001l,
						0x2l));
		assertEquals(0x2l,
				UnsignedLong.min(
						0x2l,
						0x8000000000000001l));
		assertEquals(0x8000000000000001l,
				UnsignedLong.min(
						0x8000000000000001l,
						0x8000000000000002l));
		assertEquals(0x8000000000000001l,
				UnsignedLong.min(
						0x8000000000000002l,
						0x8000000000000001l));
	}
	
	@Test
	public void testModuloAddition() throws Throwable {
		numbers((aa)->{
			BigInteger a2=new BigInteger(Long.toUnsignedString(aa));
			numbers((bb)->{
				if (0>Long.compareUnsigned(aa, bb)) {
					return;
				}
				BigInteger b2=new BigInteger(Long.toUnsignedString(bb));
				numbers((mm)->{
					if (0l==mm) {
						try {
							UnsignedLong.moduloAddition(mm, aa, bb);
							fail();
						}
						catch (ArithmeticException ex) {
						}
						return;
					}
					BigInteger m2=new BigInteger(Long.toUnsignedString(mm));
					long result=UnsignedLong.moduloAddition(mm, aa, bb);
					assertEquals(result,
							UnsignedLong.moduloAddition(mm, bb, aa));
					assertEquals(
							new BigInteger(Long.toUnsignedString(result)),
							a2.add(b2).remainder(m2));
				});
			});
		});
	}
	
	@Test
	public void testModuloExponentiation() throws Throwable {
		numbers((bb)->{
			BigInteger b2=new BigInteger(Long.toUnsignedString(bb));
			numbers((ee)->{
				BigInteger e2=new BigInteger(Long.toUnsignedString(ee));
				numbers((mm)->{
					if (0l==mm) {
						try {
							UnsignedLong.moduloExponentiation(bb, ee, mm);
							fail();
						}
						catch (ArithmeticException ex) {
						}
						return;
					}
					BigInteger m2=new BigInteger(Long.toUnsignedString(mm));
					long result
							=UnsignedLong.moduloExponentiation(bb, ee, mm);
					assertEquals(
							new BigInteger(Long.toUnsignedString(result)),
							b2.modPow(e2, m2));
				});
			});
		});
	}
	
	@Test
	public void testModuloMultiplication() throws Throwable {
		numbers((aa)->{
			BigInteger a2=new BigInteger(Long.toUnsignedString(aa));
			numbers((bb)->{
				if (0>Long.compareUnsigned(aa, bb)) {
					return;
				}
				BigInteger b2=new BigInteger(Long.toUnsignedString(bb));
				numbers((mm)->{
					if (0l==mm) {
						try {
							UnsignedLong.moduloMultiplication(mm, aa, bb);
							fail();
						}
						catch (ArithmeticException ex) {
						}
						return;
					}
					BigInteger m2=new BigInteger(Long.toUnsignedString(mm));
					long result
							=UnsignedLong.moduloMultiplication(mm, aa, bb);
					assertEquals(result,
							UnsignedLong.moduloMultiplication(mm, bb, aa));
					assertEquals(
							new BigInteger(Long.toUnsignedString(result)),
							a2.multiply(b2).remainder(m2));
				});
			});
		});
	}
	
	@Test
	public void testRound() throws Throwable {
		try {
			UnsignedLong.round(Double.NaN);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			UnsignedLong.round(Double.NEGATIVE_INFINITY);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		try {
			UnsignedLong.round(Double.POSITIVE_INFINITY);
			fail();
		}
		catch (ArithmeticException ex) {
		}
		assertEquals(0l, UnsignedLong.round(0.0));
		assertEquals(1l, UnsignedLong.round(1.0));
		assertEquals(0l, UnsignedLong.round(-1.0));
		assertEquals(1001, UnsignedLong.round(1001));
		assertEquals(Long.MAX_VALUE, UnsignedLong.round(Long.MAX_VALUE));
		assertEquals(-1l, UnsignedLong.round(4.0*(1l<<62)));
		assertEquals(0x1234567890000000l,
				UnsignedLong.round(16.0*(0x123456789000000l)));
		assertEquals(0x9876543210000000l,
				UnsignedLong.round(16.0*(0x987654321000000l)));
	}
	
	@Test
	public void testSquare() throws Throwable {
		for (long ii=0; 128>ii; ++ii) {
			assertEquals(ii*ii, UnsignedLong.square(ii));
		}
		assertFalse(UnsignedLong.squareExists(1l<<32));
		try {
			UnsignedLong.square(1l<<32);
			fail();
		}
		catch (ArithmeticException ex) {
		}
	}
	
	@Test
	public void testSquareRootFloor() throws Throwable {
		numbers((ii)->{
			long sqrt=UnsignedLong.squareRootFloor(ii);
			assertEquals(sqrt,
					UnsignedLong.squareRootFloor(
							UnsignedLong.square(sqrt)));
			assertTrue(0>=Long.compareUnsigned(
					UnsignedLong.square(sqrt), ii));
			if (UnsignedLong.squareExists(sqrt+1)) {
				assertTrue(0<Long.compareUnsigned(
						UnsignedLong.square(sqrt+1), ii));
			}
		});
	}
	
	@Test
	public void testToDouble() throws Throwable {
		assertEquals(1.0, UnsignedLong.toDouble(1l), 0.1);
		assertEquals(1.0*(1l<<62), UnsignedLong.toDouble(1l<<62), 0.1);
		assertEquals(2.0*(1l<<62), UnsignedLong.toDouble(1l<<63), 0.1);
	}
	
	@Test
	public void testUnsignedInt() throws Throwable {
		assertEquals(1l<<3, UnsignedLong.unsignedInt(1<<3));
		assertEquals(1l<<31, UnsignedLong.unsignedInt(1<<31));
	}
}
