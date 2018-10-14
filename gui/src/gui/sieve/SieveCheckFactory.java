package gui.sieve;

import java.util.function.Supplier;

public interface SieveCheckFactory {
	Sieve create() throws Throwable;
	
	static SieveCheckFactory create(String label,
			Supplier<Sieve> sieveFactory, int smallSegmentSizeLog2) {
		return new SieveCheckFactory() {
			@Override
			public Sieve create() throws Throwable {
				return sieveFactory.get();
			}

			@Override
			public int smallSegmentSizeLog2() {
				return smallSegmentSizeLog2;
			}
			
			@Override
			public String toString() {
				return label;
			}
		};
	}
	
	int smallSegmentSizeLog2();
}
