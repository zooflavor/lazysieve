package gui.math;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.regex.Pattern;

public class UnsignedLong {
	public static final long MAX_PRIME=(1l<<32)-1l;
	public static int MAX_PRIME_COUNT=203280221;
	public static final long UNSIGNED_INT_MASK=0xffffffffl;
	
	private UnsignedLong() {
	}
	
	public static long firstSievePosition(long prime, long start) {
		long first=UnsignedLong.square(prime);
		if (0>Long.compareUnsigned(first, start)) {
			first=prime*Long.divideUnsigned(start, prime);
			if (0>Long.compareUnsigned(first, start)) {
				first+=prime;
			}
			if (0l==(first&1l)) {
				first+=prime;
			}
		}
		return first;
	}
	
	public static String format(long value) {
		String result=Long.toUnsignedString(value);
		if (4<=result.length()) {
			NumberFormat format=NumberFormat.getIntegerInstance();
			if (format.isGroupingUsed()) {
				DecimalFormatSymbols symbols
						=DecimalFormatSymbols.getInstance();
				int separators=(result.length()-1)/3;
				StringBuilder sb=new StringBuilder(result.length()+separators);
				for (int ii=0; result.length()>ii; ++ii) {
					if ((0!=ii)
							&& (0==((result.length()-ii)%3))) {
						sb.append(symbols.getGroupingSeparator());
					}
					sb.append(result.charAt(ii));
				}
				result=sb.toString();
			}
		}
		return result;
	}
	
	public static long max(long value0, long value1) {
		return (0<=Long.compareUnsigned(value0, value1))?value0:value1;
	}
	
	public static long min(long value0, long value1) {
		return (0>=Long.compareUnsigned(value0, value1))?value0:value1;
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
	
	public static long parse(String value) {
		NumberFormat format=NumberFormat.getIntegerInstance();
		if (format.isGroupingUsed()) {
			DecimalFormatSymbols symbols
					=DecimalFormatSymbols.getInstance();
			value=value.replaceAll(
					Pattern.quote(""+symbols.getGroupingSeparator()),
					"");
		}
		return Long.parseUnsignedLong(value);
	}
	
	public static long square(long value) {
		if (!squareExists(value)) {
			throw new ArithmeticException();
		}
		return value*value;
	}
	
	public static boolean squareExists(long value) {
		return 0>=Long.compareUnsigned(value, MAX_PRIME);
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
	
	public static long unsignedInt(int value) {
		return value&UNSIGNED_INT_MASK;
	}
}
