package gui.sieve;

import gui.graph.Sample2D;
import gui.ui.progress.Progress;

public interface SieveMeasure {
	Sample2D measure(Progress progress) throws Throwable;
}
