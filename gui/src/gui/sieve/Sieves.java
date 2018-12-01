package gui.sieve;

import gui.Command;
import gui.check.CheckSieve;
import gui.sieve.eratosthenes.BucketSieve;
import gui.sieve.eratosthenes.CacheOptimizedLinearSieve;
import gui.sieve.eratosthenes.SimpleBucketSieve;
import gui.sieve.eratosthenes.QueueSieve;
import gui.sieve.eratosthenes.SieveOfEratosthenes;
import gui.test.NoOpSieve;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sieves {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("check"),
									Command.Argument.constant("sieve"),
									Command.Argument.PATH,
									Command.Argument.STRING,
									Command.Argument.LONG,
									Command.Argument.LONG),
							CheckSieve::checkSieve,
							"Main check sieve [adatbázis könyvtár] [szita] [kezdet] [vég]",
							null),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("describe"),
									Command.Argument.constant("sieves")),
							Sieves::describeSieves,
							"Main describe sieves",
							null),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("list"),
									Command.Argument.constant("sieves")),
							Sieves::listSieves,
							"Main list sieves",
							null)));
	public static List<Sieve.Descriptor> SIEVES;
	
	static {
		List<Sieve.Descriptor> sieves=new ArrayList<>();
		sieves.addAll(BucketSieve.SIEVES);
		sieves.addAll(CacheOptimizedLinearSieve.SIEVES);
		sieves.addAll(NoOpSieve.SIEVES);
		sieves.addAll(QueueSieve.SIEVES);
		sieves.addAll(SieveOfAtkin.SIEVES);
		sieves.addAll(SieveOfEratosthenes.SIEVES);
		sieves.addAll(SimpleBucketSieve.SIEVES);
		sieves.addAll(TrialDivision.SIEVES);
		sieves.sort((f0, f1)->f0.longName.compareTo(f1.longName));
		SIEVES=Collections.unmodifiableList(new ArrayList<>(sieves));
	}
	
	private Sieves() {
	}
	
	public static void describeSieves(List<Object> arguments)
			throws Throwable {
		Sieves.SIEVES.forEach((sieve)->
				System.out.println(sieve.shortName+" - "+sieve.longName));
	}
	
	public static void listSieves(List<Object> arguments) throws Throwable {
		Sieves.SIEVES.forEach((sieve)->System.out.println(sieve.shortName));
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
