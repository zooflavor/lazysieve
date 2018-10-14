package gui.io;

import gui.ui.progress.Progress;
import gui.util.IntList;

public class SmallPrimesProducer extends PrimesCache {
	public SmallPrimesProducer() {
		super(Database.SMALL_PRIMES_MAX);
	}
	
	@Override
	protected IntList primes(Progress progress) throws Throwable {
		return Database.smallPrimes(progress);
	}
}
