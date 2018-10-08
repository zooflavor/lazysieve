package gui.check;

import gui.io.Database;
import gui.io.Segment;
import gui.math.UnsignedLongMath;
import gui.ui.GuiProcess;
import gui.ui.progress.Progress;
import gui.util.LongList;
import java.util.List;
import javax.swing.JFrame;

abstract class CheckProcess extends GuiProcess<JFrame, CheckSegments> {
	@FunctionalInterface
	protected static interface Primes {
		LongList get(Progress progress) throws Throwable;
	}
	
	private LongList primes;
	private long primesMax;
	private final List<Segment.Info> segmentInfos;
	private final int[] selectedRows;
	private LongList smallPrimes;

	public CheckProcess(CheckSegments parent, List<Segment.Info> segmentInfos,
			int[] selectedRows) {
		super(true, parent, CheckSegments.TITLE);
		this.segmentInfos=segmentInfos;
		this.selectedRows=selectedRows;
	}

	@Override
	protected void background() throws Throwable {
		Segment readSegment=new Segment();
		Segment generatedSegment=new Segment();
		long maxSegmentStart=0l;
		for (int ii=0; selectedRows.length>ii; ++ii) {
			Segment.Info info=segmentInfos.get(selectedRows[ii]);
			if (0>Long.compareUnsigned(maxSegmentStart, info.segmentStart)) {
				maxSegmentStart=info.segmentStart;
			}
		}
		primesMax=UnsignedLongMath
				.squareRootFloor(maxSegmentStart+Segment.NUMBERS);
		Progress subProgress
				=progress.subProgress(0.1, null, 1.0);
		for (int ii=0; selectedRows.length>ii; ++ii) {
			Progress subProgress2=subProgress.subProgress(
					1.0*ii/selectedRows.length,
					String.format("segment %1$,d/%2$,d",
							ii+1, selectedRows.length),
					1.0*(ii+1)/selectedRows.length);
			subProgress2.progress(0.0);
			Segment.Info info=segmentInfos.get(selectedRows[ii]);
			readSegment.read(info.path);
			subProgress2.progress(0.1);
			generatedSegment.clear(0l, 0l, 0l, readSegment.segmentStart);
			Primes primes2;
			if (1l==readSegment.segmentStart) {
				generatedSegment.setNotPrime(0);
				primes2=(progress2)->{
					if (null==smallPrimes) {
						smallPrimes=Database.smallPrimes(progress2);
					}
					progress2.finished();
					return smallPrimes;
				};
			}
			else {
				primes2=(progress2)->{
					if (null==primes) {
						primes=parent.gui.database
								.readPrimes(primesMax, progress2);
					}
					progress2.finished();
					return primes;
				};
			}
			generate(primes2,
					subProgress2.subProgress(0.1, "generate", 0.9),
					generatedSegment);
			subProgress2.progress(0.9);
			check(generatedSegment,
					subProgress2.subProgress(0.9, "check", 1.0),
					readSegment);
			subProgress2.progress(1.0);
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

	protected abstract void generate(CheckProcess.Primes primes,
			Progress progress, Segment segment) throws Throwable;
}
