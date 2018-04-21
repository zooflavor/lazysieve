package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.BigInteger;

public class Info {
	public final BigInteger end;
	public final long position;
	public final BigInteger start;
	
	public Info(BigInteger end, long position, BigInteger start) {
		this.end=end;
		this.position=position;
		this.start=start;
	}
	
	private static String pad(int length, String string) {
		while (string.length()<length) {
			string="\u0020"+string;
		}
		return string;
	}
	
	public void print(boolean machine, PrintWriter writer) throws IOException {
		if (machine) {
			writer.println(String.format("end\t%1$s", end));
			writer.println(String.format("position\t%1$s", position));
			writer.println(String.format("start\t%1$s", start));
		}
		else {
			String endString=String.format("%1$,d", end);
			String positionString=String.format("%1$,d", position);
			String startString=String.format("%1$,d", start);
			int length=Math.max(
					Math.max(endString.length(),
							positionString.length()),
					startString.length());
			endString=pad(length, endString);
			positionString=pad(length, positionString);
			startString=pad(length, startString);
			writer.println(String.format("start:    %1$s", startString));
			writer.println(String.format("end:      %1$s", endString));
			writer.println(String.format("position: %1$s", positionString));
		}
	}
	
	public static Info read(RandomAccessFile file) throws IOException {
		file.seek(0l);
		BigInteger start=BigInteger.ZERO;
		while (true) {
			byte next=file.readByte();
			start=start.shiftLeft(7).or(BigInteger.valueOf((next&0x7f)<<1));
			if (0==(next&0x80)) {
				break;
			}
		}
		start=start.or(BigInteger.ONE);
		long position=file.getFilePointer();
		BigInteger end=start.add(
				BigInteger.valueOf(file.length()-position)
						.shiftLeft(4));
		return new Info(end, position, start);
	}
}
