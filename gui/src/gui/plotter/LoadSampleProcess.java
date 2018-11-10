package gui.plotter;

import gui.graph.PlotType;
import gui.graph.Sample;
import gui.ui.Color;
import gui.ui.GuiProcess;
import gui.ui.MessageException;
import gui.util.MeasuringInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class LoadSampleProcess extends GuiProcess<Plotter, JFrame> {
	private final Path path;
	private Sample sample;
	
	private LoadSampleProcess(Plotter parent, Path path) {
		super(true, parent, Plotter.TITLE);
		this.path=path;
	}
	
	@Override
	protected void background() throws Throwable {
		long size=Files.size(path);
		if (0>=size) {
			return;
		}
		PlotType plotType=null;
		Sample.Builder sample2=Sample.builder();
		try (InputStream is=Files.newInputStream(path);
				MeasuringInputStream mis=new MeasuringInputStream(is);
				BufferedInputStream bis=new BufferedInputStream(mis);
				Reader rd=new InputStreamReader(bis,
						StandardCharsets.US_ASCII);
				BufferedReader br=new BufferedReader(rd)) {
			for (String line; null!=(line=br.readLine()); ) {
				progress.progress(1.0*mis.count()/size);
				int index=line.indexOf(",");
				if (null==plotType) {
					if (0>index) {
						plotType=PlotType.valueOf(line);
						continue;
					}
					else {
						plotType=PlotType.LINE;
					}
				}
				if (0>index) {
					throw new MessageException(
							String.format("malformed line %1$s", line));
				}
				long key=Long.parseLong(line.substring(0, index));
				double value=Double.parseDouble(line.substring(index+1));
				sample2.add(key, value);
			}
		}
		if (0>=sample2.size()) {
			throw new MessageException(
					String.format("empty sample file %1$s", path));
		}
		Color color=parent.selectNewColor();
		sample=sample2.create(path.getFileName().toString(),
				Colors.INTERPOLATION, plotType, color, color);
		progress.finished();
	}
	
	@Override
	protected void foreground() throws Throwable {
		if (null!=sample) {
			parent.addSample(sample);
		}
	}
	
	public static void start(Plotter plotter) {
        JFileChooser chooser=new JFileChooser(
				plotter.session.database.rootDirectory.toFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if ((JFileChooser.APPROVE_OPTION
						!=chooser.showOpenDialog(plotter.window()))
                || (null==chooser.getSelectedFile())
				|| (!Files.exists(chooser.getSelectedFile().toPath()))) {
            return;
        }
        new LoadSampleProcess(
						plotter, chooser.getSelectedFile().toPath())
                .start(plotter.session.executor);
	}
}
