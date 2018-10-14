package gui.sieve;

public interface OperationCounter {
	static OperationCounter COUNTER=new OperationCounter() {
		private long count;
		
		@Override
		public void add(long operations) {
			count+=operations;
		}
		
		@Override
		public long get() {
			return count;
		}
		
		@Override
		public void increment() {
			++count;
		}
		
		@Override
		public void reset() {
			count=0l;
		}
	};
	
	static OperationCounter NOOP=new OperationCounter() {
		@Override
		public void add(long operations) {
		}
		
		@Override
		public long get() {
			return 0l;
		}
		
		@Override
		public void increment() {
		}
		
		@Override
		public void reset() {
		}
	};
	
	void add(long operations);
	long get();
	void increment();
	void reset();
}
