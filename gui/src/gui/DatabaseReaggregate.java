package gui;

import gui.ui.GuiProcess;
import javax.swing.JFrame;

public class DatabaseReaggregate {
    public static final char MNEMONIC='a';
    public static final String TITLE="DB reaggregate";
    
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
			parent.showMessage("reaggregate completed");
        }
    }
    
    public static void start(Gui gui) throws Throwable {
        new Process(gui)
				.start(gui.session.executor);
    }
}
