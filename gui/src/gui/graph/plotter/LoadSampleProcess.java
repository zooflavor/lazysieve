package gui.graph.plotter;

import gui.graph.PlotType;
import gui.graph.Sample;
import gui.io.CSVReader;
import gui.ui.Color;
import gui.ui.GuiProcess;
import gui.ui.MessageException;
import gui.ui.progress.Progress;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LoadSampleProcess extends GuiProcess<Plotter, JFrame> {
	private final List<Path> paths;
	private List<Sample> samples;
	
	private LoadSampleProcess(Plotter parent, List<Path> paths) {
		super(true, parent, Plotter.TITLE);
		this.paths=paths;
	}
	
	@Override
	protected void background() throws Throwable {
		PlotType plotType=null;
		samples=new ArrayList<>(paths.size());
		List<Color> colors=parent.selectNewColors(paths.size());
		for (int ii=0; paths.size()>ii; ++ii) {
			Progress subProgress=progress.subProgress(
					1.0*ii/paths.size(), null, 1.0*(ii+1)/paths.size());
			Path path=paths.get(ii);
			Sample.Builder sample2=Sample.builder();
			try (CSVReader csv=CSVReader.open(path)) {
				while (true) {
					subProgress.progress(csv.progress());
					List<String> row=csv.read();
					if (null==row) {
						break;
					}
					if (null==plotType) {
						if (row.isEmpty()) {
							plotType=PlotType.LINE;
						}
						else {
							try {
								plotType=PlotType.valueOf(row.get(0));
								continue;
							}
							catch (IllegalArgumentException ex) {
								plotType=PlotType.LINE;
							}
						}
					}
					if (2>row.size()) {
						continue;
					}
					try {
						long key=Long.parseUnsignedLong(row.get(0));
						double value=Double.parseDouble(row.get(1));
						sample2.add(key, value);
					}
					catch (NumberFormatException ex) {
					}
				}
			}
			if (0>=sample2.size()) {
				throw new MessageException(
						String.format("üres mintafájl %1$s", path));
			}
			Color color=colors.get(ii);
			String label=path.getFileName().toString();
			if (label.toLowerCase().endsWith(".csv")) {
				label=label.substring(0, label.length()-4);
			}
			samples.add(sample2.create(
					label, Color.INTERPOLATION, plotType, color));
		}
		progress.finished();
	}
	
	@Override
	protected void foreground() throws Throwable {
		if (null!=samples) {
			parent.addSamples(samples);
		}
	}
	
	public static void start(Plotter plotter) {
		JFileChooser chooser=new JFileChooser(
				plotter.session.database.samplesDirectory().toFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
        chooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION
						!=chooser.showOpenDialog(plotter.window())) {
            return;
        }
		File[] files=chooser.getSelectedFiles();
		if ((null==files)
				|| (0>=files.length)) {
			return;
		}
		List<Path> paths=new ArrayList<>(files.length);
		for (File file: files) {
			paths.add(file.toPath());
		}
        new LoadSampleProcess(plotter, paths)
                .start(plotter.session.executor);
	}
}
