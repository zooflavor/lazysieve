package gui.math;

public class UnsignedLongMath {
	private UnsignedLongMath() {
	}
	
	public static long moduloAddition(long modulus, long value0, long value1) {
		if (0==modulus) {
			throw new ArithmeticException();
		}
		value0=Long.remainderUnsigned(value0, modulus);
		value1=Long.remainderUnsigned(value1, modulus);
		long result=value0+value1;
		if ((0<Long.compareUnsigned(value0, result))/*
				|| (0<Long.compareUnsigned(value1, result))*/) {
			result=Long.remainderUnsigned(-modulus, modulus)
					+Long.remainderUnsigned(result, modulus);
		}
		return Long.remainderUnsigned(result, modulus);
	}
	
	public static long moduloExponentiation(long base, long exponent,
			long modulus) {
		if (0==modulus) {
			throw new ArithmeticException();
		}
		base=Long.remainderUnsigned(base, modulus);
		long result=Long.remainderUnsigned(1l, modulus);
		while (0l!=exponent) {
			if (0l!=(exponent&1l)) {
				result=moduloMultiplication(modulus, result, base);
			}
			base=moduloMultiplication(modulus, base, base);
			exponent>>>=1;
		}
		return result;
	}
	
	public static long moduloMultiplication(long modulus, long value0,
			long value1) {
		if (0==modulus) {
			throw new ArithmeticException();
		}
		value0=Long.remainderUnsigned(value0, modulus);
		value1=Long.remainderUnsigned(value1, modulus);
		long result=0l;
		while (0l!=value1) {
			if (0l!=(value1&1l)) {
				result=moduloAddition(modulus, result, value0);
			}
			value0=moduloAddition(modulus, value0, value0);
			value1>>>=1;
		}
		return result;
	}
	
	public static long square(long value) {
		if (!squareExists(value)) {
			throw new ArithmeticException();
		}
		return value*value;
	}
	
	public static boolean squareExists(long value) {
		return 0>Long.compareUnsigned(value, 1l<<(Long.SIZE/2));
	}
	
	public static long squareRootFloor(long value) {
		long higher=(1l<<(Long.SIZE/2))-1l;
		String sh=Long.toUnsignedString(higher);
		long ss=square(higher);
		String sss=Long.toUnsignedString(ss);
		int cc=Long.compareUnsigned(square(higher), value);
		if (0>=Long.compareUnsigned(square(higher), value)) {
			return higher;
		}
		long floor=0l;
		while (floor+1<higher) {
			long middle=(floor+higher)>>1;
			if (0>=Long.compareUnsigned(square(middle), value)) {
				floor=middle;
			}
			else {
				higher=middle;
			}
		}
		return floor;
	}
}
