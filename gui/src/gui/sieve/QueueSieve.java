package gui.sieve;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QueueSieve extends SegmentedSieve {
	public static final List<SieveCheckFactory> CHECKS
			=Collections.unmodifiableList(Arrays.asList(
					SieveCheckFactory.create(
							"Queue sieve",
							()->new QueueSieve(false),
							16),
					SieveCheckFactory.create(
							"Queue sieve-fix",
							()->new QueueSieve(true),
							16)));
	public static final List<SieveMeasureFactory> MEASURES
			=Collections.unmodifiableList(Arrays.asList(
					SieveMeasureFactory.create(
							"Queue sieve",
							false,
							()->new QueueSieve(false),
							24, 1, 10),
					SieveMeasureFactory.create(
							"Queue sieve-fix",
							false,
							()->new QueueSieve(true),
							24, 1, 10)));
	
	private final PrimeQueue queue
			=new PrimeQueue(UnsignedLong.MAX_PRIME_COUNT);
	private final boolean replace;
	
	public QueueSieve(boolean replace) {
		this.replace=replace;
	}
	
	@Override
	protected void addPrime(long position, int prime) {
		queue.add(position, prime);
	}
	
	@Override
	public void reset() throws Throwable {
		queue.clear();
		queue.add(9l, 3);
		start=5l;
	}
	
	@Override
	public void reset(PrimesProducer primesProducer, Progress progress,
			long start) throws Throwable {
		checkOdd(start);
		this.start=start;
		queue.clear();
		primesProducer.primes(
				(prime)->queue.add(
						UnsignedLong.firstSievePosition(
								prime, QueueSieve.this.start),
						(int)prime),
				UnsignedLong.min(UnsignedLong.MAX_PRIME, start-1),
				progress);
		if (3l==this.start) {
			queue.add(9l, 3);
			this.start=5l;
		}
	}
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable) throws Throwable {
		queue.operationCounter=operationCounter;
		while (true) {
			long position=queue.peekPosition();
			if (0>=Long.compare(end, position)) {
				break;
			}
			int prime1=queue.peekPrime();
			long prime2=2l*UnsignedLong.unsignedInt(prime1);
			for (; 0<Long.compareUnsigned(end, position);
					position+=prime2) {
				operationCounter.increment();
				sieveTable.setComposite(position);
			}
			if (replace) {
				queue.replacePosition(position);
			}
			else {
				queue.remove();
				queue.add(position, prime1);
			}
		}
	}
	
	@Override
	public long start() {
		return start;
	}
}
