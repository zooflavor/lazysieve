package gui.math;

import java.math.BigInteger;
import java.util.function.LongConsumer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class UnsignedLongMathTest {
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
}
