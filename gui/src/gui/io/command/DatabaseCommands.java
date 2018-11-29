package gui.io.command;

import gui.Command;
import gui.io.Database;
import gui.io.DatabaseInfo;
import gui.ui.progress.PrintStreamProgress;
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
							"Main database [adatbázis könyvtár] import aggregates [összesítőfájl]"),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("database"),
									Command.Argument.PATH,
									Command.Argument.constant("info")),
							DatabaseCommands::info,
							"Main database [adatbázis könyvtár] info"),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("database"),
									Command.Argument.PATH,
									Command.Argument.constant("info"),
									Command.Argument.constant("crunch"),
									Command.Argument.LONG),
							DatabaseCommands::info,
							"Main database [adatbázis könyvtár] info crunch [szegmensek száma]"),
					new Command.Descriptor(
							Arrays.asList(
									Command.Argument.constant("database"),
									Command.Argument.PATH,
									Command.Argument.constant("reaggregate")),
							DatabaseCommands::reaggregate,
							"Main database [adatbázis könyvtár] reaggregate")));
	
	private DatabaseCommands() {
	}
	
	public static void importAggregates(List<Object> arguments)
			throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		database.importAggregates((Path)arguments.get(4),
				new PrintStreamProgress(false, System.out));
	}
	
	public static void info(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		Long crunchSegments=null;
		if (5==arguments.size()) {
			crunchSegments=(Long)arguments.get(4);
		}
		DatabaseInfo info=database.info(
				new PrintStreamProgress(false, System.out));
		info.output(crunchSegments, database, true).forEach((output)->{
			if (null==output) {
				System.out.println();
			}
			else {
				System.out.println(output.key+": "+output.value);
			}
		});
	}
	
	public static void reaggregate(List<Object> arguments) throws Throwable {
		Database database=new Database((Path)arguments.get(1));
		database.reaggregate(new PrintStreamProgress(false, System.out));
	}
}
