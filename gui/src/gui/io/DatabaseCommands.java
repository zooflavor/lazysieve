package gui.io;

import gui.Command;
import gui.math.UnsignedLong;
import gui.ui.progress.PrintStreamProgress;
import gui.ui.progress.Progress;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatabaseCommands {
	public static final List<Command.Descriptor> COMMANDS
			=Collections.unmodifiableList(Arrays.asList(
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("database"),
									Command.Argument.PATH,
									Command.Argument.constant("import"),
									Command.Argument.constant("aggregates"),
									Command.Argument.PATH),
							DatabaseCommands::importAggregates,
							"Main database [database-directory] import aggregates [aggregates-file]"),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("database"),
									Command.Argument.PATH,
									Command.Argument.constant("crunch"),
									Command.Argument.constant("info"),
									Command.Argument.LONG),
							DatabaseCommands::crunchInfo,
							"Main database [database-directory] crunch info [segments]"),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("database"),
									Command.Argument.PATH,
									Command.Argument.constant("info")),
							DatabaseCommands::info,
							"Main database [database-directory] info"),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("database"),
									Command.Argument.PATH,
									Command.Argument.constant("reaggregate")),
							DatabaseCommands::reaggregate,
							"Main database [database-directory] reaggregate")));
	
	private DatabaseCommands() {
	}
	
	public static void crunchInfo(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		long segments=(Long)arguments.get(4);
		Database.Info info=database.info(Progress.NULL);
		Database.TypeInfo aggregatesInfo=info.aggregates;
		Database.TypeInfo segmentsInfo=info.segments;
		if ((1l<<32)>=UnsignedLong.min(
				aggregatesInfo.missingSegmentStart,
				segmentsInfo.missingSegmentStart)) {
			System.out.println(String.format(
					"init.bin %1$s",
					database.rootDirectory));
		}
		else {
			System.out.println(String.format(
					"generator.bin %1$s start 0x%2$s segments %3$s",
					database.rootDirectory,
					Long.toUnsignedString(
							aggregatesInfo.missingSegmentStart, 16),
					Long.toUnsignedString(segments)));
		}
	}
	
	public static void importAggregates(List<Object> arguments)
			throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		database.importAggregates((Path)arguments.get(4),
				new PrintStreamProgress(false, System.out));
	}
	
	public static void info(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		Database.Info info=database.info(
				new PrintStreamProgress(false, System.out));
		info(database, "segments", info.segments);
		info(database, "aggregates", info.aggregates);
		if (0l<info.numberOfNewSegments) {
			System.out.println(String.format(
					"new segments: %1$,d",
					info.numberOfNewSegments));
			System.out.println(" *** you should reaggregate the database!");
		}
	}
	
	private static void info(Database database, String type,
			Database.TypeInfo info) {
		System.out.println(String.format("%1$s:", type));
		System.out.println(String.format(
				"\tnumber of segments: %1$,d",
				info.numberOfSegments));
		if (null!=info.firstSegmentStart) {
			System.out.println(String.format(
					"\tstart of the first segment: %1$,d",
					info.firstSegmentStart));
		}
		if (null!=info.lastSegmentStart) {
			System.out.println(String.format(
					"\tstart of the last segment: %1$,d",
					info.lastSegmentStart));
		}
		if (null!=info.missingSegmentStart) {
			System.out.println(String.format(
					"\tstart of the first missing segment: %1$,d",
					info.missingSegmentStart));
		}
		if (null!=info.missingSegments) {
			System.out.println(String.format(
					"\tnumber of missing segments: %1$,d",
					info.missingSegments));
		}
		if (null!=info.missingSegmentStart) {
			if (null==info.missingSegments) {
				System.out.println(String.format(
						" ***  you could sieve from 0x%1$x",
						info.missingSegmentStart));
				System.out.println(String.format(
						" ***  generator.bin %1$s start 0x%2$x reserve-space 0x100000000",
						database.rootDirectory,
						info.missingSegmentStart));
			}
			else {
				System.out.println(String.format(
						" ***  you could sieve %1$,d segments from %2$,d",
						info.missingSegments,
						info.missingSegmentStart));
				System.out.println(String.format(
						" ***  generator.bin %1$s start 0x%2$x segments 0x%3$x",
						database.rootDirectory,
						info.missingSegmentStart,
						info.missingSegments));
			}
		}
	}
	
	public static void reaggregate(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		database.reaggregate(new PrintStreamProgress(false, System.out));
	}
}
