package gui.io;

import gui.ui.progress.Progress;
import gui.util.MeasuringInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

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
	
	private static class Version0 extends Stream {
		private Version0(MeasuringInputStream measure, long size,
				DataInputStream stream) {
			super(measure, size, stream);
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
			return new AggregateBlock(block);
		}
	}
	
	private static class Versionless extends Stream {
		private Versionless(MeasuringInputStream measure, long size,
				DataInputStream stream) {
			super(measure, size, stream);
		}
		
		@Override
		protected AggregateBlock nextImpl() throws IOException {
			try {
				return new AggregateBlock(Aggregate.readFrom(stream));
			}
			catch (EOFException ex) {
				return null;
			}
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
		if (4l>size) {
			throw new IllegalStateException("too small");
		}
		Integer version;
		if (8l>size) {
			version=null;
		}
		else {
			try (InputStream fis=Files.newInputStream(path);
					InputStream bis=new BufferedInputStream(fis, 8);
					DataInputStream dis=new DataInputStream(bis)) {
				int magic=dis.readInt();
				if (Aggregates.MAGIC==magic) {
					version=dis.readInt();
				}
				else {
					version=null;
				}
			}
		}
		boolean error=true;
		InputStream fis=Files.newInputStream(path);
		try {
			InputStream bis=new BufferedInputStream(fis);
			try {
				MeasuringInputStream mis=new MeasuringInputStream(bis);
				try {
					if (null==version) {
						InputStream gis=new GZIPInputStream(mis);
						try {
							DataInputStream dis=new DataInputStream(gis);
							try {
								dis.readInt();
								AggregatesReader reader
										=new AggregatesReader.Versionless(
											mis, size, dis);
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
								gis.close();
							}
						}
					}
					else {
						DataInputStream dis=new DataInputStream(mis);
						try {
							dis.readLong();
							AggregatesReader reader;
							switch (version) {
								case 0:
									reader=new AggregatesReader.Version0(
											mis, size, dis);
									break;
								default:
									throw new RuntimeException(
											String.format(
												"unknown version %1$,d",
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
