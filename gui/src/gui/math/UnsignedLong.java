package gui.math;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.regex.Pattern;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class UnsignedLong {
	public static final long MAX_PRIME=(1l<<32)-1l;
	public static int MAX_PRIME_COUNT=203280221;
	public static final long UNSIGNED_INT_MASK=0xffffffffl;
	
	static {
		new UnsignedLong();
	}
	
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
		return format(NumberFormat.getIntegerInstance(),
				DecimalFormatSymbols.getInstance(),
				value);
	}
	
	public static String format(NumberFormat format,
			DecimalFormatSymbols symbols, long value) {
		String result=Long.toUnsignedString(value);
		if (4<=result.length()) {
			if (format.isGroupingUsed()) {
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
		return parse(NumberFormat.getIntegerInstance(),
				DecimalFormatSymbols.getInstance(),
				value);
	}
	
	public static long parse(NumberFormat format, DecimalFormatSymbols symbols,
			String value) {
		if (format.isGroupingUsed()) {
			value=value.replaceAll(
					Pattern.quote(""+symbols.getGroupingSeparator()),
					"");
		}
		return Long.parseUnsignedLong(value);
	}
	
	public static long round(double value) {
		if (!Double.isFinite(value)) {
			throw new ArithmeticException(Double.toString(value));
		}
		if (0.0>=value) {
			return 0l;
		}
		if (2.0*Long.MAX_VALUE<=value) {
			return -1l;
		}
		if (Long.MAX_VALUE>=value) {
			return Math.round(value);
		}
		return (1l<<63)+Math.round(value-2.0*(1l<<62));
	}
	
	public static long square(long value) {
		if (!squareExists(value)) {
			throw new ArithmeticException(format(value));
		}
		return value*value;
	}
	
	public static boolean squareExists(long value) {
		return 0>=Long.compareUnsigned(value, MAX_PRIME);
	}
	
	public static long squareRootFloor(long value) {
		long higher=(1l<<(Long.SIZE/2))-1l;
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
	
	public static double toDouble(long value) {
		long value2=value&Long.MAX_VALUE;
		double result=value2;
		if (value2!=value) {
			result+=2.0*(1l<<62);
		}
		return result;
	}
	
	public static long unsignedInt(int value) {
		return value&UNSIGNED_INT_MASK;
	}
}
