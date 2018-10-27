package gui.sieve.eratosthenes;

public class Bucket {
	public static final int BUCKET_SIZE=1000;
	
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
	
	public boolean isFull() {
		return BUCKET_SIZE<=size;
	}
}
