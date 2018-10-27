package gui.sieve.eratosthenes;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.PrimeQueue;
import gui.sieve.SieveCheckFactory;
import gui.sieve.SieveMeasureFactory;
import gui.sieve.SieveTable;
import gui.ui.progress.Progress;
import gui.util.IntList;
import gui.util.LongList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QueueSieve extends EratosthenesianSieve {
	public static final List<SieveCheckFactory> CHECKS
			=Collections.unmodifiableList(Arrays.asList(
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-bin.heap",
							()->new QueueSieve(false),
							16),
					SieveCheckFactory.create(
							"Sieve of Eratosthenes-bin.heap-in-place",
							()->new QueueSieve(true),
							16)));
	public static final List<SieveMeasureFactory> MEASURES
			=Collections.unmodifiableList(Arrays.asList(
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-bin.heap",
							false,
							()->new QueueSieve(false),
							24, 1, 10),
					SieveMeasureFactory.create(
							"Sieve of Eratosthenes-bin.heap-in-place",
							false,
							()->new QueueSieve(true),
							24, 1, 10)));
	
	private final PrimeQueue queue
			=new PrimeQueue(UnsignedLong.MAX_PRIME_COUNT);
	private final boolean replace;
	private final LongList smallPrimePositions=new LongList();
	private final IntList smallPrimes=new IntList();
	
	public QueueSieve(boolean replace) {
		super(5l);
		this.replace=replace;
	}
	
	@Override
	protected void addPrime(long end, OperationCounter operationCounter,
			long position, long prime, SieveTable sieveTable, long start)
			throws Throwable {
		addPrime(position, prime);
	}
	
	private void addPrime(long position, long prime) {
		if (2l*prime<=segmentSize) {
			smallPrimePositions.add(position);
			smallPrimes.add((int)prime);
		}
		else {
			queue.add(position, (int)prime);
		}
	}
	
	@Override
	protected void reset(PrimesProducer primesProducer,
			Progress progress) throws Throwable {
		queue.clear();
		smallPrimePositions.clear();
		smallPrimes.clear();
		if (0l==startSegment) {
			queue.add(9l, 3);
			return;
		}
		long start=start();
		primesProducer.primes(
				(prime)->addPrime(
						UnsignedLong.firstSievePosition(prime, start),
						prime),
				UnsignedLong.min(UnsignedLong.MAX_PRIME, start-1),
				progress);
	}
	
	@Override
	protected void sieveSegment(long end, OperationCounter operationCounter,
			SieveTable sieveTable, long start) throws Throwable {
		queue.operationCounter=operationCounter;
		for (int ii=smallPrimes.size()-1; 0<=ii; --ii) {
			long position=smallPrimePositions.get(ii);
			position=sieve(end, operationCounter, position,
					UnsignedLong.unsignedInt(smallPrimes.get(ii)),
					sieveTable);
			smallPrimePositions.set(ii, position);
		}
		while (true) {
			long position=queue.peekPosition();
			if (0>=Long.compareUnsigned(end, position)) {
				break;
			}
			int prime1=queue.peekPrime();
			position=sieve(end, operationCounter, position,
					UnsignedLong.unsignedInt(prime1), sieveTable);
			if (replace) {
				queue.replacePosition(position);
			}
			else {
				queue.remove();
				queue.add(position, prime1);
			}
		}
	}
}