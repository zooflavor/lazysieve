package gui.sieve.eratosthenes;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.Sieve;
import gui.sieve.SieveTable;
import gui.ui.progress.Progress;
import gui.util.IntList;
import gui.util.LongList;
import gui.util.QuickSort;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CacheOptimizedLinearSieve extends SegmentedEratosthenesianSieve {
	public static final List<Sieve.Descriptor> SIEVES
			=Collections.unmodifiableList(Arrays.asList(
					new Sieve.Descriptor(
							CacheOptimizedLinearSieve::new,
							"Cache-optimalizált lineáris szita",
							"cols",
							30, 6, 20)));
	
	private int[] brokenBuckets;
	private int[][] bucketStarts;
	private int circlesMax;
	private int circlesSetUp;
	private int[] circleStarts;
	private boolean cols;
	private int[] currentBuckets;
	private final LongList positions
			=new LongList(UnsignedLong.MAX_PRIME_COUNT);
	private final IntList primes=new IntList(UnsignedLong.MAX_PRIME_COUNT);
	private int sqrtIndex;
	
	public CacheOptimizedLinearSieve() {
		super(3l);
	}
	
	@Override
	protected void addPrime(long end, OperationCounter operationCounter,
			long position, long prime, SieveTable sieveTable, long start)
			throws Throwable {
		positions.add(position);
		primes.add((int)prime);
	}
	
	private void colsSetup() throws Throwable {
		if (2>primes.size()) {
			throw new IllegalStateException();
		}
		cols=true;
		circlesMax=primeCircle(primes.size()-1);
		brokenBuckets=new int[circlesMax+2];
		bucketStarts=new int[circlesMax+2][];
		bucketStarts[0]=new int[0];
		bucketStarts[circlesMax+1]=new int[0];
		circleStarts=new int[circlesMax+2];
		currentBuckets=new int[circlesMax+2];
		circleStarts[0]=0;
		circleStarts[circlesMax+1]=primes.size();
		colsSetupCircles(primeCircle(0), 0, circlesMax, primes.size()-1);
		circlesSetUp=0;
	}
	
	private void colsSetupBuckets(long start) {
		while (circlesMax>circlesSetUp) {
			int circle=circlesSetUp+1;
			int circleStart=circleStarts[circle];
			if (circleStart>=circleStarts[circle+1]) {
				throw new IllegalStateException();
			}
			long prime=UnsignedLong.unsignedInt(primes.get(circleStart));
			if (0>=Long.compareUnsigned(start+segmentSize,
					UnsignedLong.square(prime))) {
				break;
			}
			colsSetupBuckets(circle, start);
			circlesSetUp=circle;
		}
	}
	
	private void colsSetupBuckets(int circle, long start) {
		int circleEnd=circleStarts[circle+1];
		int circleStart=circleStarts[circle];
		for (int primeIndex=circleStart; circleEnd>primeIndex; ++primeIndex) {
			long prime=UnsignedLong.unsignedInt(primes.get(primeIndex));
			long position=prime*Long.divideUnsigned(start, prime);
			while ((0l==(position&1l))
					|| (0<Long.compareUnsigned(start, position))) {
				position+=prime;
			}
			positions.set(primeIndex, position);
		}
		currentBuckets[circle]=0;
		bucketStarts[circle]=new int[circle+1];
		bucketStarts[circle][0]=circleStart;
		colsSetupBuckets(start, circle,
				0, circleStart,
				circle+1, circleEnd);
		brokenBuckets[circle]=circle;
		for (int bucket=circle;
				(0<bucket) && (circleEnd==bucketStarts[circle][bucket]);
				--bucket) {
			bucketStarts[circle][bucket]=circleStart;
			--brokenBuckets[circle];
		}
	}
	
	private void colsSetupBuckets(long start, int circle, int leftBucket,
			int leftIndex, int rightBucket, int rightIndex) {
		if (leftBucket+1>=rightBucket) {
			return;
		}
		int middleBucket=(leftBucket+rightBucket)/2;
		int middleIndex=QuickSort.split(
				leftIndex,
				(index)->middleBucket<=primeBucket(index, start),
				this::swap,
				rightIndex);
		for (int ii=leftIndex; middleIndex>ii; ++ii) {
			if (middleBucket<=primeBucket(ii, start)) {
				throw new RuntimeException();
			}
		}
		for (int ii=middleIndex; rightIndex>ii; ++ii) {
			if (middleBucket>primeBucket(ii, start)) {
				throw new RuntimeException();
			}
		}
		bucketStarts[circle][middleBucket]=middleIndex;
		colsSetupBuckets(start, circle,
				leftBucket, leftIndex,
				middleBucket, middleIndex);
		colsSetupBuckets(start, circle,
				middleBucket, middleIndex,
				rightBucket, rightIndex);
	}
	
	private void colsSetupCircles(int leftCircle, int leftIndex,
			int rightCircle, int rightIndex) {
		if (leftCircle>=rightCircle) {
			return;
		}
		if (leftIndex+1==rightIndex) {
			for (int cc=rightCircle; leftCircle<cc; --cc) {
				circleStarts[cc]=rightIndex;
			}
			return;
		}
		int middleIndex=(leftIndex+rightIndex)/2;
		int middleCircle=primeCircle(middleIndex);
		colsSetupCircles(leftCircle, leftIndex, middleCircle, middleIndex);
		colsSetupCircles(middleCircle, middleIndex, rightCircle, rightIndex);
	}
	
	private int primeBucket(int index, long start) {
		return primeBucket(positions.get(index), start);
	}
	
	private int primeBucket(long position, long start) {
		return (int)((position-start)>>>segmentSizeLog2);
	}
	
	private int primeCircle(int index) {
		return primeCircle(UnsignedLong.unsignedInt(primes.get(index)));
	}
	
	private int primeCircle(long prime) {
		return (int)((2l*prime)>>>segmentSizeLog2);
	}
	
	@Override
	protected void reset(PrimesProducer primesProducer, Progress progress)
			throws Throwable {
		brokenBuckets=null;
		bucketStarts=null;
		circleStarts=null;
		circlesMax=0;
		cols=false;
		currentBuckets=null;
		positions.clear();
		primes.clear();
		sqrtIndex=0;
		if (0l==startSegment) {
			return;
		}
		primesProducer.primes(
				(prime)->{
					positions.add(
							UnsignedLong.firstSievePosition(prime, start()));
					primes.add((int)prime);
				},
				UnsignedLong.min(UnsignedLong.MAX_PRIME,
						segmentSize*startSegment),
				progress);
		if (0>=Long.compareUnsigned(UnsignedLong.MAX_PRIME, start())) {
			colsSetup();
			colsSetupBuckets(start());
		}
	}
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
		if (!cols) {
			if (0<Long.compareUnsigned(UnsignedLong.MAX_PRIME, start)) {
				while ((primes.size()>sqrtIndex)
						&& (0<=Long.compareUnsigned(end,
								UnsignedLong.square(UnsignedLong.unsignedInt(
										primes.get(sqrtIndex)))))) {
					++sqrtIndex;
				}
				for (int ii=0; sqrtIndex>ii; ++ii) {
					operationCounter.increment();
					long position=positions.get(ii);
					if (0<Long.compareUnsigned(end, position)) {
						position=sieve(end, operationCounter, position,
								UnsignedLong.unsignedInt(primes.get(ii)),
								sieveTable);
						positions.set(ii, position);
					}
				}
				return;
			}
			colsSetup();
		}
		colsSetupBuckets(start);
		for (int ii=0; circleStarts[1]>ii; ++ii) {
			operationCounter.increment();
			long position=positions.get(ii);
			if (0<Long.compareUnsigned(end, position)) {
				position=sieve(end, operationCounter, position,
						UnsignedLong.unsignedInt(primes.get(ii)),
						sieveTable);
				positions.set(ii, position);
			}
		}
		for (int circle=1; circlesSetUp>=circle; ++circle) {
			operationCounter.increment();
			int bucket=currentBuckets[circle];
			int nextBucket=bucket+1;
			if (circle<nextBucket) {
				nextBucket=0;
			}
			int bucketEnd=bucketStarts[circle][nextBucket];
			int bucketStart=bucketStarts[circle][bucket];
			if (brokenBuckets[circle]==bucket) {
				int circleEnd=circleStarts[circle+1];
				int circleStart=circleStarts[circle];
				for (int primeIndex=bucketStart;
						bucketEnd!=primeIndex; ) {
					operationCounter.increment();
					long position=positions.get(primeIndex);
					sieveTable.setComposite(position);
					position+=2l*UnsignedLong.unsignedInt(
							primes.get(primeIndex));
					positions.set(primeIndex, position);
					int newBucket=bucket+primeBucket(position, start);
					if (circle<newBucket) {
						newBucket-=circle+1;
					}
					if (bucket!=newBucket) {
						if (bucketStart!=primeIndex) {
							swap(bucketStart, primeIndex);
						}
						++bucketStart;
						if (circleEnd<=bucketStart) {
							bucketStart=circleStart;
							brokenBuckets[circle]
									=(0==bucket)?circle:(bucket-1);
						}
					}
					++primeIndex;
					if (circleEnd<=primeIndex) {
						primeIndex=circleStart;
					}
				}
			}
			else {
				for (int primeIndex=bucketStart;
						bucketEnd>primeIndex;
						++primeIndex) {
					operationCounter.increment();
					long position=positions.get(primeIndex);
					sieveTable.setComposite(position);
					position+=2l*UnsignedLong.unsignedInt(
							primes.get(primeIndex));
					positions.set(primeIndex, position);
					int newBucket=bucket+primeBucket(position, start);
					if (circle<newBucket) {
						newBucket-=circle+1;
					}
					if (bucket!=newBucket) {
						if (bucketStart!=primeIndex) {
							swap(bucketStart, primeIndex);
						}
						++bucketStart;
					}
				}
			}
			bucketStarts[circle][bucket]=bucketStart;
			++bucket;
			if (circle<bucket) {
				bucket=0;
			}
			currentBuckets[circle]=bucket;
		}
	}
	
	private void swap(int index0, int index1) {
		positions.swap(index0, index1);
		primes.swap(index0, index1);
	}
}
