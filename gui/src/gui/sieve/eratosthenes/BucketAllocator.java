package gui.sieve.eratosthenes;

public class BucketAllocator {
	private Bucket freeList;
	
	public Bucket allocate(Bucket next) {
		if (null==freeList) {
			return new Bucket(next);
		}
		Bucket bucket=freeList;
		freeList=freeList.next;
		return bucket.clear(next);
	}
	
	public void freeBucket(Bucket bucket) {
		freeList=bucket.clear(freeList);
	}
	
	public void freeList(Bucket bucket) {
		while (null!=bucket) {
			Bucket bucket2=bucket;
			bucket=bucket.next;
			freeBucket(bucket2);
		}
	}
}
