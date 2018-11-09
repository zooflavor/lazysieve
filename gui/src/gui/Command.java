package gui;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@FunctionalInterface
public interface Command {
	abstract class Argument<T> {
		public static final Argument<Long> LONG=new Argument<Long>(
				"(?:[0-9]{1,19})|(?:0x[0-9a-f]{1,16})") {
			@Override
			public Long parse(String argument) throws Throwable {
				return (argument.startsWith("0x"))
						?Long.parseUnsignedLong(argument.substring(2), 16)
						:Long.parseUnsignedLong(argument, 10);
			}
		};
		public static final Argument<Path> PATH=new Argument<Path>(".*") {
			@Override
			public Path parse(String argument) throws Throwable {
				return Paths.get(argument);
			}
		};
		public static final Argument<String> STRING
				=new Argument<String>(".*") {
					@Override
					public String parse(String argument) throws Throwable {
						return argument;
					}
				};
		
		public final Pattern pattern;
		
		public Argument(Pattern pattern) {
			this.pattern=pattern;
		}
		
		public Argument(String regex) {
			this(Pattern.compile(regex));
		}
		
		public static Argument<String> constant(String constant) {
			return new Argument<String>(Pattern.quote(constant)) {
				@Override
				public String parse(String argument) throws Throwable {
					return argument;
				}
			};
		}
		
		public boolean matches(String argument) {
			return pattern.matcher(argument).matches();
		}
		
		public abstract T parse(String argument) throws Throwable;
	}
	
	class Descriptor {
		public final List<Argument<?>> arguments;
		public final Command command;
		public final String usage;
		public final Argument<?> vararg;
		
		public Descriptor(List<Argument<?>> arguments, Command command,
				String usage, Argument<?> vararg) {
			this.arguments=Collections.unmodifiableList(
					new ArrayList<>(arguments));
			this.command=command;
			this.usage=usage;
			this.vararg=vararg;
		}
		
		public Descriptor(List<Argument<?>> arguments, Command command,
				String usage) {
			this(arguments, command, usage, null);
		}
	}
	
	void execute(List<Object> arguments) throws Throwable;
}
