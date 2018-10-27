package gui.plotter;

import gui.graph.Sample;
import gui.ui.GuiProcess;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
		try (OutputStream os=Files.newOutputStream(path);
				OutputStream bos=new BufferedOutputStream(os);
				Writer wr=new OutputStreamWriter(bos,
						StandardCharsets.US_ASCII);
				Writer bw=new BufferedWriter(wr);
				PrintWriter pw=new PrintWriter(bw)) {
			pw.println(sample.plotType.toString());
			for (int ii=0; sample.size()>ii; ++ii) {
				progress.progress(1.0*ii/sample.size());
				pw.print(sample.xx(ii));
				pw.print(",");
				pw.print(sample.yy(ii));
				pw.println();
			}
		}
		progress.finished();
	}
	
	@Override
	protected void foreground() throws Throwable {
	}
	
	public static void start(Plotter plotter, Sample sample) {
        JFileChooser chooser=new JFileChooser(
				plotter.session.database.rootDirectory.toFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
								String.format("Overwrite %1$s?",
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
