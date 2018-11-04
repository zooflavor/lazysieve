package gui.io;

import gui.ui.progress.Progress;

@FunctionalInterface
public interface AggregatesConsumer {
	void consume(AggregateBlock aggregateBlock, Progress progress)
			throws Throwable;
}
