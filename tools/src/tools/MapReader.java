package tools;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;

public class MapReader {
	private int bits;
	private int bitsCount;
	private final byte[] buf=new byte[4096];
	private int bufCount;
	private int bufIndex;
	private final RandomAccessFile file;
	
	private MapReader(int bits, int bitsCount, RandomAccessFile file) {
		this.bits=bits;
		this.bitsCount=bitsCount;
		this.file=file;
	}
	
	public static MapReader create(Info info, RandomAccessFile file,
			BigInteger start) throws IOException {
		if (0>start.compareTo(info.start)) {
			throw new IllegalArgumentException(String.format(
					"start %1$,d is smaller than the file's start %2$,d",
					start,
					info.start));
		}
		if (0<start.compareTo(info.end)) {
			throw new IllegalArgumentException(String.format(
					"start %1$,d is larger than the file's end %2$,d",
					start,
					info.end));
		}
		BigInteger offset=start.subtract(info.start);
		file.seek(info.position+offset.shiftRight(4).longValueExact());
		int bitsCount=offset.and(BigInteger.valueOf(15l)).intValueExact()>>1;
		int bits;
		if (0<bitsCount) {
			bits=file.readByte()&0xff;
			bits>>=bitsCount;
			bitsCount=8-bitsCount;
		}
		else {
			bits=0;
		}
		return new MapReader(bits, bitsCount, file);
	}
	
	public int read() throws IOException {
		while (8>bitsCount) {
			if (bufIndex<bufCount) {
				bits|=((buf[bufIndex]&0xff)<<bitsCount);
				bitsCount+=8;
				++bufIndex;
			}
			else {
				bufIndex=0;
				bufCount=file.read(buf);
				if (0>bufCount) {
					return -1;
				}
			}
		}
		int result=bits&0xff;
		bits>>=8;
		bitsCount-=8;
		return result;
	}
}
