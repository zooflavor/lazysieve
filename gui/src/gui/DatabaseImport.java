package gui;

import gui.ui.GuiProcess;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class DatabaseImport {
    public static final char MNEMONIC='m';
    public static final String TITLE="DB import";
    
    private static class Process extends GuiProcess<JFrame, Gui> {
        private final Path path;
        
        public Process(Gui parent, Path path) {
            super(true, parent, TITLE);
            this.path=path;
        }
        
        @Override
        protected void background() throws Throwable {
            parent.database.importAggregates(path, progress);
        }
        
        @Override
        protected void foreground() throws Throwable {
			parent.showMessage("import completed");
        }
    }
    
    public static void start(Gui gui) throws Throwable {
        JFileChooser chooser
                =new JFileChooser(gui.database.rootDirectory.toFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if ((JFileChooser.APPROVE_OPTION!=chooser.showOpenDialog(chooser))
                || (null==chooser.getSelectedFile())
                || (!Files.exists(chooser.getSelectedFile().toPath()))) {
            return;
        }
        new Process(gui, chooser.getSelectedFile().toPath())
                .start(gui.executor);
    }
}
