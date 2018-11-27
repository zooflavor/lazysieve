package gui.io;

import java.io.IOException;
import java.io.InputStream;

public class MeasuringInputStream extends InputStream {
	private long count;
	private final InputStream stream;
	
	public MeasuringInputStream(InputStream stream) {
		this.stream=stream;
	}
	
	@Override
	public int available() throws IOException {
		return stream.available();
	}
	
	@Override
	public void close() throws IOException {
		stream.close();
	}
	
	public long count() {
		return count;
	}
	
	@Override
	public int read() throws IOException {
		int result=stream.read();
		if (0<=result) {
			++count;
		}
		return result;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result=stream.read(b, off, len);
		if (0<result) {
			count+=result;
		}
		return result;
	}
	
	@Override
	public long skip(long n) throws IOException {
		long result=stream.skip(n);
		count+=result;
		return result;
	}
}
