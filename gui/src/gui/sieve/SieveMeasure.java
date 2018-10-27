package gui.sieve;

import gui.graph.Sample;
import gui.ui.progress.Progress;

public interface SieveMeasure {
	Sample measure(Progress progress) throws Throwable;
}
