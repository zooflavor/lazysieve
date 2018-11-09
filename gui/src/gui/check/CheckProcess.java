package gui.check;

import gui.ui.GuiProcess;
import java.util.List;
import javax.swing.JFrame;

class CheckProcess extends GuiProcess<CheckSegments, JFrame> {
	private final ReferenceSegment referenceSegment;
	private final List<Long> segmentStarts;
	
	public CheckProcess(CheckSegments parent,
			ReferenceSegment referenceSegment, List<Long> segmentStarts) {
		super(true, parent, CheckSegments.TITLE);
		this.referenceSegment=referenceSegment;
		this.segmentStarts=segmentStarts;
	}

	@Override
	protected void background() throws Throwable {
		CheckSegments.checkSegments(parent.session.database, progress,
				referenceSegment, segmentStarts);
	}

	@Override
	protected void foreground() throws Throwable {
		parent.showMessage("Az szegmensek helyesek.");
	}
}
