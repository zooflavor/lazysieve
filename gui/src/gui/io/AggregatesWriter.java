package gui.io;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AggregatesWriter implements AutoCloseable {
	private final FileDescriptor fileDescriptor;
	private long segmentStart;
	private final DataOutputStream stream;
	private boolean successful;
	private final Path target;
	private final Path temp;
	
	private AggregatesWriter(FileDescriptor fileDescriptor,
			DataOutputStream stream, Path target, Path temp) {
		this.fileDescriptor=fileDescriptor;
		this.target=target;
		this.temp=temp;
		this.stream=stream;
	}
	
	@Override
	public void close() throws IOException {
		try {
			fileDescriptor.sync();
		}
		finally {
			stream.close();
		}
		if (successful) {
			if (Files.exists(target)) {
				Files.delete(target);
			}
			Files.move(temp, target);
		}
		else {
			Files.delete(temp);
		}
	}
	
	public static AggregatesWriter create(Path target, Path temp)
			throws IOException {
		if (Files.exists(temp)) {
			Files.delete(temp);
		}
		boolean error=true;
		FileOutputStream fos=new FileOutputStream(temp.toFile());
		try {
			OutputStream bos=new BufferedOutputStream(fos);
			try {
				DataOutputStream dos=new DataOutputStream(bos);
				try {
					AggregatesWriter writer=new AggregatesWriter(
							fos.getFD(), dos, target, temp);
					writer.writeHeader();
					error=false;
					return writer;
				}
				finally {
					if (error) {
						dos.close();
					}
				}
			}
			finally {
				if (error) {
					bos.close();
				}
			}
		}
		finally {
			if (error) {
				fos.close();
			}
		}
	}
	
	public void setSuccessful() {
		successful=true;
	}
	
	public void write(AggregateBlock aggregateBlock) throws Throwable {
		long segmentStart2=aggregateBlock.get().segmentStart;
		if (0<=Long.compareUnsigned(segmentStart, segmentStart2)) {
			throw new IllegalArgumentException("non-monotonic segment start");
		}
		segmentStart=segmentStart2;
		aggregateBlock.writeTo(stream);
	}
	
	private void writeHeader() throws IOException {
		stream.writeInt(Aggregates.MAGIC);
		stream.writeInt(0);
	}
}
