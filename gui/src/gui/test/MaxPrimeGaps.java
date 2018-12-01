package gui.test;

import gui.Command;
import gui.io.AggregateBlock;
import gui.io.AggregatesConsumer;
import gui.io.AggregatesReader;
import gui.io.CSVWriter;
import gui.io.Database;
import gui.ui.progress.PrintStreamProgress;
import gui.ui.progress.Progress;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MaxPrimeGaps {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("test"),
									Command.Argument.constant("max"),
									Command.Argument.constant("prime"),
									Command.Argument.constant("gaps"),
									Command.Argument.PATH,
									Command.Argument.PATH),
							MaxPrimeGaps::maxPrimeGaps,
							"Main test max prime gaps [adatbázis könytár] [kimeneti fájl]",
							null)));
	
	public static void maxPrimeGaps(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(4));
		Map<Long, Long> gapStarts=new HashMap<>();
		try (AggregatesReader aggregates=database.aggregatesReader()) {
			aggregates.consume(true,
					new AggregatesConsumer() {
						private long maxPrime;
						
						@Override
						public void consume(AggregateBlock aggregateBlock,
								Progress progress) throws Throwable {
							if (0!=aggregateBlock.get().maxPrime) {
								if (0!=maxPrime) {
									gapStarts.putIfAbsent(
										aggregateBlock.get().minPrime-maxPrime,
										maxPrime);
								}
								maxPrime=aggregateBlock.get().maxPrime;
							}
							aggregateBlock.get().primeGapStarts.forEach(
									gapStarts::putIfAbsent);
						}
					},
					null,
					new PrintStreamProgress(false, System.out));
		}
		Map<Long, Long> startGaps=new TreeMap<>();
		gapStarts.forEach((gap, start)->startGaps.put(start, gap));
		try (CSVWriter writer=CSVWriter.open((Path)arguments.get(5))) {
			writer.write(Arrays.asList("prime", "prime gap"));
			long maxGap=0l;
			for (Map.Entry<Long, Long> entry: startGaps.entrySet()) {
				Long start=entry.getKey();
				Long gap=entry.getValue();
				if (gap>maxGap) {
					maxGap=gap;
					writer.write(Arrays.asList(
							Long.toUnsignedString(start),
							Long.toUnsignedString(gap)));
				}
			}
		}
	}
}
