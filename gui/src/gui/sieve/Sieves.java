package gui.sieve;

import gui.sieve.atkin.SieveOfAtkin;
import gui.sieve.eratosthenes.BucketSieve;
import gui.sieve.eratosthenes.CacheOptimizedLinearSieve;
import gui.sieve.eratosthenes.QueueSieve;
import gui.sieve.eratosthenes.SieveOfEratosthenes;
import gui.sieve.eratosthenes.SimpleBucketSieve;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sieves {
	public static List<SieveCheckFactory> CHECKS;
	public static List<SieveMeasureFactory> MEASURES;
	
	static {
		List<SieveCheckFactory> checks=new ArrayList<>();
		checks.addAll(SieveOfAtkin.CHECKS);
		checks.addAll(SieveOfEratosthenes.CHECKS);
		checks.addAll(SimpleBucketSieve.CHECKS);
		checks.addAll(BucketSieve.CHECKS);
		checks.addAll(CacheOptimizedLinearSieve.CHECKS);
		checks.addAll(QueueSieve.CHECKS);
		checks.addAll(TrialDivision.CHECKS);
		CHECKS=Collections.unmodifiableList(checks);
		
		List<SieveMeasureFactory> measures=new ArrayList<>();
		measures.addAll(SieveOfAtkin.MEASURES);
		measures.addAll(SieveOfEratosthenes.MEASURES);
		measures.addAll(SimpleBucketSieve.MEASURES);
		measures.addAll(BucketSieve.MEASURES);
		measures.addAll(CacheOptimizedLinearSieve.MEASURES);
		measures.addAll(QueueSieve.MEASURES);
		measures.addAll(TrialDivision.MEASURES);
		MEASURES=Collections.unmodifiableList(measures);
	}
	
	private Sieves() {
	}
}
