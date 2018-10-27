package gui.sieve.eratosthenes;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.SieveCheckFactory;
import gui.sieve.SieveMeasureFactory;
import gui.sieve.SieveTable;
import gui.ui.progress.Progress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleBucketSieve extends EratosthenesianSieve {
	public static final List<SieveCheckFactory> CHECKS
			=Collections.unmodifiableList(Arrays.asList(
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-simple buckets",
							()->new SimpleBucketSieve(),
							24)));
	public static final List<SieveMeasureFactory> MEASURES
			=Collections.unmodifiableList(Arrays.asList(
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-simple buckets",
							false,
							()->new SimpleBucketSieve(),
							30, 1, 20)));
	
	private long addPrimePosition;
	private final BucketAllocator bucketAllocator=new BucketAllocator();
	private final Bucket[] buckets=new Bucket[64];
	private OperationCounter operationCounter;
	
	public SimpleBucketSieve() {
		super(3l);
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
		add(bucketIndex(addPrimePosition, position),
				position, (int)prime);
	}
	
	private int bucketIndex(long current, long position) {
		return Long.numberOfLeadingZeros(current^position);
	}
	
	@Override
	protected void reset(PrimesProducer primesProducer, Progress progress)
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
		addPrimePosition=start-2l;
		primesProducer.primes(
				(prime)->addPrime(
						UnsignedLong.firstSievePosition(prime, start),
						prime),
				UnsignedLong.min(UnsignedLong.MAX_PRIME, start-1l),
				progress);
	}
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
		this.operationCounter=operationCounter;
		for (; 0<Long.compareUnsigned(end, start); start+=2l) {
			operationCounter.increment();
			addPrimePosition=start;
			int index=bucketIndex(addPrimePosition-2l, addPrimePosition);
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
					add(bucketIndex(addPrimePosition, position),
							position, prime1);
				}
				bucketAllocator.freeBucket(bucket2);
			}
		}
	}
}
