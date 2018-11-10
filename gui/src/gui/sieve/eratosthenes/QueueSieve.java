package gui.sieve.eratosthenes;

import gui.io.PrimesProducer;
import gui.math.UnsignedLong;
import gui.sieve.OperationCounter;
import gui.sieve.PrimeQueue;
import gui.sieve.Sieve;
import gui.sieve.SieveTable;
import gui.ui.progress.Progress;
import gui.util.IntList;
import gui.util.LongList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QueueSieve extends SegmentedEratosthenesianSieve {
	public static final List<Sieve.Descriptor> SIEVES
			=Collections.unmodifiableList(Arrays.asList(
					new Sieve.Descriptor(
							()->new QueueSieve(false),
							"Eratoszthenész szitája-bin.kupac",
							"bin-heap",
							24, 1, 10),
					new Sieve.Descriptor(
							()->new QueueSieve(true),
							"Eratoszthenész szitája-bin.kupac helyben",
							"bin-heap-inplace",
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
