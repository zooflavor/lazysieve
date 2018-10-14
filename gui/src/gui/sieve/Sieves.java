package gui.sieve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sieves {
	public static List<SieveCheckFactory> CHECKS;
	public static List<SieveMeasureFactory> MEASURES;
	
	static {
		List<SieveCheckFactory> checks=new ArrayList<>();
		checks.addAll(BucketSieve.CHECKS);
		checks.addAll(QueueSieve.CHECKS);
		checks.addAll(SieveOfEratosthenes.CHECKS);
		checks.addAll(TrialDivision.CHECKS);
		CHECKS=Collections.unmodifiableList(checks);
		
		List<SieveMeasureFactory> measures=new ArrayList<>();
		measures.addAll(BucketSieve.MEASURES);
		measures.addAll(QueueSieve.MEASURES);
		measures.addAll(SieveOfEratosthenes.MEASURES);
		measures.addAll(TrialDivision.MEASURES);
		MEASURES=Collections.unmodifiableList(measures);
	}
	
	private Sieves() {
	}
}
