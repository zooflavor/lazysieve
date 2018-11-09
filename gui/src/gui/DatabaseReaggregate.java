package gui;

import gui.ui.GuiProcess;
import javax.swing.JFrame;

public class DatabaseReaggregate {
    public static final char MNEMONIC='t';
    public static final String TITLE="DB összesítés";
    
    public static class Process extends GuiProcess<Gui, JFrame> {
        public Process(Gui gui) {
            super(true, gui, TITLE);
        }

        @Override
        protected void background() throws Throwable {
            parent.session.database.reaggregate(progress);
        }

        @Override
        protected void foreground() throws Throwable {
			parent.showMessage("összesítés befejeződött");
        }
    }
    
    public static void start(Gui gui) throws Throwable {
        new Process(gui)
				.start(gui.session.executor);
    }
}
