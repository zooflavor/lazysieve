package gui.sieve;

import gui.math.UnsignedLong;
import java.util.Arrays;

public class LongTable implements SieveTable {
	private final long[] bits=new long[(int)((UnsignedLong.MAX_PRIME+1l)>>>7)];

	@Override
	public void clear(boolean prime) {
		Arrays.fill(bits, prime?0l:-1l);
	}

	@Override
	public void flip(long number) throws Throwable {
		if (0<=Long.compareUnsigned(UnsignedLong.MAX_PRIME, number)) {
			number=(number-1l)>>>1;
			bits[(int)(number>>>6)]^=1l<<(number&0x3fl);
		}
	}

	@Override
	public boolean isPrime(long number) throws Throwable {
		number=(number-1l)>>>1;
		return 0l==(bits[(int)(number>>>6)]&(1l<<(number&0x3fl)));
	}

	@Override
	public void listPrimes(long end, PrimeConsumer primeConsumer,
			long start) throws Throwable {
		if (0l==(end&1l)) {
			--end;
		}
		if (0l==(start&1l)) {
			++start;
		}
		int ei=(int)(end>>>1);
		int si=(int)(start>>>1);
		for (; (0<Integer.compareUnsigned(ei, si)) && (0!=(si&0x3f)); ++si) {
			if (0l==(bits[si>>>6]&(1l<<(si&0x3fl)))) {
				primeConsumer.prime(2l*si+1l);
			}
		}
		for (; 0<Integer.compareUnsigned(ei, si); si+=64) {
			long bb=~bits[si>>>6];
			int bi=si;
			while (0!=bb) {
				int nz=Long.numberOfTrailingZeros(bb);
				bi+=nz;
				bb>>>=nz;
				primeConsumer.prime(2l*bi+1);
				++bi;
				bb>>>=1;
			}
		}
		for (; 0<Integer.compareUnsigned(ei, si); ++si) {
			if (0l==(bits[si>>>6]&(1l<<(si&0x3fl)))) {
				primeConsumer.prime(2l*si+1l);
			}
		}
	}

	@Override
	public void setComposite(long number) throws Throwable {
		if (0<=Long.compareUnsigned(UnsignedLong.MAX_PRIME, number)) {
			number=(number-1l)>>>1;
			bits[(int)(number>>>6)]|=1l<<(number&0x3fl);
		}
	}

	@Override
	public void setPrime(long number) throws Throwable {
		if (0<=Long.compareUnsigned(UnsignedLong.MAX_PRIME, number)) {
			number=(number-1l)>>>1;
			bits[(int)(number>>>6)]&=~(1l<<(number&0x3fl));
		}
	}
}
