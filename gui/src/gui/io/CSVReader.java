package gui.io;

import gui.util.MeasuringInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CSVReader implements AutoCloseable {
	private final MeasuringInputStream measuringStream;
	private final PushbackReader reader;
	private final long size;
	
	public CSVReader(MeasuringInputStream measuringStream,
			PushbackReader reader, long size) {
		this.measuringStream=measuringStream;
		this.reader=reader;
		this.size=size;
	}
	
	@Override
	public void close() throws IOException {
		reader.close();
	}
	
	public static CSVReader open(Path path) throws IOException {
		long size=Files.size(path);
		boolean error=true;
		InputStream fis=Files.newInputStream(path);
		try {
			MeasuringInputStream mis=new MeasuringInputStream(fis);
			try {
				InputStream bis=new BufferedInputStream(fis);
				try {
					Reader sr=new InputStreamReader(
							bis, StandardCharsets.UTF_8);
					try {
						Reader br=new BufferedReader(sr);
						try {
							PushbackReader pr=new PushbackReader(br);
							try {
								CSVReader result=new CSVReader(mis, pr, size);
								error=false;
								return result;
							}
							finally {
								if (error) {
									pr.close();
								}
							}
						}
						finally {
							if (error) {
								br.close();
							}
						}
					}
					finally {
						if (error) {
							sr.close();
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
					mis.close();
				}
			}
		}
		finally {
			if (error) {
				fis.close();
			}
		}
	}
	
	public double progress() {
		return 1.0*measuringStream.count()/size;
	}
	
	public List<String> read() throws IOException {
		List<String> result=new ArrayList<>();
		while (true) {
			String cell=readCell();
			if (null==cell) {
				if (result.isEmpty()) {
					return null;
				}
				break;
			}
			result.add(cell);
			int rr=readChecked();
			if (0>rr) {
				break;
			}
			char cc=(char)rr;
			if (('\r'==cc)
					|| ('\n'==cc)) {
				int r2=readChecked();
				if (0>r2) {
					break;
				}
				char c2=(char)rr;
				if ((('\r'==cc)
								&& ('\n'!=c2))
						|| (('\n'==cc)
								&& ('\r'!=c2))) {
					reader.unread(r2);
				}
				break;
			}
			if (','!=cc) {
				throw new IOException("a CSV fájl nem jólformált");
			}
		}
		return result;
	}
	
	private String readCell() throws IOException {
		int rr=readChecked();
		if (0>rr) {
			return null;
		}
		char cc=(char)rr;
		StringBuilder sb=new StringBuilder();
		if ('"'==cc) {
			while (true) {
				rr=readChecked();
				if (0>rr) {
					throw new IOException("a CSV fájl nem jólformált");
				}
				cc=(char)rr;
				if ('"'==cc) {
					rr=readChecked();
					if (0>rr) {
						break;
					}
					cc=(char)rr;
					if ('"'==cc) {
						sb.append(cc);
					}
					else {
						reader.unread(rr);
						break;
					}
				}
				sb.append(cc);
			}
		}
		else {
			reader.unread(rr);
			while (true) {
				rr=readChecked();
				if (0>rr) {
					break;
				}
				cc=(char)rr;
				if ((','==cc)
						|| ('\r'==cc)
						|| ('\n'==cc)) {
					reader.unread(rr);
					break;
				}
				sb.append(cc);
			}
		}
		return sb.toString();
	}
	
	private int readChecked() throws IOException {
		int rr=reader.read();
		if ((0<=rr)
				&& (0x20>rr)
				&& (0xa!=rr)
				&& (0xb!=rr)
				&& (0xd!=rr)) {
			throw new IOException("bináris fájl");
		}
		return rr;
	}
}
