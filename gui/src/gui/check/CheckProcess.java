package gui.check;

import gui.io.LargePrimesProducer;
import gui.io.PrimesProducer;
import gui.io.Segment;
import gui.io.SmallPrimesProducer;
import gui.ui.GuiProcess;
import gui.ui.progress.Progress;
import java.util.List;
import javax.swing.JFrame;

abstract class CheckProcess extends GuiProcess<CheckSegments, JFrame> {
	private final PrimesProducer largePrimes;
	private final List<Segment.Info> segmentInfos;
	private final int[] selectedRows;
	private final PrimesProducer smallPrimes=new SmallPrimesProducer();

	public CheckProcess(CheckSegments parent, List<Segment.Info> segmentInfos,
			int[] selectedRows) {
		super(true, parent, CheckSegments.TITLE);
		this.segmentInfos=segmentInfos;
		this.selectedRows=selectedRows;
		long maxSegmentStart=0l;
		for (int ii=0; selectedRows.length>ii; ++ii) {
			Segment.Info info=segmentInfos.get(selectedRows[ii]);
			if (0>Long.compareUnsigned(maxSegmentStart, info.segmentStart)) {
				maxSegmentStart=info.segmentStart;
			}
		}
		largePrimes=new LargePrimesProducer(parent.session.database);
	}

	@Override
	protected void background() throws Throwable {
		Segment readSegment=new Segment();
		Segment generatedSegment=new Segment();
		for (int ii=0; selectedRows.length>ii; ++ii) {
			Progress subProgress=progress.subProgress(
					1.0*ii/selectedRows.length,
					String.format("segment %1$,d/%2$,d",
							ii+1, selectedRows.length),
					1.0*(ii+1)/selectedRows.length);
			subProgress.progress(0.0);
			Segment.Info info=segmentInfos.get(selectedRows[ii]);
			readSegment.read(info.path);
			subProgress.progress(0.01);
			generatedSegment.clear(0l, 0l, 0l, true,
					readSegment.segmentStart);
			gui.io.PrimesProducer primes2;
			if (1l==readSegment.segmentStart) {
				generatedSegment.setComposite(0);
				primes2=smallPrimes;
			}
			else {
				primes2=largePrimes;
			}
			generate(primes2,
					subProgress.subProgress(0.01, "generate", 0.9),
					generatedSegment);
			subProgress.progress(0.9);
			check(generatedSegment,
					subProgress.subProgress(0.9, "check", 1.0),
					readSegment);
			subProgress.progress(1.0);
		}
		progress.finished();
	}

	private void check(Segment generatedSegment, Progress progress,
			Segment readSegment) throws Throwable {
		readSegment.compare(generatedSegment, progress);
	}

	@Override
	protected void foreground() throws Throwable {
		parent.showMessage("all segments checked out.");
	}

	protected abstract void generate(PrimesProducer primesProducer,
			Progress progress, Segment segment) throws Throwable;
}
