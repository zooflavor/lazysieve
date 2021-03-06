package gui.sieve.eratosthenes;

import gui.io.PrimeProducer;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.Sieve;
import gui.sieve.SieveTable;
import gui.ui.progress.Progress;
import gui.util.IntList;
import gui.util.LongList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BucketSieve extends EratosthenesianSieve {
	public static final List<Sieve.Descriptor> SIEVES;
	
	static {
		List<Sieve.Descriptor> sieves=new ArrayList<>();
		sieves.add(new Sieve.Descriptor(
				Bucket1Sieve::new,
				"Eratoszthenész szitája-edények",
				"buckets",
				30, 1, 20));
		for (final int bits: new int[]{1, 2, 3, 4, 5, 6, 7, 8}) {
			sieves.add(new Sieve.Descriptor(
					()->new BucketNSieve(bits),
					"Eratoszthenész szitája-edények-2^"+bits,
					"buckets-"+bits,
					30, 1, 20));
		}
		SIEVES=Collections.unmodifiableList(new ArrayList<>(sieves));
	}
	
	public static class Bucket1Sieve extends BucketSieve {
		public Bucket1Sieve() {
			super(64);
		}
		
		@Override
		protected int bucketIndex(long current, long position) {
			return Long.numberOfLeadingZeros(current^position);
		}
	}
	
	public static class BucketNSieve extends BucketSieve {
		private final int bits;
		private final byte[] digits=new byte[64];
		private final int mask;
		
		public BucketNSieve(int bits) {
			super(buckets(bits));
			this.bits=bits;
			mask=(1<<bits)-1;
			for (int ii=0; digits.length>ii; ++ii) {
				digits[ii]=(byte)((63-ii)/bits);
			}
		}
		
		@Override
		protected int bucketIndex(long current, long position) {
			int digit=digits[Long.numberOfLeadingZeros(current^position)];
			return (digit<<bits)|(((int)(position>>>(digit*bits)))&mask);
		}
		
		private static int buckets(int bits) {
			if ((0>=bits)
					|| (16<bits)) {
				throw new IllegalArgumentException(Integer.toString(bits));
			}
			int digits=64/bits;
			if (64>bits*digits) {
				++digits;
			}
			return digits<<bits;
		}
	}
	
	private long addPrimePosition;
	private final BucketAllocator bucketAllocator=new BucketAllocator();
	private final Bucket[] buckets;
	private OperationCounter operationCounter;
	private final LongList smallPrimePositions=new LongList();
	private final IntList smallPrimes=new IntList();
	
	public BucketSieve(int buckets) {
		super(3l);
		this.buckets=new Bucket[buckets];
	}
	
	private void add(int bucket, long position, int prime) {
		operationCounter.increment();
		Bucket bucket2=buckets[bucket];
		if ((null==bucket2)
				|| bucket2.isFull()) {
			bucket2=bucketAllocator.allocate(bucket2);
			buckets[bucket]=bucket2;
		}
		int index=bucket2.size;
		bucket2.positions[index]=position;
		bucket2.primes[index]=prime;
		++bucket2.size;
	}
	
	@Override
	protected void addPrime(long end, OperationCounter operationCounter,
			long position, long prime, SieveTable sieveTable, long start)
			throws Throwable {
		addPrime(position, prime);
	}
	
	private void addPrime(long position, long prime) throws Throwable {
		if (2l*prime<=segmentSize) {
			smallPrimePositions.add(position);
			smallPrimes.add((int)prime);
		}
		else {
			add(bucketIndex(addPrimePosition, segment(position)),
					position, (int)prime);
		}
	}
	
	protected abstract int bucketIndex(long current, long position);
	
	@Override
	protected void reset(PrimeProducer primeProducer, Progress progress)
			throws Throwable {
		operationCounter=OperationCounter.NOOP;
		for (int ii=buckets.length-1; 0<=ii; --ii) {
			bucketAllocator.freeList(buckets[ii]);
			buckets[ii]=null;
		}
		if (0l==startSegment) {
			return;
		}
		long start=start();
		addPrimePosition=segment(start-1l);
		primeProducer.primes(
				(prime)->addPrime(
						UnsignedLong.firstSievePosition(prime, start),
						prime),
				UnsignedLong.min(UnsignedLong.MAX_PRIME, start-1l),
				progress);
	}
	
	private long segment(long number) {
		return (number-1l)>>>segmentSizeLog2;
	}
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
		this.operationCounter=operationCounter;
		addPrimePosition=segment(start);
		if (0==addPrimePosition) {
			return;
		}
		for (int ii=smallPrimes.size()-1; 0<=ii; --ii) {
			long position=smallPrimePositions.get(ii);
			position=sieve(end, operationCounter, position,
					UnsignedLong.unsignedInt(smallPrimes.get(ii)),
					sieveTable);
			smallPrimePositions.set(ii, position);
		}
		int index=bucketIndex(addPrimePosition-1l, addPrimePosition);
		Bucket bucket=buckets[index];
		buckets[index]=null;
		while (null!=bucket) {
			Bucket bucket2=bucket;
			bucket=bucket.next;
			for (int ii=bucket2.size-1; 0<=ii; --ii) {
				long position=bucket2.positions[ii];
				int prime1=bucket2.primes[ii];
				position=sieve(end, operationCounter, position,
						UnsignedLong.unsignedInt(prime1), sieveTable);
				add(bucketIndex(addPrimePosition, segment(position)),
						position, prime1);
			}
			bucketAllocator.freeBucket(bucket2);
		}
	}
}
