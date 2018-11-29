package gui.io;

import gui.math.UnsignedLong;
import gui.ui.progress.Progress;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database {
	public static final String AGGREGATES="aggregates";
	public static final String AGGREGATES_TEMP="aggregates.tmp";
	public static final Pattern SEGMENT_PATTERN
			=Pattern.compile("primes.([0-9a-f]{8}[048c]0000001)");
	
	public final Path rootDirectory;
	
	public Database(Path rootDirectory) {
		this.rootDirectory=rootDirectory.toAbsolutePath().normalize();
	}
	
	public AggregatesReader aggregatesReader() throws IOException {
		return AggregatesReader.create(rootDirectory.resolve(AGGREGATES));
	}
	
	public void importAggregates(Path aggregatesFile, Progress progress)
			throws Throwable {
		try (AggregatesWriter writer=AggregatesWriter.create(
				rootDirectory.resolve(AGGREGATES),
				rootDirectory.resolve(AGGREGATES_TEMP))) {
			try (AggregatesReader reader0=aggregatesReader();
					AggregatesReader reader1
							=AggregatesReader.create(aggregatesFile)) {
				boolean hasMore0=true;
				boolean hasMore1=true;
				AggregateBlock block0=null;
				AggregateBlock block1=null;
				while (true) {
					if ((null==block0)
							&& hasMore0) {
						block0=reader0.next(false);
						if (null==block0) {
							hasMore0=false;
						}
					}
					if ((null==block1)
							&& hasMore1) {
						block1=reader1.next(false);
						if (null==block1) {
							hasMore1=false;
						}
					}
					progress.progress("összesítés importálása",
							0.5*(reader0.progress()+reader1.progress()));
					if (null==block0) {
						if (null==block1) {
							break;
						}
						else {
							writer.write(block1);
							block1=null;
						}
					}
					else {
						if (null==block1) {
							writer.write(block0);
							block0=null;
						}
						else {
							int cc=Long.compareUnsigned(
									block0.get().segmentStart,
									block1.get().segmentStart);
							if (0<cc) {
								writer.write(block1);
								block1=null;
							}
							else if (0>cc) {
								writer.write(block0);
								block0=null;
							}
							else {
								if (block0.get().lastModification
										>=block1.get().lastModification) {
									writer.write(block0);
								}
								else {
									writer.write(block1);
								}
								block0=null;
								block1=null;
							}
						}
					}
				}
			}
			writer.setSuccessful();
		}
		progress.finished();
	}
	
	public DatabaseInfo info(Progress progress) throws Throwable {
		NavigableMap<Long, Long> lastModifications;
		try (AggregatesReader reader=aggregatesReader()) {
			lastModifications=Aggregates.lastModifications(
					progress.subProgress(0.0, "összesítés olvasása", 0.6),
					reader);
		}
		Segments segments=readSegments(
				progress.subProgress(0.6, "szegemensfájlok olvasása", 0.99));
		int newSegments=segments.newSegments(lastModifications,
						progress.subProgress(0.99, "új szegmensfájlok", 1.0))
				.size();
		progress.finished();
		return new DatabaseInfo(
				Aggregates.info(lastModifications),
				newSegments,
				segments.info());
	}
	
	public PrimeProducer largePrimes() {
		return (consumer, max, progress)->{
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
		};
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
		Deque<Segment.Info> segmentInfos=new ArrayDeque<>(
				readSegments(
						progress.subProgress(
								0.0, "szegmensfájlok olvasása", 0.2))
						.segments
						.values());
		Segment segment=new Segment();
		try (AggregatesWriter writer=AggregatesWriter.create(
				rootDirectory.resolve(AGGREGATES),
				rootDirectory.resolve(AGGREGATES_TEMP))) {
			try (AggregatesReader reader=aggregatesReader()) {
				reader.consume(
						false,
						(aggregateBlock, progress2)->{
							Aggregate aggregate=aggregateBlock.get();
							while (!segmentInfos.isEmpty()) {
								Segment.Info segmentInfo
										=segmentInfos.peekFirst();
								if (0<=Long.compareUnsigned(
											segmentInfo.segmentStart,
											aggregate.segmentStart)) {
									break;
								}
								segmentInfos.pollFirst();
								segment.read(this, segmentInfo.segmentStart);
								writer.write(new AggregateBlock(
										segment.aggregate()));
							}
							Segment.Info segmentInfo=segmentInfos.peekFirst();
							if ((null==segmentInfo)
									|| (0<Long.compareUnsigned(
											segmentInfo.segmentStart,
											aggregate.segmentStart))) {
								writer.write(aggregateBlock);
							}
							else {
								segmentInfo=segmentInfos.pollFirst();
								if (aggregate.lastModification
										>=segmentInfo.lastModification) {
									writer.write(aggregateBlock);
								}
								else {
									segment.read(this,
											segmentInfo.segmentStart);
									writer.write(new AggregateBlock(
											segment.aggregate()));
								}
							}
						},
						null,
						progress.subProgress(0.2, "összesítés", 0.6));
			}
			Progress subProgress
					=progress.subProgress(0.6, "összesítés", 1.0);
			for (int ii=0, ss=segmentInfos.size(); ss>ii; ++ii) {
				subProgress.progress(1.0*ii/ss);
				Segment.Info segmentInfo=segmentInfos.pollFirst();
				segment.read(this, segmentInfo.segmentStart);
				writer.write(new AggregateBlock(segment.aggregate()));
			}
			writer.setSuccessful();
		}
		progress.finished();
	}
	
	public Path samplesDirectory() {
		Path directory=Paths.get("../samples").toAbsolutePath();
		return Files.exists(directory)
				?directory
				:rootDirectory;
	}
	
	public Path segmentFile(long segmentStart) {
		Segment.checkSegmentStart(segmentStart);
		return rootDirectory
				.resolve(String.format("primes.%1$016x", segmentStart));
	}
}
