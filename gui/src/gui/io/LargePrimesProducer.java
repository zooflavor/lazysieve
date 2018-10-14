package gui.io;

import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import gui.util.IntList;

public class LargePrimesProducer extends PrimesCache {
	private final Database database;
	
	public LargePrimesProducer(Database database) {
		super(UnsignedLong.MAX_PRIME);
		this.database=database;
	}
	
	@Override
	protected IntList primes(Progress progress) throws Throwable {
		return database.readPrimes(progress);
	}
}
