package gui.sieve;

import gui.util.BinaryMinHeap;
import java.util.Arrays;

public class PrimeQueue extends BinaryMinHeap {
	public OperationCounter operationCounter=OperationCounter.NOOP;
	private long[] positions;
	private int[] primes;
	
	public PrimeQueue(int expectedSize) {
		expectedSize=Math.max(1, expectedSize);
		positions=new long[expectedSize];
		primes=new int[expectedSize];
	}
	
	public void add(long position, int prime) {
		if (heapSize>=positions.length) {
			long[] positions2=Arrays.copyOf(positions, 2*positions.length);
			int[] primes2=Arrays.copyOf(primes, 2*primes.length);
			positions=positions2;
			primes=primes2;
		}
		int index=heapSize;
		positions[index]=position;
		primes[index]=prime;
		++heapSize;
		fixUp(index);
	}
	
	@Override
	protected int compare(int index0, int index1) {
		return Long.compareUnsigned(positions[index0], positions[index1]);
	}
	
	public long peekPosition() {
		checkNotEmpty();
		return positions[0];
	}
	
	public int peekPrime() {
		checkNotEmpty();
		return primes[0];
	}
	
	public void remove() {
		checkNotEmpty();
		--heapSize;
		positions[0]=positions[heapSize];
		primes[0]=primes[heapSize];
		fixDown(0);
	}
	
	public void replacePosition(long position) {
		checkNotEmpty();
		positions[0]=position;
		fixDown(0);
	}
	
	@Override
	protected void swap(int index0, int index1) {
		long temp0=positions[index0];
		positions[index0]=positions[index1];
		positions[index1]=temp0;
		int temp1=primes[index0];
		primes[index0]=primes[index1];
		primes[index1]=temp1;
		operationCounter.increment();
	}
	
	@Override
	protected void toString(int index, StringBuilder stringBuilder) {
		stringBuilder.append(primes[index]);
		stringBuilder.append(": ");
		stringBuilder.append(positions[index]);
	}
}
