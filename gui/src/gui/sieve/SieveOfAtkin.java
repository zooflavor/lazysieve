package gui.sieve;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import gui.util.IntList;
import gui.util.LongList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SieveOfAtkin extends SegmentedSieve {
	public static final List<Sieve.Descriptor> SIEVES
			=Collections.unmodifiableList(Arrays.asList(
					new Sieve.Descriptor(
							SieveOfAtkin::new,
							"Atkin szitája",
							"atkin",
							30, 6, 20)));
	
	@FunctionalInterface
	private static interface QuadraticSieve {
		void sieve(long delta, long LB, long LL,
				OperationCounter operationCounter, SieveTable sieveTable)
				throws Throwable;
	}
	
	private static final long LARGE_PRIME
			=UnsignedLong.squareRootFloor(Long.divideUnsigned(-1l, 3))+1l;
	private static final long[][] SIEVE_REMAINDERS={
			{11, 23, 47, 59},
			{1, 13, 17, 29, 37, 41, 49, 53},
			{7, 19, 31, 43}};
	private static final QuadraticSieve[] SIEVES2={
			SieveOfAtkin::sieve12Z11,
			SieveOfAtkin::sieve4Z1,
			SieveOfAtkin::sieve6Z1};
	
	static {
		if (0>=Long.compareUnsigned(
				3*UnsignedLong.square(LARGE_PRIME-1l),
				3*UnsignedLong.square(LARGE_PRIME))) {
			throw new IllegalStateException();
		}
	}
	
	public SieveOfAtkin() {
		super(7l);
	}
	
	private final LongList positions
			=new LongList(UnsignedLong.MAX_PRIME_COUNT);
	private final IntList primes=new IntList(UnsignedLong.MAX_PRIME_COUNT);
	private int sqrtIndex;
	
	private static long add(long position, long prime2) {
		position+=prime2;
		if (0<=Long.compareUnsigned(prime2, position)) {
			return 0l;
		}
		return position;
	}
	
	@Override
	protected void addPrime(long end, OperationCounter operationCounter,
			long prime, SieveTable sieveTable, long start) throws Throwable {
		long position=sieve(
				end, operationCounter, prime*prime, prime, sieveTable);
		if (0l!=position) {
			positions.add(position);
			primes.add((int)prime);
		}
	}
	
	@Override
	public boolean defaultPrime() {
		return false;
	}
	
	private static long firstSievePosition(long prime, long start) {
		long position=prime*prime;
		if (0>=Long.compareUnsigned(start, position)) {
			return position;
		}
		if (LARGE_PRIME<=prime) {
			return 0l;
		}
		long prime2=2l*position;
		position=position+prime2*(Long.divideUnsigned(start-position, prime2));
		if (0<Long.compareUnsigned(start, position)) {
			position=add(position, prime2);
			if (0l==position) {
				return 0l;
			}
		}
		return position;
	}
	
	@Override
	protected void reset(PrimesProducer primesProducer, Progress progress)
			throws Throwable {
		positions.clear();
		primes.clear();
		sqrtIndex=0;
		if (0l==startSegment) {
			return;
		}
		primesProducer.primes(
				(prime)->{
					long position=firstSievePosition(prime, start());
					if (0l!=position) {
						positions.add(position);
						primes.add((int)prime);
					}
				},
				UnsignedLong.min(UnsignedLong.MAX_PRIME,
						segmentSize*startSegment),
				progress);
	}
	
	protected long sieve(long end, OperationCounter operationCounter,
			long position, long prime, SieveTable sieveTable)
			throws Throwable {
		if (LARGE_PRIME<=prime) {
			if (0<Long.compareUnsigned(end, position)) {
				operationCounter.increment();
				sieveTable.setComposite(position);
				return 0l;
			}
			else {
				return position;
			}
		}
		long prime2=2l*prime*prime;
		for(; (0l!=position)
						&& (0<Long.compareUnsigned(end, position));
				position=add(position, prime2)) {
			operationCounter.increment();
			sieveTable.setComposite(position);
		}
		return position;
	}
    
    private static void sieve12Z11(long delta, long LB, long LL,
			OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable {
		for (long ff=1l; 10l>=ff; ++ff) {
			for (long gg=1l; 30l>=gg; ++gg) {
				if (delta==Long.remainderUnsigned(900l+3l*ff*ff-gg*gg, 60l)) {
					sieve12Z11(delta, ff, gg, LB, LL, operationCounter,
							sieveTable);
				}
			}
		}
    }
    
    private static void sieve12Z11(long delta, long ff, long gg, long LB,
			long LL, OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable {
        // step 1
        long xx=ff;
        long y0=gg;
        long k0=(3*ff*ff-gg*gg-delta)/60;
        // step 2
        while (true) {
            while (k0>=LB) {
                if (xx<=y0) {
                    return;
                }
				operationCounter.increment();
                k0=k0-y0-15;
                y0=y0+30;
            }
            // step 3
            long kk=k0;
            long yy=y0;
            // step 4
            while ((kk>=LL) && (yy<xx)) {
				operationCounter.increment();
				long ii=60l*kk+delta;
				sieveTable.flip(ii);
                kk-=yy+15;
                yy+=30;
            }
            // step 5
			operationCounter.increment();
            k0+=xx+5;
            xx+=10;
        }
    }
    
    private static void sieve4Z1(long delta, long LB, long LL,
			OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable {
		for (long ff=1l; 15l>=ff; ++ff) {
			for (long gg=1l; 30l>=gg; ++gg) {
				if (delta==Long.remainderUnsigned(4l*ff*ff+gg*gg, 60l)) {
					sieve4Z1(delta, ff, gg, LB, LL, operationCounter,
							sieveTable);
				}
			}
		}
    }
    
    private static void sieve4Z1(long delta, long ff, long gg, long LB,
			long LL, OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable {
        // step 1
        long xx=ff;
        long y0=gg;
        long k0=Long.divideUnsigned(4l*ff*ff+gg*gg-delta, 60l);
        // step 2
        while (k0<LB) {
			operationCounter.increment();
            k0+=2l*xx+15l;
            xx+=15l;
        }
        // step 3
        while (true) {
			operationCounter.increment();
            xx-=15;
            k0-=2*xx+15;
            if (xx<=0l) {
                return;
            }
            // step 4
            while (k0<LL) {
				operationCounter.increment();
                k0+=y0+15;
                y0+=30;
            }
            // step 5
            long kk=k0;
            long yy=y0;
            // step 6
            while (kk<LB) {
				operationCounter.increment();
				long ii=60l*kk+delta;
				sieveTable.flip(ii);
                kk+=yy+15;
                yy+=30;
            }
            // step 7
        }
    }
    
    private static void sieve6Z1(long delta, long LB, long LL,
			OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable {
		for (long ff=1l; 10l>=ff; ++ff) {
			for (long gg=1l; 30l>=gg; ++gg) {
				if (delta==Long.remainderUnsigned(3l*ff*ff+gg*gg, 60l)) {
					sieve6Z1(delta, ff, gg, LB, LL,
							operationCounter, sieveTable);
				}
			}
		}
    }
    
    private static void sieve6Z1(long delta, long ff, long gg, long LB,
			long LL, OperationCounter operationCounter, SieveTable sieveTable)
			throws Throwable {
        // step 1
        long xx=ff;
        long y0=gg;
        long k0=Long.divideUnsigned(3l*ff*ff+gg*gg-delta, 60l);
        // step 2
        while (k0<LB) {
			operationCounter.increment();
            k0+=xx+5;
            xx+=10;
        }
        // step 3
        while (true) {
			operationCounter.increment();
            xx-=10;
            k0-=xx+5;
            if (xx<=0l) {
                return;
            }
            // step 4
            while (k0<LL) {
				operationCounter.increment();
                k0+=y0+15;
                y0+=30;
            }
            // step 5
            long kk=k0;
            long yy=y0;
            // step 6
            while (kk<LB) {
				operationCounter.increment();
				long ii=60l*kk+delta;
				sieveTable.flip(ii);
                kk+=yy+15;
                yy+=30;
            }
            // step 7
        }
    }
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
		if (0l==startSegment) {
			sieveTable.setPrime(1l);
			sieveTable.setPrime(3l);
			sieveTable.setPrime(5l);
		}
		long eq=Long.divideUnsigned(end, 60);
		long er=Long.remainderUnsigned(end, 60);
		long sq=Long.divideUnsigned(start, 60);
		long sr=Long.remainderUnsigned(start, 60);
		for (int ii=SIEVES2.length-1; 0<=ii; --ii) {
			QuadraticSieve sieve=SIEVES2[ii];
			for (int jj=SIEVE_REMAINDERS[ii].length-1; 0<=jj; --jj) {
				long delta=SIEVE_REMAINDERS[ii][jj];
				long LB=(delta<er)
						?(eq+1l)
						:eq;
				long LL=(delta<sr)
						?(sq+1l)
						:sq;
				if (LB>LL) {
					sieve.sieve(delta, LB, LL, operationCounter, sieveTable);
				}
			}
		}
		while ((primes.size()>sqrtIndex)
				&& (0<=Long.compareUnsigned(end,
						UnsignedLong.square(UnsignedLong.unsignedInt(
								primes.get(sqrtIndex)))))) {
			++sqrtIndex;
		}
		for (int ii=0; sqrtIndex>ii; ++ii) {
			operationCounter.increment();
			long position=positions.get(ii);
			if (0l!=position) {
				if (0<Long.compareUnsigned(end, position)) {
					position=sieve(end, operationCounter, position,
							UnsignedLong.unsignedInt(primes.get(ii)),
							sieveTable);
					positions.set(ii, position);
				}
			}
		}
	}
}
