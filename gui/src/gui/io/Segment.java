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
	public static final long END_NUMBER=1l-(1l<<34);
	public static final long GENERATOR_START_NUMBER=(1l<<32)+1l;
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
		long[] primeCounts=new long[12];
		long[] primeGapFrequencies=new long[2048];
		long[] primeGapStarts=new long[primeGapFrequencies.length];
		class Aa {
			long maxPrime=0l;
			long minPrime=0l;
			
			private void next(long prime) {
				++primeCounts[(int)Long.remainderUnsigned(prime, 12l)];
				if (0==minPrime) {
					minPrime=prime;
				}
				else {
					long gap2=prime-maxPrime;
					if (gap2>=primeGapFrequencies.length) {
						throw new IllegalStateException(String.format(
								"prime gap %1$,d is too large", gap2));
					}
					int gap=(int)gap2;
					++primeGapFrequencies[gap];
					if (0l==primeGapStarts[gap]) {
						primeGapStarts[gap]=maxPrime;
					}
				}
				maxPrime=prime;
			}
		}
		Aa aa=new Aa();
		listPrimes(segmentEnd, aa::next, segmentStart);
		long aggregateEnd=System.nanoTime();
		long aggregateNanos=aggregateEnd-aggregateStart;
		for (int ii=0; primeCounts.length>ii; ii+=2) {
			if (0l!=primeCounts[ii]) {
				throw new IllegalStateException(String.format(
						"prime congruent to %1$,d (mod 12)", ii));
			}
		}
		if (0l!=primeCounts[9]) {
			throw new IllegalStateException(String.format(
					"prime congruent to %1$,d (mod 12)", 9));
		}
		if ((1l<segmentStart)
				&& (0l!=primeCounts[3])) {
			throw new IllegalStateException(String.format(
					"prime congruent to %1$,d (mod 12)", 3));
		}
		Map<Long, Long> primeGapStarts2=new HashMap<>();
		Map<Long, Long> primeGapFrequencies2=new HashMap<>();
		for (int gap=primeGapFrequencies.length-1; 0<=gap; --gap) {
			if (0!=primeGapFrequencies[gap]) {
				Long gap2=(long)gap;
				primeGapFrequencies2.put(gap2, primeGapFrequencies[gap]);
				primeGapStarts2.put(gap2, primeGapStarts[gap]);
			}
		}
		return new Aggregate(aggregateNanos, initNanos, lastModification,
				aa.maxPrime, aa.minPrime,
				primeCounts[11],
				primeCounts[1]+primeCounts[5],
				primeCounts[3]+primeCounts[7]+primeCounts[11],
				primeCounts[1]+primeCounts[7],
				primeGapFrequencies2, primeGapStarts2,
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
		if (1l!=(segmentStart&(NUMBERS-1))) {
			throw new IllegalArgumentException(String.format(
					"a %1$s szegmens kezdet nem 1-gyel, hanem %2$s-vel kongruens (mod %3$s)",
					UnsignedLong.format(segmentStart),
					UnsignedLong.format(segmentStart&(NUMBERS-1)),
					UnsignedLong.format(NUMBERS)));
		}
	}
	
	@Override
	public void clear(boolean prime) {
		Arrays.fill(segment, (byte)(prime?0:-1));
	}
	
	public void clear(long lastModification, boolean prime,
			long segmentStart) {
		checkSegmentStart(segmentStart);
		this.lastModification=lastModification;
		this.segmentStart=segmentStart;
		clear(prime);
		segmentEnd=segmentStart+NUMBERS;
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
	
	public void read(long lastModification, long segmentStart,
			InputStream stream) throws IOException {
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
		if (0<=stream.read()) {
			throw new IOException("no eof");
		}
	}
	
	public void read(Path path) throws IOException {
		long lastModification2=Files.getLastModifiedTime(path).toMillis();
		String fileName=path.getFileName().toString();
		long segmentStart2=Long.parseUnsignedLong(
				fileName.substring(fileName.length()-16, fileName.length()),
				16);
		try (InputStream is=Files.newInputStream(path);
				InputStream bis0=new BufferedInputStream(is)) {
			read(lastModification2, segmentStart2, bis0);
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
