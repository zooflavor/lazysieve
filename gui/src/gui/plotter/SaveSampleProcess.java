package gui.plotter;

import gui.graph.Sample;
import gui.io.CSVWriter;
import gui.ui.GuiProcess;
import gui.ui.progress.Progress;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SaveSampleProcess extends GuiProcess<Plotter, JFrame> {
	private final Path path;
	private final Sample sample;
	
	private SaveSampleProcess(Plotter parent, Path path, Sample sample) {
		super(true, parent, Plotter.TITLE);
		this.path=path;
		this.sample=sample;
	}
	
	@Override
	protected void background() throws Throwable {
        SaveSampleProcess.save(path, progress, sample);
	}
	
	@Override
	protected void foreground() throws Throwable {
	}
    
    public static void save(Path path, Progress progress, Sample sample)
            throws Throwable {
		try (CSVWriter csv=CSVWriter.open(path)) {
			csv.write(Arrays.asList(sample.plotType.toString(), ""));
			for (int ii=0; sample.size()>ii; ++ii) {
				progress.progress(1.0*ii/sample.size());
				csv.write(Arrays.asList(
						Long.toUnsignedString(sample.xx(ii)),
						Double.toString(sample.yy(ii))));
			}
		}
		progress.finished();
    }
	
	public static void start(Plotter plotter, Sample sample) {
        JFileChooser chooser=new JFileChooser(
				plotter.session.database.samplesDirectory().toFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
        chooser.setMultiSelectionEnabled(false);
		chooser.setSelectedFile(new File(chooser.getCurrentDirectory(),
				sample.label+".csv"));
        if ((JFileChooser.APPROVE_OPTION
						!=chooser.showSaveDialog(plotter.window()))
                || (null==chooser.getSelectedFile())) {
            return;
        }
		if (Files.exists(chooser.getSelectedFile().toPath())
				&& (JOptionPane.YES_OPTION
						!=JOptionPane.showConfirmDialog(plotter.window(),
								String.format("Felülírja a %1$s fájlt?",
										chooser.getSelectedFile()),
								Plotter.TITLE,
								JOptionPane.YES_NO_OPTION))) {
			return;
		}
        new SaveSampleProcess(
						plotter, chooser.getSelectedFile().toPath(), sample)
                .start(plotter.session.executor);
	}
}
