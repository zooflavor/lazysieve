package gui.test;

import gui.Command;
import gui.io.AggregateBlock;
import gui.io.AggregatesConsumer;
import gui.io.AggregatesReader;
import gui.io.CSVWriter;
import gui.io.Database;
import gui.ui.progress.PrintStreamProgress;
import gui.ui.progress.Progress;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrimeCounts {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("test"),
									Command.Argument.constant("prime"),
									Command.Argument.constant("counts"),
									Command.Argument.PATH,
									Command.Argument.PATH),
							PrimeCounts::primeCounts,
							"Main test prime counts [adatbázis könytár] [kimeneti fájl]",
							null)));
	
	public static void primeCounts(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(3));
		try (AggregatesReader aggregates=database.aggregatesReader();
				CSVWriter writer=CSVWriter.open((Path)arguments.get(4))) {
			writer.write(Arrays.asList("n", "pi(n)"));
			aggregates.consume(true,
					new AggregatesConsumer() {
						private long count=1l;
						
						@Override
						public void consume(AggregateBlock aggregateBlock,
								Progress progress) throws Throwable {
							long end=aggregateBlock.get().segmentEnd-1l;
							count+=aggregateBlock.get().primeCount4Z1
									+aggregateBlock.get().primeCount4Z3;
							if (end==Long.highestOneBit(end)) {
								try {
									writer.write(Arrays.asList(
											Long.toUnsignedString(end),
											Long.toUnsignedString(count)));
								}
								catch (IOException ex) {
									throw new RuntimeException(ex);
								}
							}
						}
					},
					null,
					new PrintStreamProgress(false, System.out));
		}
	}
}
