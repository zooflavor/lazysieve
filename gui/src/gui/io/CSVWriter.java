package gui.io;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CSVWriter implements AutoCloseable {
	private final Writer writer;
	
	public CSVWriter(Writer writer) {
		this.writer=writer;
	}
	
	@Override
	public void close() throws IOException {
		writer.close();
	}
	
	public static CSVWriter open(Path path) throws IOException {
		boolean error=true;
		OutputStream fos=Files.newOutputStream(path);
		try {
			OutputStream bos=new BufferedOutputStream(fos);
			try {
				Writer sw=new OutputStreamWriter(bos, StandardCharsets.UTF_8);
				try {
					Writer bw=new BufferedWriter(sw);
					try {
						CSVWriter result=new CSVWriter(bw);
						error=false;
						return result;
					}
					finally {
						if (error) {
							bw.close();
						}
					}
				}
				finally {
					if (error) {
						sw.close();
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
	
	public void write(List<String> cells) throws IOException {
		for (int ii=0; cells.size()>ii; ++ii) {
			if (0<ii) {
				writer.write(',');
			}
			writeCell(cells.get(ii));
		}
		writer.write("\r\n");
	}
	
	private void writeCell(String cell) throws IOException {
		boolean quote=false;
		for (int ii=cell.length()-1; 0<=ii; --ii) {
			char cc=cell.charAt(ii);
			if (('\n'==cc)
					|| ('\r'==cc)
					|| (','==cc)
					|| ('"'==cc)) {
				quote=true;
				break;
			}
		}
		if (quote) {
			writer.write('"');
			for (int ii=0; cell.length()>ii; ++ii) {
				char cc=cell.charAt(ii);
				writer.write(cc);
				if ('"'==cc) {
					writer.write(cc);
				}
			}
			writer.write('"');
		}
		else {
			writer.write(cell);
		}
	}
}
