package gui.io;

import gui.ui.progress.Progress;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AggregatesReader implements AutoCloseable {
	private static class Empty extends AggregatesReader {
		public Empty() {
			super(0l);
		}
		
		@Override
		public void close() throws IOException {
		}
		
		@Override
		protected long measure() throws IOException {
			return 0l;
		}
		
		@Override
		protected AggregateBlock nextImpl() throws IOException {
			return null;
		}
	}
	
	private static abstract class Stream extends AggregatesReader {
		private final MeasuringInputStream measure;
		protected final DataInputStream stream;
		
		public Stream(MeasuringInputStream measure, long size,
				DataInputStream stream) {
			super(size);
			this.measure=measure;
			this.stream=stream;
		}
		
		@Override
		public void close() throws IOException {
			stream.close();
		}
		
		@Override
		protected long measure() throws IOException {
			return measure.count();
		}
	}
	
	private static class Version01 extends Stream {
		private final int version;
		
		private Version01(MeasuringInputStream measure, long size,
				DataInputStream stream, int version) {
			super(measure, size, stream);
			this.version=version;
		}
		
		@Override
		protected AggregateBlock nextImpl() throws IOException {
			int blockSize;
			try {
				blockSize=stream.readInt();
			}
			catch (EOFException ex) {
				return null;
			}
			byte[] block=new byte[blockSize];
			stream.readFully(block, 0, blockSize);
			return new AggregateBlock(block, version);
		}
	}
	
	private long nextSegmentStart=1l;
	private long progress0;
	private long progress1;
	private final long size;
	
	private AggregatesReader(long size) {
		this.size=size;
	}
	
	@Override
	public abstract void close() throws IOException;
	
	public void consume(boolean completePrefix, AggregatesConsumer consumer,
			String message, Progress progress) throws Throwable {
		for (AggregateBlock aggregateBlock;
				null!=(aggregateBlock=next(completePrefix)); ) {
			Progress subProgress=subProgress(message, progress);
			subProgress.progress(0.0);
			consumer.consume(aggregateBlock, subProgress);
		}
		progress.finished(message);
	}
	
	protected abstract long measure() throws IOException;
	
	public AggregateBlock next(boolean completePrefix) throws Throwable {
		progress0=progress1;
		AggregateBlock aggregateBlock=nextImpl();
		progress1=measure();
		if ((null==aggregateBlock)
				|| (completePrefix
						&& (aggregateBlock.get().segmentStart
								!=nextSegmentStart))) {
			return null;
		}
		nextSegmentStart+=Segment.NUMBERS;
		return aggregateBlock;
	}
	
	protected abstract AggregateBlock nextImpl() throws IOException;
	
	public double progress() {
		return 1.0*progress0/size;
	}
	
	public Progress subProgress(String message, Progress progress) {
		return ((0l>=size) || (progress1>=size))
				?(progress.subProgress(1.0, message, 1.0))
				:(progress.subProgress(
						1.0*progress0/size, message, 1.0*progress1/size));
	}
	
	public static AggregatesReader create(Path path) throws IOException {
		if (!Files.exists(path)) {
			return new AggregatesReader.Empty();
		}
		long size=Files.size(path);
		if (8l>size) {
			throw new IllegalStateException(String.format(
					"A %1$s összesítő fájl túl kicsi",
					path));
		}
		boolean error=true;
		InputStream fis=Files.newInputStream(path);
		try {
			InputStream bis=new BufferedInputStream(fis);
			try {
				MeasuringInputStream mis=new MeasuringInputStream(bis);
				try {
					DataInputStream dis=new DataInputStream(mis);
					try {
						int magic=dis.readInt();
						if (Aggregates.MAGIC!=magic) {
							throw new IllegalArgumentException(String.format(
									"A %1$s fájl nem összesítőfájl.",
									path));
						}
						int version=dis.readInt();
						AggregatesReader reader;
						switch (version) {
							case 0:
							case Aggregates.VERSION:
								reader=new AggregatesReader.Version01(
										mis, size, dis, version);
								break;
							default:
								throw new RuntimeException(
										String.format(
											"ismeretlen verzió %1$,d",
											version));
						}
						error=false;
						return reader;
					}
					finally {
						if (error) {
							dis.close();
						}
					}
				}
				finally {
					if (error) {
						mis.close();
					}
				}
			}
			finally {
				if (error) {
					bis.close();
				}
			}
		}
		finally {
			if (error) {
				fis.close();
			}
		}
	}
}
