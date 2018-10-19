package gui.sieve;

import gui.sieve.atkin.SieveOfAtkin;
import gui.sieve.eratosthenes.BucketSieveOfEratosthenes;
import gui.sieve.eratosthenes.QueueSieveOfEratosthenes;
import gui.sieve.eratosthenes.SieveOfEratosthenes;
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
		checks.addAll(BucketSieveOfEratosthenes.CHECKS);
		checks.addAll(QueueSieveOfEratosthenes.CHECKS);
		checks.addAll(TrialDivision.CHECKS);
		CHECKS=Collections.unmodifiableList(checks);
		
		List<SieveMeasureFactory> measures=new ArrayList<>();
		measures.addAll(SieveOfAtkin.MEASURES);
		measures.addAll(SieveOfEratosthenes.MEASURES);
		measures.addAll(BucketSieveOfEratosthenes.MEASURES);
		measures.addAll(QueueSieveOfEratosthenes.MEASURES);
		measures.addAll(TrialDivision.MEASURES);
		MEASURES=Collections.unmodifiableList(measures);
	}
	
	private Sieves() {
	}
}
