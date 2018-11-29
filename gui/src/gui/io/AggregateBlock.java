package gui.io;

import gui.util.Supplier;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AggregateBlock implements Supplier<Aggregate> {
	private final Aggregate aggregate;
	private byte[] block;
	
	public AggregateBlock(byte[] block, int version) throws IOException {
		this.block=(Aggregates.VERSION==version)?block:null;
		try (InputStream bis=new ByteArrayInputStream(block);
				InputStream gis=new GZIPInputStream(bis);
				DataInputStream dis=new DataInputStream(gis)) {
			aggregate=Aggregate.readFrom(dis, version);
		}
	}
	
	public AggregateBlock(Aggregate aggregate) {
		this.aggregate=aggregate;
		this.block=null;
	}
	
	@Override
	public Aggregate get() throws Throwable {
		return aggregate;
	}
	
	public void writeTo(DataOutputStream stream) throws IOException {
		if (null==block) {
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			try (OutputStream gos=new GZIPOutputStream(bos);
					DataOutputStream dos=new DataOutputStream(gos)) {
				aggregate.writeTo(dos);
			}
			finally {
				bos.close();
			}
			block=bos.toByteArray();
		}
		stream.writeInt(block.length);
		stream.write(block);
	}
}
