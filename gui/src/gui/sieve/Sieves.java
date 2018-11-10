package gui.sieve;

import gui.sieve.eratosthenes.BucketSieve;
import gui.sieve.eratosthenes.CacheOptimizedLinearSieve;
import gui.sieve.eratosthenes.IncrementalBucketSieve;
import gui.sieve.eratosthenes.QueueSieve;
import gui.sieve.eratosthenes.SegmentedSieveOfEratosthenes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sieves {
	public static List<Sieve.Descriptor> SIEVES;
	
	static {
		List<Sieve.Descriptor> sieves=new ArrayList<>();
		sieves.addAll(BucketSieve.SIEVES);
		sieves.addAll(CacheOptimizedLinearSieve.SIEVES);
		sieves.addAll(IncrementalBucketSieve.SIEVES);
		sieves.addAll(SieveOfAtkin.SIEVES);
		sieves.addAll(SegmentedSieveOfEratosthenes.SIEVES);
		sieves.addAll(QueueSieve.SIEVES);
		sieves.addAll(TrialDivision.SIEVES);
		sieves.sort((f0, f1)->f0.longName.compareTo(f1.longName));
		SIEVES=Collections.unmodifiableList(new ArrayList<>(sieves));
	}
	
	private Sieves() {
	}
	
	public static Sieve.Descriptor parse(String string) {
		for (Sieve.Descriptor sieve: SIEVES) {
			if (sieve.shortName.equals(string)) {
				return sieve;
			}
		}
		throw new IllegalArgumentException("ismeretlen szita "+string);
	}
}
