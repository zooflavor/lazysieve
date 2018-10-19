package gui.io;

import gui.math.UnsignedLong;
import gui.sieve.SieveTable;
import gui.ui.MessageException;
import gui.ui.progress.Progress;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Segment implements SieveTable {
	public static final int BITS=1<<29;
	public static final int BYTES=BITS>>3;
	public static final int LONGS=BITS>>6;
	public static final long MAX=-(1l<<33)-1l;
	public static final long MIN=3l;
	public static final long NUMBERS=BITS<<1;
	
	public static class Info {
		public final long lastModification;
		public final Path path;
		public final long segmentStart;
		
		public Info(long lastModification, Path path, long segmentStart) {
			this.lastModification=lastModification;
			this.path=path;
			this.segmentStart=segmentStart;
		}
	}
	
	public long gunzipNanos;
	public long gzipBytes;
	public long gzipNanos;
	public long initNanos;
	public long lastModification;
	private final byte[] longBuffer=new byte[8];
	public final byte[] segment=new byte[BYTES];
	public long segmentEnd;
	public long segmentStart;
	public long sieveNanos;
	
	public Aggregate aggregate() throws Throwable {
		check();
		long aggregateStart=System.nanoTime();
		class Aa {
			long maxPrime=0l;
			long minPrime=0l;
			long primeCount12Z11=0l;
			long primeCount4Z1=0l;
			long primeCount4Z3=0l;
			long primeCount6Z1=0l;
		}
		Aa aa=new Aa();
		Map<Long, Long> primeGapStarts=new HashMap<>();
		Map<Long, Long> primeGapFrequencies=new HashMap<>();
		listPrimes(segmentEnd,
				(prime)->{
					if (0==((prime>>>1)&1)) {
						++aa.primeCount4Z1;
					}
					else {
						++aa.primeCount4Z3;
					}
					if (1l==Long.remainderUnsigned(prime, 6)) {
						++aa.primeCount6Z1;
					}
					if (11l==Long.remainderUnsigned(prime, 12)) {
						++aa.primeCount12Z11;
					}
					if (0==aa.minPrime) {
						aa.minPrime=prime;
					}
					else {
						Long gap=prime-aa.maxPrime;
						Long frequency=primeGapFrequencies.get(gap);
						primeGapFrequencies.put(gap,
								(null==frequency)?1l:(frequency+1l));
						Long start=primeGapStarts.get(gap);
						if (null==start) {
							primeGapStarts.put(gap, aa.maxPrime);
						}
					}
					aa.maxPrime=prime;
				},
				segmentStart);
		long aggregateEnd=System.nanoTime();
		long aggregateNanos=aggregateEnd-aggregateStart;
		return new Aggregate(aggregateNanos, gunzipNanos, gzipBytes, gzipNanos,
				initNanos, lastModification, aa.maxPrime, aa.minPrime,
				aa.primeCount12Z11, aa.primeCount4Z1, aa.primeCount4Z3,
				aa.primeCount6Z1, primeGapFrequencies, primeGapStarts,
				segmentStart/Segment.NUMBERS, segmentEnd, segmentStart,
				sieveNanos);
	}
	
	public int bitIndex(long number) {
		if (segmentStart>number) {
			throw new IllegalArgumentException(String.format(
					"%1$,d>%2$,d", segmentStart, number));
		}
		if (segmentStart+NUMBERS<number) {
			throw new IllegalArgumentException(String.format(
					"%1$,d<%2$,d", segmentStart+NUMBERS, number));
		}
		if (0==(number%2)) {
			throw new IllegalArgumentException(String.format(
					"0==(%1$,d%%2)", number));
		}
		return (int)((number-segmentStart)>>>1);
	}
	
	public void check() {
		checkSegmentStart(segmentStart);
		if (segmentEnd!=segmentStart+NUMBERS) {
			throw new IllegalStateException(String.format(
					"%1$,d!=%2$,d",
					segmentEnd, segmentStart+NUMBERS));
		}
	}
	
	public static void checkSegmentStart(long segmentStart) {
		if (1l!=(segmentStart&((1l<<30)-1))) {
			throw new IllegalArgumentException(String.format(
					"1l!=(%1$,d&((1l<<30)-1))", segmentStart));
		}
	}
	
	public void clear(long gunzipNanos, long gzipBytes, long lastModification,
			boolean prime, long segmentStart) {
		checkSegmentStart(segmentStart);
		this.gunzipNanos=gunzipNanos;
		this.gzipBytes=gzipBytes;
		this.lastModification=lastModification;
		this.segmentStart=segmentStart;
		Arrays.fill(segment, (byte)(prime?0:-1));
		segmentEnd=segmentStart+NUMBERS;
		gzipNanos=0l;
		initNanos=0l;
		sieveNanos=0l;
	}
	
	public void compare(Segment reference, Progress progress)
			throws Throwable {
		compare(segmentEnd, reference, progress, segmentStart);
	}
	
	public void compare(long end, Segment reference, Progress progress,
			long start) throws Throwable {
		check();
		reference.check();
		if (this.segmentStart!=reference.segmentStart) {
			throw new IllegalStateException(String.format(
					"%1$s!=%2$s",
					UnsignedLong.format(this.segmentStart),
					UnsignedLong.format(reference.segmentStart)));
		}
		if (0<Long.compareUnsigned(segmentStart, start)) {
			throw new IllegalStateException(String.format(
					"%1$s>%2$s",
					UnsignedLong.format(segmentStart),
					UnsignedLong.format(start)));
		}
		if (0>Long.compareUnsigned(segmentEnd, end)) {
			throw new IllegalStateException(String.format(
					"%1$s<%2$s",
					UnsignedLong.format(segmentEnd),
					UnsignedLong.format(end)));
		}
		int bitIndex=bitIndex(start);
		int bits=(int)((end-start)>>>1);
		int maxBits=bits;
		for (; (0<bits) && (0!=(bitIndex&7)); ++bitIndex, --bits) {
			progress.progress(1.0*(maxBits-bits)/maxBits);
			if (isPrime(bitIndex)!=reference.isPrime(bitIndex)) {
				throw new MessageException(String.format(
						"segments differ at %1$,d"
								+"; this: %2$s, reference: %3$s",
						number(bitIndex),
						isPrime(bitIndex),
						reference.isPrime(bitIndex)));
			}
		}
		for (; 8<=bits; bitIndex+=8, bits-=8) {
			progress.progress(1.0*(maxBits-bits)/maxBits);
			byte thisByte=this.segment[bitIndex>>3];
			byte referenceByte=reference.segment[bitIndex>>3];
			if (thisByte!=referenceByte) {
				int diff=(thisByte&0xff)^(referenceByte&0xff);
				int bit=0;
				while (0==(diff&1)) {
					diff>>>=1;
					++bit;
				}
				bitIndex=(bitIndex&(~7))+bit;
				throw new MessageException(String.format(
						"segments differ at %1$,d"
								+"; this: %2$s, reference: %3$s",
						number(bitIndex),
						isPrime(bitIndex),
						reference.isPrime(bitIndex)));
			}
		}
		for (; 0<bits; ++bitIndex, --bits) {
			progress.progress(1.0*(maxBits-bits)/maxBits);
			if (isPrime(bitIndex)!=reference.isPrime(bitIndex)) {
				throw new MessageException(String.format(
						"segments differ at %1$,d"
								+"; this: %2$s, reference: %3$s",
						number(bitIndex),
						isPrime(bitIndex),
						reference.isPrime(bitIndex)));
			}
		}
		progress.finished();
	}
	
	@Override
	public void flip(long number) throws Throwable {
		number-=segmentStart;
		segment[(int)(number>>>4)]^=1<<((number>>>1)&0x7);
	}
	
	public boolean isPrime(int bitIndex) {
		return 0==(segment[bitIndex>>3]&(1<<(bitIndex&0x7)));
	}
	
	@Override
	public boolean isPrime(long number) throws Throwable {
		number=(number-segmentStart)>>>1;
		return 0==(segment[(int)(number>>3)]&(1<<(number&0x7)));
	}
	
	@Override
	public void listPrimes(long end, PrimeConsumer primeConsumer, long start)
			throws Throwable {
		if (0l==(end&1l)) {
			--end;
		}
		if (0l==(start&1l)) {
			++start;
		}
		int ei=bitIndex(end);
		int si=bitIndex(start);
		for (; (ei>si) && (0!=(si&7)); ++si) {
			if (isPrime(si)) {
				primeConsumer.prime(number(si));
			}
		}
		for (; ei>si; si+=8) {
			int bb=(~segment[si>>>3])&0xff;
			int bi=si;
			while (0!=bb) {
				int nz=Integer.numberOfTrailingZeros(bb);
				bi+=nz;
				bb>>>=nz;
				primeConsumer.prime(number(bi));
				++bi;
				bb>>>=1;
			}
		}
		for (; ei>si; ++si) {
			if (isPrime(si)) {
				primeConsumer.prime(number(si));
			}
		}
	}
	
	public long number(int bitIndex) {
		return segmentStart+(((long)bitIndex)<<1);
	}
	
	public void read(Database database, long segmentStart) throws IOException {
		read(database.segmentFile(segmentStart));
	}
	
	public void read(long gzipBytes, long lastModification, long segmentStart,
			InputStream stream) throws IOException {
		long gunzipStart=System.nanoTime();
		this.gzipBytes=gzipBytes;
		this.lastModification=lastModification;
		for (int ii=0; segment.length>ii; ) {
			int rr=stream.read(segment, ii, segment.length-ii);
			if (0>rr) {
				throw new EOFException();
			}
			ii+=rr;
		}
		this.segmentStart=readLong(stream);
		if (this.segmentStart!=segmentStart) {
			throw new IllegalArgumentException(String.format(
					"%2$,d!=%2$,d", this.segmentStart, segmentStart));
		}
		segmentEnd=this.segmentStart+NUMBERS;
		initNanos=readLong(stream);
		sieveNanos=readLong(stream);
		gzipNanos=readLong(stream);
		if (0<=stream.read()) {
			throw new IOException("no eof");
		}
		long gunzipEnd=System.nanoTime();
		gunzipNanos=gunzipEnd-gunzipStart;
	}
	
	public void read(Path path) throws IOException {
		long gzipBytes2=Files.size(path);
		long lastModification2=Files.getLastModifiedTime(path).toMillis();
		long segmentStart2=Long.parseUnsignedLong(
				path.getFileName().toString().substring(0, 16),
				16);
		try (InputStream is=Files.newInputStream(path);
				InputStream bis0=new BufferedInputStream(is)) {
				//InputStream bis0=new BufferedInputStream(is);
				//InputStream gzis=new GZIPInputStream(bis0);
				//InputStream bis1=new BufferedInputStream(gzis)) {
			read(gzipBytes2, lastModification2, segmentStart2, bis0);
			//read(gzipBytes2, lastModification2, segmentStart2, bis1);
		}
	}
	
	private long readLong(InputStream stream) throws IOException {
		for (int index=0, length=8; 0<length; ) {
			int rr=stream.read(longBuffer, index, length);
			if (0>rr) {
				throw new EOFException();
			}
			index+=rr;
			length-=rr;
		}
		long result=0l;
		for (int ii=7; 0<=ii; --ii) {
			result|=(longBuffer[ii]&0xffl)<<(ii<<3);
		}
		return result;
	}
	
	public void setComposite(int bitIndex) {
		segment[bitIndex>>3]|=1<<(bitIndex&0x7);
	}
	
	@Override
	public void setComposite(long number) throws Throwable {
		number-=segmentStart;
		segment[(int)(number>>>4)]|=1<<((number>>>1)&0x7);
	}
	
	@Override
	public void setPrime(long number) throws Throwable {
		number-=segmentStart;
		segment[(int)(number>>>4)]&=~(1<<((number>>>1)&0x7));
	}
}
