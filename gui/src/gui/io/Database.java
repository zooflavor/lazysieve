package gui.io;

import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import gui.util.IntList;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Database implements PrimesProducer {
	public static final String AGGREGATES="aggregates";
	public static final String AGGREGATES_TEMP="aggregates.tmp";
	public static final Pattern SEGMENT_PATTERN
			=Pattern.compile("([0-9a-f]{8}[048c]0000001).primes");
	public static final long SMALL_PRIMES_MAX
			=UnsignedLong.squareRootFloor(Segment.NUMBERS+1l);
	
	public static class Info {
		public final TypeInfo aggregates;
		public final long numberOfNewSegments;
		public final TypeInfo segments;
		
		public Info(TypeInfo aggregates, long numberOfNewSegments,
				TypeInfo segments) {
			this.aggregates=aggregates;
			this.numberOfNewSegments=numberOfNewSegments;
			this.segments=segments;
		}
	}
	
	public static class TypeInfo {
		public final Long firstSegmentStart;
		public final Long lastSegmentStart;
		public final Long missingSegments;
		public final Long missingSegmentStart;
		public final long numberOfSegments;
		
		public TypeInfo(Long firstSegmentStart, Long lastSegmentStart,
				Long missingSegments, Long missingSegmentStart,
				long numberOfSegments) {
			this.firstSegmentStart=firstSegmentStart;
			this.lastSegmentStart=lastSegmentStart;
			this.missingSegments=missingSegments;
			this.missingSegmentStart=missingSegmentStart;
			this.numberOfSegments=numberOfSegments;
		}
		
		public static TypeInfo info(NavigableMap<Long, ?> segments) {
			if (segments.isEmpty()) {
				return new TypeInfo(null, null, null, 1l, 0l);
			}
			long missingSegmentStart=1l;
			while (segments.containsKey(missingSegmentStart)) {
				missingSegmentStart+=Segment.NUMBERS;
			}
			Long missingSegments;
			Long next=segments.higherKey(missingSegmentStart);
			missingSegments=(null==next)
					?null
					:((next-missingSegmentStart)/Segment.NUMBERS);
			return new TypeInfo(
					segments.firstKey(),
					segments.lastKey(),
					missingSegments,
					missingSegmentStart,
					segments.size());
		}
	}
	
	public final Path rootDirectory;
	
	public Database(Path rootDirectory) {
		this.rootDirectory=rootDirectory.toAbsolutePath().normalize();
	}
	
	public void importAggregates(Path aggregatesFile, Progress progress)
			throws Throwable {
        Aggregates aggregates
                =readAggregates(progress.subProgress(
								0.0, "read aggregates", 0.3))
                        .importAggregates(
								readAggregates(aggregatesFile,
										progress.subProgress(0.3,
												"import aggregates", 0.6)));
        progress.cancellable(false);
		writeAggregates(aggregates,
				progress.subProgress(0.6, "write aggregates", 1.0));
		progress.finished();
	}
	
	public Database.Info info(Progress progress) throws Throwable {
		Aggregates aggregates=readAggregates(
				progress.subProgress(0.0, "read aggregates", 0.6));
		Segments segments=readSegments(
				progress.subProgress(0.6, "read segments", 0.99));
		int newSegments=segments.newSegments(aggregates,
						progress.subProgress(0.99, "new segments", 1.0))
				.size();
		progress.finished();
		return new Info(
				aggregates.info(),
				newSegments,
				segments.info());
	}
	
	@Override
	public void primes(PrimeConsumer consumer, long max, Progress progress)
			throws Throwable {
		if (0>Long.compareUnsigned(UnsignedLong.MAX_PRIME, max)) {
			throw new IllegalArgumentException();
		}
		if (3l>max) {
			return;
		}
		long lastPrime=1l;
		Segment segment=new Segment();
		for (long ss=0; 4>ss; ++ss) {
			long segmentStart=ss*Segment.NUMBERS+1l;
			if (0<Long.compareUnsigned(segmentStart, max)) {
				return;
			}
			segment.read(this, segmentStart);
			for (int bitIndex=0; Segment.BITS>bitIndex; ++bitIndex) {
				if (0==(bitIndex&1023)) {
					progress.progress(1.0*lastPrime/max);
				}
				if (!segment.isPrime(bitIndex)) {
					continue;
				}
				lastPrime=segment.number(bitIndex);
				if (0<Long.compareUnsigned(lastPrime, max)) {
					return;
				}
				consumer.prime(lastPrime);
			}
		}
	}
	
	public Aggregates readAggregates(Progress progress) throws Throwable {
		return readAggregates(rootDirectory.resolve(AGGREGATES), progress);
	}
	
	public static Aggregates readAggregates(Path path, Progress progress)
			throws Throwable {
		progress.checkCancelled();
		if (Files.exists(path)) {
			try (InputStream is=Files.newInputStream(path);
					InputStream bis0=new BufferedInputStream(is);
					GZIPInputStream gzis=new GZIPInputStream(bis0);
					InputStream bis1=new BufferedInputStream(gzis);
					DataInputStream dis=new DataInputStream(bis1)) {
				return Aggregates.readFrom(dis, progress);
			}
			catch (FileNotFoundException ex) {
			}
		}
		progress.finished();
		return new Aggregates(new ArrayList<>(0));
	}
	
	public IntList readPrimes(Progress progress) throws Throwable {
		IntList result=new IntList(UnsignedLong.MAX_PRIME_COUNT);
		primes((prime)->result.add((int)prime),
				UnsignedLong.MAX_PRIME,
				progress);
		return result;
	}
	
	public Segments readSegments(Progress progress) throws Throwable {
		progress.checkCancelled();
		List<Segment.Info> segments=new ArrayList<>();
		PriorityQueue<Path> paths
				=new PriorityQueue<>((p0, p1)->{
					int cc=Integer.compare(
							p1.getNameCount(), p0.getNameCount());
					return (0==cc)
							?p0.compareTo(p1)
							:cc;
				});
		paths.add(rootDirectory);
		while (!paths.isEmpty()) {
			progress.progress(null,
					1.0*segments.size()/(segments.size()+paths.size()));
			Path path=paths.poll();
			if (Files.isDirectory(path)) {
				Files.list(path).forEach(paths::add);
			}
			else {
				Matcher matcher=SEGMENT_PATTERN.matcher(
						path.getFileName().toString());
				if (matcher.matches()) {
					long segmentStart=Long.parseUnsignedLong(
							matcher.group(1), 16);
					Segment.checkSegmentStart(segmentStart);
					if (path.equals(segmentFile(segmentStart))) {
						segments.add(new Segment.Info(
								Files.getLastModifiedTime(path).toMillis(),
								path,
								segmentStart));
					}
				}
			}
		}
		progress.finished();
		return new Segments(segments);
	}
	
	public void reaggregate(Progress progress) throws Throwable {
		Aggregates aggregates=readAggregates(
				progress.subProgress(0.0, "read aggregates", 0.2));
		Segment segment=new Segment();
		List<Long> newSegments=readSegments(
						progress.subProgress(0.2, "read segments", 0.25))
				.newSegments(aggregates,
						progress.subProgress(0.25, "new segments", 0.3));
		List<Aggregate> newAggregates=new ArrayList<>();
		int ii=0;
		for (Long segmentStart: newSegments) {
			progress.checkCancelled();
			Progress subProgress=progress.subProgress(
					(0.3*(newSegments.size()-ii)+0.9*ii)
							/newSegments.size(),
					"aggregate segments",
					(0.3*(newSegments.size()-ii-1)+0.9*(ii+1))
							/newSegments.size());
			subProgress.progress("aggregate segment", 0.0);
			segment.read(this, segmentStart);
			newAggregates.add(segment.aggregate());
			++ii;
			if (0==(ii%128)) {
				aggregates=aggregates.importAggregates(
						new Aggregates(newAggregates));
				newAggregates.clear();
                progress.cancellable(false);
				writeAggregates(aggregates, subProgress);
                progress.cancellable(true);
			}
		}
        progress.cancellable(false);
		writeAggregates(
				aggregates.importAggregates(new Aggregates(newAggregates)),
				progress.subProgress(0.9, "write aggregates", 1.0));
		progress.finished();
	}
	
	public Path segmentFile(long segmentStart) {
		Segment.checkSegmentStart(segmentStart);
		return rootDirectory
				.resolve(String.format("%1$02x", (segmentStart>>56)&0xff))
				.resolve(String.format("%1$02x", (segmentStart>>48)&0xff))
				.resolve(String.format("%1$02x", (segmentStart>>40)&0xff))
				.resolve(String.format("%1$016x.primes", segmentStart));
	}
	
	public static IntList smallPrimes(Progress progress) throws Throwable {
		IntList primes=new IntList(4096);
		for (long ii=3l; SMALL_PRIMES_MAX>=ii; ii+=2l) {
			progress.progress(1.0*ii/SMALL_PRIMES_MAX);
			boolean prime=true;
			for (int jj=0; primes.size()>jj; ++jj) {
				long prime2=primes.get(jj);
				if (ii<prime2*prime2) {
					break;
				}
				if (0l==ii%prime2) {
					prime=false;
					break;
				}
			}
			if (prime) {
				primes.add((int)ii);
			}
		}
		return primes;
	}
	
	public void writeAggregates(Aggregates aggregates, Progress progress)
			throws Throwable {
		progress.checkCancelled();
		Path realPath=rootDirectory.resolve(AGGREGATES);
		Path tempPath=rootDirectory.resolve(AGGREGATES_TEMP);
		Files.deleteIfExists(tempPath);
		try (FileOutputStream os=new FileOutputStream(tempPath.toFile());
				OutputStream bos0=new BufferedOutputStream(os);
				GZIPOutputStream gzos=new GZIPOutputStream(bos0);
				OutputStream bos1=new BufferedOutputStream(gzos);
				DataOutputStream dos=new DataOutputStream(bos1)) {
			aggregates.writeTo(dos, progress);
			dos.flush();
			os.getFD().sync();
		}
		Files.deleteIfExists(realPath);
		Files.move(tempPath, realPath);
	}
}
