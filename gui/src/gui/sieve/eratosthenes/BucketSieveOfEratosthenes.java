package gui.sieve.eratosthenes;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.SieveCheckFactory;
import gui.sieve.SieveMeasureFactory;
import gui.sieve.SieveTable;
import gui.ui.progress.Progress;
import gui.util.IntList;
import gui.util.LongList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BucketSieveOfEratosthenes extends EratosthenesianSieve {
	private static final int BUCKET_SIZE=1000;
	
	public static final List<SieveCheckFactory> CHECKS
			=Collections.unmodifiableList(Arrays.asList(
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets",
							Bucket1Sieve::new,
							24),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets-2^1",
							()->new BucketNSieve(1),
							24),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets-2^2",
							()->new BucketNSieve(2),
							24),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets-2^3",
							()->new BucketNSieve(3),
							24),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets-2^4",
							()->new BucketNSieve(4),
							24),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets-2^5",
							()->new BucketNSieve(5),
							24),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets-2^6",
							()->new BucketNSieve(6),
							24),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets-2^7",
							()->new BucketNSieve(7),
							24),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-buckets-2^8",
							()->new BucketNSieve(8),
							24)));
	public static final List<SieveMeasureFactory> MEASURES
			=Collections.unmodifiableList(Arrays.asList(
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets",
							false,
							Bucket1Sieve::new,
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets-2^1",
							false,
							()->new BucketNSieve(1),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets-2^2",
							false,
							()->new BucketNSieve(2),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets-2^3",
							false,
							()->new BucketNSieve(3),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets-2^4",
							false,
							()->new BucketNSieve(4),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets-2^5",
							false,
							()->new BucketNSieve(5),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets-2^6",
							false,
							()->new BucketNSieve(6),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets-2^7",
							false,
							()->new BucketNSieve(7),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-buckets-2^8",
							false,
							()->new BucketNSieve(8),
							30, 1, 20)));
	
	private static class Bucket {
		public Bucket next;
		public final long[] positions=new long[BUCKET_SIZE];
		public final int[] primes=new int[BUCKET_SIZE];
		public int size;
		
		public Bucket(Bucket next) {
			this.next=next;
		}
		
		public Bucket clear(Bucket next) {
			this.next=next;
			size=0;
			return this;
		}
	}
	
	public static class Bucket1Sieve extends BucketSieveOfEratosthenes {
		public Bucket1Sieve() {
			super(64);
		}
		
		@Override
		protected int bucketIndex(long current, long position) {
			return Long.numberOfLeadingZeros(current^position);
		}
	}
	
	public static class BucketNSieve extends BucketSieveOfEratosthenes {
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
	private final Bucket[] buckets;
	private Bucket freeList;
	private OperationCounter operationCounter=OperationCounter.NOOP;
	private int segmentSizeLog2;
	private final LongList smallPrimePositions=new LongList();
	private final IntList smallPrimes=new IntList();
	
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BucketSieveOfEratosthenes(int buckets) {
		super(3l);
		this.buckets=new Bucket[buckets];
	}
	
	private void add(int bucket, long position, int prime) {
		operationCounter.increment();
		Bucket bucket2=buckets[bucket];
		if ((null==bucket2)
				|| (BUCKET_SIZE<=bucket2.size)) {
			bucket2=allocate(bucket2);
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
	
	private Bucket allocate(Bucket next) {
		if (null==freeList) {
			return new Bucket(next);
		}
		Bucket bucket=freeList;
		freeList=freeList.next;
		return bucket.clear(next);
	}
	
	protected abstract int bucketIndex(long current, long position);
	
	private void freeBucket(Bucket bucket) {
		freeList=bucket.clear(freeList);
	}
	
	@Override
	protected void reset(PrimesProducer primesProducer, Progress progress)
			throws Throwable {
		segmentSizeLog2=Long.numberOfTrailingZeros(segmentSize);
		for (int ii=buckets.length-1; 0<=ii; --ii) {
			while (null!=buckets[ii]) {
				Bucket bucket=buckets[ii];
				buckets[ii]=bucket.next;
				freeBucket(bucket);
			}
		}
		if (0l==startSegment) {
			return;
		}
		long start=start();
		addPrimePosition=segment(start-1l);
		primesProducer.primes(
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
				add(bucketIndex(addPrimePosition, segment(position)), position,
						prime1);
			}
			freeBucket(bucket2);
		}
	}
}
