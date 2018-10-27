package gui;

import gui.io.DatabaseCommands;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Throwable {
		try {
			List<Command.Descriptor> commands=new ArrayList<>();
			commands.addAll(DatabaseCommands.COMMANDS);
			commands.addAll(Gui.COMMANDS);
			commands.sort((c0, c1)->c0.usage.compareTo(c1.usage));
			outer: for (Command.Descriptor command: commands) {
				if (args.length!=command.arguments.size()) {
					continue;
				}
				for (int ii=0; args.length>ii; ++ii) {
					if (!command.arguments.get(ii).pattern.matcher(args[ii])
							.matches()) {
						continue outer;
					}
				}
				List<Object> arguments=new ArrayList<>(args.length);
				for (int ii=0; args.length>ii; ++ii) {
					arguments.add(command.arguments.get(ii).parse(args[ii]));
				}
				command.command.execute(arguments);
				return;
			}
			System.out.println("usage:");
			for (Command.Descriptor command: commands) {
				System.out.println("\t"+command.usage);
			}
			System.exit(1);
		}
		catch (Throwable throwable) {
			System.out.flush();
			throwable.printStackTrace(System.err);
			System.exit(1);
		}
	}
}