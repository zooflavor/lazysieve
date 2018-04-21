package tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
	private static final int PAGE_SIZE=65536;
	private static final List<BigInteger> STEPS
			=Collections.unmodifiableList(Arrays.asList(
					BigInteger.ZERO,
					BigInteger.valueOf(2),
					BigInteger.valueOf(4),
					BigInteger.valueOf(6),
					BigInteger.valueOf(8),
					BigInteger.valueOf(10),
					BigInteger.valueOf(12),
					BigInteger.valueOf(14),
					BigInteger.valueOf(16)));
	
	public static void append(Path outputFile, Path inputFile)
			throws Throwable {
		if (!Files.exists(outputFile)) {
			throw new FileNotFoundException(outputFile.toString());
		}
		try (RandomAccessFile input
						=new RandomAccessFile(inputFile.toFile(), "r");
				RandomAccessFile output
						=new RandomAccessFile(outputFile.toFile(), "rw")) {
			Info inputInfo=Info.read(input);
			Info outputInfo=Info.read(output);
			if (!outputInfo.end.equals(inputInfo.start)) {
				throw new IllegalArgumentException(String.format(
						"%1$s doesn't fit at the end of %2$s"
								+"; end %3$,d != start %4$,d",
						inputFile,
						outputFile,
						outputInfo.end,
						inputInfo.start));
			}
			output.seek(output.length());
			input.seek(inputInfo.position);
			byte[] buf=new byte[PAGE_SIZE];
			for (int rr; 0<=(rr=input.read(buf)); ) {
				output.write(buf, 0, rr);
			}
		}
	}
	
	public static void info(Path inputFile, boolean machine) throws Throwable {
		try (RandomAccessFile file
				=new RandomAccessFile(inputFile.toFile(), "r");
				PrintWriter writer=new PrintWriter(System.out)) {
			Info.read(file).print(machine, writer);
		}
	}
	
	public static void list(Path inputFile, boolean machine) throws Throwable {
		try (RandomAccessFile file
				=new RandomAccessFile(inputFile.toFile(), "r")) {
			Info info=Info.read(file);
			BigInteger number=info.start;
			file.seek(info.position);
			byte[] buf=new byte[PAGE_SIZE];
			for (int rr; 0<=(rr=file.read(buf)); ) {
				for (int ii=0; rr>ii; ++ii) {
					byte bb=buf[ii];
					for (int jj=0; 8>jj; ++jj) {
						if (0==(bb&(1<<jj))) {
							System.out.println(String.format(
									machine?"%1$s":"%1$,d",
									number.add(STEPS.get(jj))));
						}
					}
					number=number.add(STEPS.get(8));
				}
			}
		}
	}
	
    public static void main(String[] args) throws Throwable {
		try {
			boolean ok=true;
			switch (args.length) {
				case 2:
					switch (args[0]) {
						case "info":
							info(Paths.get(args[1]), false);
							break;
						case "list":
							list(Paths.get(args[1]), false);
							break;
						case "machine-info":
							info(Paths.get(args[1]), true);
							break;
						case "machine-list":
							list(Paths.get(args[1]), true);
							break;
						case "seed":
							seed(Paths.get(args[1]));
							break;
						default:
							ok=false;
							break;
					}	break;
				case 3:
					switch (args[0]) {
						case "append":
							append(Paths.get(args[1]), Paths.get(args[2]));
							break;
						default:
							ok=false;
							break;
					}	break;
				default:
					ok=false;
					break;
			}
			if (ok) {
				System.exit(0);
			}
			else {
				System.out.println("usage:");
				System.out.println(
						"\ttools.Main append output-file input-file");
				System.out.println("\ttools.Main info input-file");
				System.out.println("\ttools.Main list input-file");
				System.out.println("\ttools.Main machine-info input-file");
				System.out.println("\ttools.Main machine-list input-file");
				System.out.println("\ttools.Main seed output-file");
			}
		}
		catch (Throwable throwable) {
			throwable.printStackTrace(System.err);
		}
		System.exit(1);
    }
	
	public static void seed(Path outputFile) throws Throwable {
		if (Files.exists(outputFile)) {
			throw new IOException(String.format(
					"file %1$s already exists", outputFile));
		}
		byte[] numbers=new byte[1<<16];
		for (int ii=0; numbers.length>ii; ++ii) {
			for (int bb=0; 8>bb; ++bb) {
				if (0==(numbers[ii]&(1<<bb))) {
					int bi=(ii<<3)+bb;
					int prime=3+(bi<<1);
					bi+=prime;
					for (; (numbers.length<<3)>bi; bi+=prime) {
						numbers[bi>>3]|=1<<(bi&7);
					}
				}
			}
		}
		try (RandomAccessFile file
				=new RandomAccessFile(outputFile.toFile(), "rw")) {
			file.write(1);
			file.write(numbers);
		}
	}
}
