package gui.check;

import gui.Gui;
import gui.io.Segments;
import gui.ui.GuiProcess;
import javax.swing.JFrame;

class StartProcess extends GuiProcess<Gui, JFrame> {
		private Segments segments;
		
		public StartProcess(Gui parent) {
			super(true, parent, CheckSegments.TITLE);
		}
		
		@Override
		protected void background() throws Throwable {
			segments=parent.session.database.readSegments(progress);
		}
		
		@Override
		protected void foreground() throws Throwable {
			if (segments.segments.isEmpty()) {
				parent.showMessage("no segments");
			}
			else {
				new CheckSegments(parent, segments)
						.show();
			}
		}
}
