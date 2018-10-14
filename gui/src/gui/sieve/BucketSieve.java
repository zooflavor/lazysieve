package gui.sieve;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BucketSieve extends SegmentedSieve {
	private static final int BUCKET_SIZE=1000;
	
	public static final List<SieveCheckFactory> CHECKS
			=Collections.unmodifiableList(Arrays.asList(
					SieveCheckFactory.create(
							"Bucket sieve",
							Bucket1Sieve::new,
							24),
					SieveCheckFactory.create(
							"Bucket sieve-2^1",
							()->new BucketNSieve(1),
							24),
					SieveCheckFactory.create(
							"Bucket sieve-2^2",
							()->new BucketNSieve(2),
							24),
					SieveCheckFactory.create(
							"Bucket sieve-2^3",
							()->new BucketNSieve(3),
							24),
					SieveCheckFactory.create(
							"Bucket sieve-2^4",
							()->new BucketNSieve(4),
							24),
					SieveCheckFactory.create(
							"Bucket sieve-2^5",
							()->new BucketNSieve(5),
							24),
					SieveCheckFactory.create(
							"Bucket sieve-2^6",
							()->new BucketNSieve(6),
							24),
					SieveCheckFactory.create(
							"Bucket sieve-2^7",
							()->new BucketNSieve(7),
							24),
					SieveCheckFactory.create(
							"Bucket sieve-2^8",
							()->new BucketNSieve(8),
							24)));
	public static final List<SieveMeasureFactory> MEASURES
			=Collections.unmodifiableList(Arrays.asList(
					SieveMeasureFactory.create(
							"Bucket sieve",
							false,
							Bucket1Sieve::new,
							30, 1, 20),
					SieveMeasureFactory.create(
							"Bucket sieve-2^1",
							false,
							()->new BucketNSieve(1),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Bucket sieve-2^2",
							false,
							()->new BucketNSieve(2),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Bucket sieve-2^3",
							false,
							()->new BucketNSieve(3),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Bucket sieve-2^4",
							false,
							()->new BucketNSieve(4),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Bucket sieve-2^5",
							false,
							()->new BucketNSieve(5),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Bucket sieve-2^6",
							false,
							()->new BucketNSieve(6),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Bucket sieve-2^7",
							false,
							()->new BucketNSieve(7),
							30, 1, 20),
					SieveMeasureFactory.create(
							"Bucket sieve-2^8",
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
	
	public static class Bucket1Sieve extends BucketSieve {
		public Bucket1Sieve() {
			super(64);
		}
		
		@Override
		protected int bucketIndex(long current, long position) {
			return Long.numberOfLeadingZeros(current^position);
		}
		
		@Override
		protected long bucketSize(int index) {
			return 1l<<(63-index);
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
		
		@Override
		protected long bucketSize(int index) {
			return 1l<<(bits*(index>>>bits));
		}
	}
	
	private long addPrimePosition;
	private final Bucket[] buckets;
	private final long[] bucketSizes;
	private Bucket freeList;
	private OperationCounter operationCounter=OperationCounter.NOOP;
	
	@SuppressWarnings("OverridableMethodCallInConstructor")
	public BucketSieve(int buckets) {
		this.buckets=new Bucket[buckets];
		bucketSizes=new long[buckets];
		for (int ii=buckets-1; 0<=ii; --ii) {
			bucketSizes[ii]=UnsignedLong.max(2l, bucketSize(ii));
		}
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
	protected void addPrime(long position, int prime) {
		add(bucketIndex(addPrimePosition, position), position, prime);
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
	
	protected abstract long bucketSize(int index);
	
	private void freeBucket(Bucket bucket) {
		freeList=bucket.clear(freeList);
	}
	
	@Override
	public void reset() throws Throwable {
		for (int ii=buckets.length-1; 0<=ii; --ii) {
			while (null!=buckets[ii]) {
				Bucket bucket=buckets[ii];
				buckets[ii]=bucket.next;
				freeBucket(bucket);
			}
		}
		start=3l;
	}
	
	@Override
	public void reset(PrimesProducer primesProducer, Progress progress,
			long start) throws Throwable {
		checkOdd(start);
		reset();
		this.start=start;
		addPrimePosition=start-2l;
		primesProducer.primes(
				(prime)->addPrime(
						UnsignedLong.firstSievePosition(prime, this.start),
						(int)prime),
				UnsignedLong.min(UnsignedLong.MAX_PRIME, this.start-1l),
				progress);
	}
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable) throws Throwable {
		this.operationCounter=operationCounter;
		for (long start2=start; 0<Long.compareUnsigned(end, start2); ) {
			int index=bucketIndex(start2-2l, start2);
			Bucket bucket=buckets[index];
			if (null==bucket) {
				start2+=UnsignedLong.min(end-start2, bucketSizes[index]);
				continue;
			}
			buckets[index]=null;
			do {
				Bucket bucket2=bucket;
				bucket=bucket.next;
				for (int ii=bucket2.size-1; 0<=ii; --ii) {
					long position=bucket2.positions[ii];
					int prime1=bucket2.primes[ii];
					if (position==start2) {
						long prime2=2l*UnsignedLong.unsignedInt(prime1);
						for (; 0<Long.compareUnsigned(end, position);
								position+=prime2) {
							operationCounter.increment();
							sieveTable.setComposite(position);
						}
					}
					add(bucketIndex(start2, position), position, prime1);
				}
				freeBucket(bucket2);
			}
			while (null!=bucket);
			start2+=2l;
		}
		addPrimePosition=end-2l;
	}
	
	@Override
	public long start() {
		return start;
	}
}
